package ca.rmen.android.poetassistant.settings.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import javax.inject.Inject

/**
 * Use case for the system text-to-speech settings screen.
 * Owns the TTS settings intent: it reports whether a system activity
 * can handle it, and opens it.
 * Called with a context parameter (not injected) as it requires one.
 */
class OpenSystemTtsSettingsUseCase @Inject constructor() {

    /**
     * Returns true when a system activity can handle the TTS settings
     * intent.
     *
     * @param context The Android context used to query the package
     * manager.
     */
    fun canOpen(context: Context): Boolean {
        val intent = Intent(SYSTEM_TTS_SETTINGS_ACTION)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            ) != null
        } else {
            @Suppress("DEPRECATION")
            intent.resolveActivity(context.packageManager) != null
        }
    }

    /**
     * Opens the system TTS settings screen.
     *
     * @param context The Android context used to start the activity.
     */
    operator fun invoke(context: Context) {
        context.startActivity(Intent(SYSTEM_TTS_SETTINGS_ACTION))
    }

    private companion object {
        const val SYSTEM_TTS_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"
    }
}