package ca.rmen.android.poetassistant.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ca.rmen.android.poetassistant.main.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit


@Singleton
class SettingsRepository @Inject constructor(application: Application) {

    companion object {
        private const val PREF_LAYOUT = "PREF_LAYOUT"
        private const val PREF_LAYOUT_CLEAN = "Clean"
        private const val PREF_LAYOUT_EFFICIENT = "Efficient"

        private const val PREF_THEME = "PREF_THEME"
        private const val PREF_THEME_LIGHT = "Light"
        private const val PREF_THEME_DARK = "Dark"
        private const val PREF_THEME_AUTO = "Auto"

        private const val PREF_THESAURUS_REVERSE_LOOKUP_ENABLED = "PREF_THESAURUS_REVERSE_LOOKUP_ENABLED"
        private const val PREF_ALL_RHYMES_ENABLED = "PREF_ALL_RHYMES_ENABLED"
        private const val PREF_MATCH_AO_AA_ENABLED = "PREF_MATCH_AO_AA_ENABLED"
        private const val PREF_MATCH_AOR_AO_ENABLED = "PREF_MATCH_AOR_AO_ENABLED"

        // WOTD settings.
        private const val PREF_WOTD_ENABLED = "PREF_WOTD_ENABLED"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY = "PREF_WOTD_NOTIFICATION_PRIORITY"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_MAX = "max"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_HIGH = "high"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT = "default"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_LOW = "low"
        private const val PREF_WOTD_NOTIFICATION_PRIORITY_MIN = "min"

        // Voice settings.
        private const val PREF_VOICE = "PREF_VOICE"
        private const val PREF_VOICE_SPEED = "PREF_VOICE_SPEED_V3"
        private const val PREF_VOICE_PITCH = "PREF_VOICE_PITCH_V3"
        private const val VOICE_SPEED_NORMAL = 100
        private const val VOICE_PITCH_NORMAL = 100
        private const val VOICE_SPEED_MIN = 0
        private const val VOICE_SPEED_MAX = 200
        private const val VOICE_PITCH_MIN = 0
        private const val VOICE_PITCH_MAX = 200

        private const val PREF_SELECTION_LOOKUP = "PREF_SELECTION_LOOKUP"

        // The last-opened tab. Persisted app state, not a user setting:
        // not part of the Settings object.
        private const val PREF_TAB = "PREF_TAB"
        private const val PREF_TAB_DEFAULT = "RHYMER"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    val settingsFlow: StateFlow<Settings> field = MutableStateFlow<Settings>(getSettings())

    private val sharedPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener = {_, _ ->
        settingsFlow.tryEmit(getSettings())
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPrefsListener)
    }

    /**
     * Returns the current settings, read synchronously from the prefs
     * in-memory state. The value is always fresh: apply() updates the
     * in-memory state before any prefs listener runs.
     */
    private fun getSettings() = Settings(
        layout = when (sharedPreferences.getString(PREF_LAYOUT, null)) {
            PREF_LAYOUT_CLEAN -> Layout.CLEAN
            else -> Layout.EFFICIENT
        },
        theme = when (sharedPreferences.getString(PREF_THEME, null)) {
            PREF_THEME_LIGHT -> Theme.LIGHT
            PREF_THEME_DARK -> Theme.DARK
            else -> Theme.AUTO
        },
        isThesaurusReverseLookupEnabled = sharedPreferences.getBoolean(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED, false),
        isAllRhymesEnabled = sharedPreferences.getBoolean(PREF_ALL_RHYMES_ENABLED, false),
        isAOAAMatchEnabled = sharedPreferences.getBoolean(PREF_MATCH_AO_AA_ENABLED, false),
        isAORAOMatchEnabled = sharedPreferences.getBoolean(PREF_MATCH_AOR_AO_ENABLED, false),
        // Defaults: WOTD enabled, priority default.
        // Unknown stored priority values map to DEFAULT.
        isWotdEnabled = sharedPreferences.getBoolean(PREF_WOTD_ENABLED, false),
        wotdNotificationPriority = when (sharedPreferences.getString(PREF_WOTD_NOTIFICATION_PRIORITY, PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT)) {
            PREF_WOTD_NOTIFICATION_PRIORITY_MAX -> WotdNotificationPriority.MAX
            PREF_WOTD_NOTIFICATION_PRIORITY_HIGH -> WotdNotificationPriority.HIGH
            PREF_WOTD_NOTIFICATION_PRIORITY_LOW -> WotdNotificationPriority.LOW
            PREF_WOTD_NOTIFICATION_PRIORITY_MIN -> WotdNotificationPriority.MIN
            else -> WotdNotificationPriority.DEFAULT
        },
        voice = sharedPreferences.getString(PREF_VOICE, null) ?: Settings.VOICE_SYSTEM,
        voiceSpeed = sharedPreferences.getInt(PREF_VOICE_SPEED, VOICE_SPEED_NORMAL),
        voicePitch = sharedPreferences.getInt(PREF_VOICE_PITCH, VOICE_PITCH_NORMAL),
        isSelectionLookupEnabled = sharedPreferences.getBoolean(PREF_SELECTION_LOOKUP, true),
    )

    fun setLayout(layout: Layout) {
        sharedPreferences.edit {
            putString(
                PREF_LAYOUT,
                when (layout) {
                    Layout.CLEAN -> PREF_LAYOUT_CLEAN
                    else -> PREF_LAYOUT_EFFICIENT
                }
            )
        }
    }

    fun setTheme(theme: Theme) {
        sharedPreferences.edit {
            putString(
                PREF_THEME,
                when (theme) {
                    Theme.LIGHT -> PREF_THEME_LIGHT
                    Theme.DARK -> PREF_THEME_DARK
                    else -> PREF_THEME_AUTO
                }
            )
        }
    }

    fun setVoice(voice: String) {
        sharedPreferences.edit { putString(PREF_VOICE, voice) }
    }

    /**
     * Sets the voice speed. The value is clamped to the 0-200 range:
     * the legacy screen enforced the maximum via SeekBarPreference.max,
     * and Compose sliders must not exceed it either.
     */
    fun setVoiceSpeed(voiceSpeed: Int) {
        sharedPreferences.edit {
            putInt(
                PREF_VOICE_SPEED,
                voiceSpeed.coerceIn(VOICE_SPEED_MIN, VOICE_SPEED_MAX)
            )
        }
    }

    /**
     * Sets the voice pitch. The value is clamped to the 0-200 range,
     * like the speed.
     */
    fun setVoicePitch(voicePitch: Int) {
        sharedPreferences.edit {
            putInt(
                PREF_VOICE_PITCH,
                voicePitch.coerceIn(VOICE_PITCH_MIN, VOICE_PITCH_MAX)
            )
        }
    }

    fun setSelectionLookupEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_SELECTION_LOOKUP, enabled) }
    }

    fun setWotdEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_WOTD_ENABLED, enabled) }
    }

    fun setWotdNotificationPriority(priority: WotdNotificationPriority) {
        val value = when (priority) {
            WotdNotificationPriority.MAX -> PREF_WOTD_NOTIFICATION_PRIORITY_MAX
            WotdNotificationPriority.HIGH -> PREF_WOTD_NOTIFICATION_PRIORITY_HIGH
            WotdNotificationPriority.LOW -> PREF_WOTD_NOTIFICATION_PRIORITY_LOW
            WotdNotificationPriority.MIN -> PREF_WOTD_NOTIFICATION_PRIORITY_MIN
            WotdNotificationPriority.DEFAULT -> PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT
        }
        sharedPreferences.edit { putString(PREF_WOTD_NOTIFICATION_PRIORITY, value) }
    }

    fun setAllRhymesEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_ALL_RHYMES_ENABLED, enabled) }
    }

    fun setAOAAMatchEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_MATCH_AO_AA_ENABLED, enabled) }
    }

    fun setAORAOMatchEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_MATCH_AOR_AO_ENABLED, enabled) }
    }

    fun setThesaurusReverseLookupEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED, enabled) }
    }

    /**
     * The last-opened tab: persisted app state, not a user setting.
     * Returns null if no valid tab is stored.
     */
    fun getTab(): Tab? = Tab.parse(sharedPreferences.getString(PREF_TAB, PREF_TAB_DEFAULT) ?: PREF_TAB_DEFAULT)

    fun setTab(tab: Tab) {
        sharedPreferences.edit { putString(PREF_TAB, tab.name) }
    }
}