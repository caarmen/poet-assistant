/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.reader

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.OpenableColumns
import android.support.annotation.VisibleForTesting
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

data class PoemFile(val uri: Uri?, val name: String?, val text: String?) {
    companion object {
        private val TAG = Constants.TAG + PoemFile::class.java.simpleName

        fun open(context: Context, uri: Uri, callback: PoemFileCallback) {
            Log.d(TAG, "open(uri=$uri, callback=$callback")
            Single.fromCallable({ readPoemFile(context, uri) })
                    .doOnError({ throwable -> Log.w(TAG, "Couldn't open file", throwable) })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ poemFile -> callback.onPoemLoaded(poemFile) },
                            { _ -> callback.onPoemLoaded(null) })
        }

        @WorkerThread
        private fun readPoemFile(context: Context, uri: Uri): PoemFile? {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val text = inputStream.bufferedReader().use { it.readText() }
            val displayName = readDisplayName(context, uri)
            return PoemFile(uri, displayName, text)
        }

        fun save(context: Context, uri: Uri?, text: String, callback: PoemFileCallback) {
            Log.d(TAG, "save: uri=$uri, text=$text, callback=$callback")
            Single.fromCallable { savePoemFile(context, uri, text) }
                    .doOnError({ throwable -> Log.v(TAG, "Couldn't save file", throwable) })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ poemFile -> callback.onPoemSaved(poemFile) },
                            { _ -> callback.onPoemSaved(null) })
        }

        @WorkerThread
        private fun savePoemFile(context: Context, uri: Uri?, text: String): PoemFile {
            val outputStream = context.contentResolver.openOutputStream(uri, "w") ?: throw IOException("Couldn't open OutputStream to uri " + uri)
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.use {
                writer.write(text)
            }
            val displayName = readDisplayName(context, uri)
            return PoemFile(uri, displayName, text)
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun print(context: Context, poemFile: PoemFile, callback: PoemFileCallback) {
            val webView = WebView(context)
            print(context, webView, poemFile, callback)
        }

        @VisibleForTesting
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun print(context: Context, webView: WebView, poemFile: PoemFile, callback: PoemFileCallback) {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    var title = poemFile.name
                    title = if (TextUtils.isEmpty(title)) {
                        context.getString(R.string.print_default_title)
                    } else {
                        title!!.replace(Regex(".txt$"), ".pdf")
                    }
                    @Suppress("DEPRECATION")
                    val printDocumentAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webView.createPrintDocumentAdapter(title) else webView.createPrintDocumentAdapter()
                    val printAttributes = PrintAttributes.Builder().build()
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager?
                    if (printManager != null) {
                        val printJob = printManager.print(title, printDocumentAdapter, printAttributes)
                        callback.onPrintJobCreated(poemFile, printJob)
                    }
                }
            }
            val formattedText = context.getString(R.string.print_preview_html, poemFile.text)
            webView.loadDataWithBaseURL(null, formattedText, "text/html", "UTF-8", null)
        }

        /**
         * Generate a suggested filename based on the first few words of the poem text.
         */
        fun generateFileName(text: String): String? {
            val minLength = 8
            val maxLength = 16
            var textStart: String
            try {
                textStart = Pattern.compile("[^\\p{L}]+").matcher(text).replaceAll("-")
                textStart = textStart.replace(Regex("^-"), "")
            } catch (e: PatternSyntaxException) {
                // Not sure why \\p{IsAlphabetic} worked on unit tests but not on an android device.
                // \\p{L} worked on a couple of devices, but let's not take any chances.
                Log.v(TAG, "Couldn't generate file name for " + text + ": " + e.message, e)
                return null
            }
            textStart = textStart.substring(0, Math.min(maxLength, textStart.length))
            var lastWordBegin = textStart.length
            for (i in textStart.length - 1 downTo minLength + 1) {
                if (!Character.isLetter(textStart[i])) {
                    lastWordBegin = i
                    break
                }
            }
            // replace trailing hyphen
            textStart = textStart.replace(Regex("-$"), "")
            // If there's nothing left, give up.
            if (textStart.isEmpty()) return null
            if (textStart.length <= minLength) return textStart + ".txt"
            lastWordBegin = Math.min(lastWordBegin, textStart.length)
            textStart = textStart.substring(0, lastWordBegin) + ".txt"
            return textStart
        }

        /**
         * Generate a suggested filename based on the first few words of the poem text.
         */
        fun readDisplayName(context: Context, uri: Uri?): String? {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
            return null
        }
    }
}
