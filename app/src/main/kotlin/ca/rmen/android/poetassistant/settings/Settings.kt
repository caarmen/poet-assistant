package ca.rmen.android.poetassistant.settings

import androidx.core.app.NotificationCompat

enum class Theme {
    LIGHT,
    DARK,
    AUTO,
}
enum class Layout {
    CLEAN,
    EFFICIENT
}

/**
 * Priority for the WOTD system notification. Only has an effect below
 * API 26; above it the notification channel importance governs.
 */
enum class WotdNotificationPriority(val priority: Int) {
    MAX(NotificationCompat.PRIORITY_MAX),
    HIGH(NotificationCompat.PRIORITY_HIGH),
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    LOW(NotificationCompat.PRIORITY_LOW),
    MIN(NotificationCompat.PRIORITY_MIN),
}

data class Settings(
    val layout: Layout,
    val theme: Theme,
    val isThesaurusReverseLookupEnabled: Boolean,
    val isAllRhymesEnabled: Boolean,
    val isAOAAMatchEnabled: Boolean,
    val isAORAOMatchEnabled: Boolean,
    val isWotdEnabled: Boolean,
    val wotdNotificationPriority: WotdNotificationPriority,
    val voice: String,
    val voiceSpeed: Int,
    val voicePitch: Int,
    val isSelectionLookupEnabled: Boolean,
) {
    companion object {
        /**
         * Sentinel voice value meaning "use the system default voice".
         * Same string value as the legacy SettingsPrefs.VOICE_SYSTEM constant:
         * both stacks read and write the same preferences file.
         */
        const val VOICE_SYSTEM = "VOICE_SYSTEM"
    }
}
