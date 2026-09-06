package ca.rmen.android.poetassistant.main.common.usecases

import android.content.Context
import android.content.Intent
import ca.rmen.android.poetassistant.main.common.models.ExternalAppMenuItem
import javax.inject.Inject

/**
 * Use case for opening an external app with text to process.
 * Launches the specified external app with the given text using ACTION_PROCESS_TEXT intent.
 */
class OpenExternalAppUseCase @Inject constructor(){
    /**
     * Opens the specified external app with the given text.
     *
     * @param text The text to be processed by the external app.
     * @param externalApp The external app to open.
     * @param context The Android context used to start the activity.
     */
    operator fun invoke(text: String, externalApp: ExternalAppMenuItem, context: Context) {
        val intent = Intent()
            .setAction(Intent.ACTION_PROCESS_TEXT)
            .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            .setClassName(
                externalApp.packageName,
                externalApp.className
            )
        context.startActivity(intent)
    }
}
