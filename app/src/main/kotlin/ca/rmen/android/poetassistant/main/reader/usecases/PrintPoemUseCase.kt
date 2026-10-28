/*
 * Copyright (c) 2016-present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.reader.usecases

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.di.MainDispatcher
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Use case for printing the poem via the system print framework.
 * Wraps the offscreen-WebView render (platform capability, like
 * SpeakTextUseCase wraps Tts).
 *
 * The print name: the file-backed poem's file name when one exists,
 * else the generated save-as name. A file-backed poem without a file
 * name prints under the default print title — no generated fallback.
 *
 * @param applicationContext The application context: the WebView
 * renders with it, so it holds no activity — the WebView stays alive
 * while the print UI is up and is destroyed only on cancellation.
 * @param mainDispatcher The coroutine dispatcher for main-thread work
 * (WebView creation and PrintManager require the main thread).
 * @param poemRepository Repository for the current poem (the print
 * name source).
 * @param getSaveAsFilenameUseCase Use case for the generated name of
 * an unbound poem.
 */
class PrintPoemUseCase @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val poemRepository: PoemRepository,
    private val getSaveAsFilenameUseCase: GetSaveAsFilenameUseCase,
) {

    companion object {
        private val TAG = Constants.TAG + PrintPoemUseCase::class.java.simpleName
    }

    /**
     * Prints the poem.
     *
     * The poem renders inside an offscreen WebView with the
     * print_preview_html template. The print dialog title: the print
     * name with a .txt suffix replaced by .pdf, or the default print
     * title when the name is null or empty.
     *
     * The WebView stays alive after printing starts: the print
     * document adapter renders from its content while the system
     * print UI is up.
     *
     * @param activity The activity to print from: PrintManager accepts
     * only an activity context. Used for the print call only, never
     * stored.
     * @param poemText The poem text.
     * @return the created print job, or null when no print service is
     * available. A cancelled call throws CancellationException — the
     * WebView is destroyed in that case.
     */
    suspend operator fun invoke(
        activity: Activity,
        poemText: String,
    ): PrintJob? {
        val printName = when (val poem = poemRepository.poem.value) {
            is Poem.PersistedPoem -> poem.name
            else -> getSaveAsFilenameUseCase(poemText)
        }
        return print(activity, printName, poemText)
    }

    private suspend fun print(activity: Activity, poemName: String?, poemText: String): PrintJob? =
        withContext(mainDispatcher) {
            val webView = WebView(applicationContext)
            try {
                suspendCancellableCoroutine { continuation ->
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Some WebView builds report a finished page more
                            // than once; the coroutine is resumed (or
                            // cancelled) only by the first report.
                            if (!continuation.isActive) return
                            val title = when {
                                poemName.isNullOrEmpty() -> applicationContext.getString(R.string.print_default_title)
                                else -> poemName.replace(Regex(".txt$"), ".pdf")
                            }
                            val printDocumentAdapter = webView.createPrintDocumentAdapter(title)
                            val printAttributes = PrintAttributes.Builder().build()
                            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager?
                            if (printManager != null) {
                                val printJob = printManager.print(title, printDocumentAdapter, printAttributes)
                                Log.d(TAG, "Print job id = ${printJob.id}, info = ${printJob.info}")
                                continuation.resume(printJob)
                            } else {
                                Log.d(TAG, "No print service available")
                                continuation.resume(null)
                            }
                        }
                    }
                    val formattedText = applicationContext.getString(R.string.print_preview_html, poemText)
                    webView.loadDataWithBaseURL(null, formattedText, "text/html", "UTF-8", null)
                }
            } catch (e: CancellationException) {
                // The cancellation resumption travels through the main
                // dispatcher (withContext above), so this cleanup runs on
                // the main thread — WebView.destroy() requires it. The
                // WebView is only destroyed on cancellation: on success
                // it stays alive, because the print document adapter
                // renders from its content while the print UI is up.
                webView.destroy()
                throw e
            }
        }
}
