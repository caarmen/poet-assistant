package ca.rmen.android.poetassistant.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import ca.rmen.android.poetassistant.main.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


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
        theme = theme,
        isThesaurusReverseLookupEnabled = sharedPreferences.getBoolean(PREF_THESAURUS_REVERSE_LOOKUP_ENABLED, false),
        isAllRhymesEnabled = sharedPreferences.getBoolean(PREF_ALL_RHYMES_ENABLED, false),
        isAOAAMatchEnabled = sharedPreferences.getBoolean(PREF_MATCH_AO_AA_ENABLED, false),
        isAORAOMatchEnabled = sharedPreferences.getBoolean(PREF_MATCH_AOR_AO_ENABLED, false),
        // Defaults: WOTD disabled, priority default.
        // Unknown stored priority values map to DEFAULT.
        isWotdEnabled = sharedPreferences.getBoolean(PREF_WOTD_ENABLED, false),
        wotdNotificationPriority = when (sharedPreferences.getString(PREF_WOTD_NOTIFICATION_PRIORITY, PREF_WOTD_NOTIFICATION_PRIORITY_DEFAULT)) {
            PREF_WOTD_NOTIFICATION_PRIORITY_MAX -> WotdNotificationPriority.MAX
            PREF_WOTD_NOTIFICATION_PRIORITY_HIGH -> WotdNotificationPriority.HIGH
            PREF_WOTD_NOTIFICATION_PRIORITY_LOW -> WotdNotificationPriority.LOW
            PREF_WOTD_NOTIFICATION_PRIORITY_MIN -> WotdNotificationPriority.MIN
            else -> WotdNotificationPriority.DEFAULT
        },
        voice = voice,
        voiceSpeed = voiceSpeed,
        voicePitch = voicePitch,
        isSelectionLookupEnabled = sharedPreferences.getBoolean(PREF_SELECTION_LOOKUP, true),
    )

    fun setLayout(layout: Layout) {
        sharedPreferences.edit {
            putString(
                PREF_LAYOUT,
                when (layout) {
                    Layout.CLEAN -> PREF_LAYOUT_CLEAN
                    Layout.EFFICIENT -> PREF_LAYOUT_EFFICIENT
                }
            )
        }
    }

    var theme: Theme get() = when(sharedPreferences.getString(PREF_THEME, null)) {
        PREF_THEME_LIGHT -> Theme.LIGHT
        PREF_THEME_DARK -> Theme.DARK
        else -> Theme.AUTO
    }
        set(newValue) {
            sharedPreferences.edit {
                putString(
                    PREF_THEME,
                    when (newValue) {
                        Theme.LIGHT -> PREF_THEME_LIGHT
                        Theme.DARK -> PREF_THEME_DARK
                        Theme.AUTO -> PREF_THEME_AUTO
                    }
                )
            }
        }


    var voice : String get() =
        sharedPreferences.getString(PREF_VOICE, null) ?: Settings.VOICE_SYSTEM
        set(newValue) {
            sharedPreferences.edit { putString(PREF_VOICE, newValue) }
        }

    var voiceSpeed : Int get() =
        sharedPreferences.getInt(PREF_VOICE_SPEED, VOICE_SPEED_NORMAL)
        /**
         * Sets the voice speed. The value is clamped to the 0-200 range,
         * which is the range the sliders offer.
         */
        set(newValue) {
            sharedPreferences.edit {
                putInt(
                    PREF_VOICE_SPEED,
                    newValue.coerceIn(VOICE_SPEED_MIN, VOICE_SPEED_MAX)
                )
            }
        }

    var voicePitch: Int get() =
        sharedPreferences.getInt(PREF_VOICE_PITCH, VOICE_PITCH_NORMAL)
        /**
         * Sets the voice pitch. The value is clamped to the 0-200 range,
         * like the speed.
         */
        set(newValue) {
            sharedPreferences.edit {
                putInt(
                    PREF_VOICE_PITCH,
                    newValue.coerceIn(VOICE_PITCH_MIN, VOICE_PITCH_MAX)
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
    var tab : Tab get() = Tab.parse(sharedPreferences.getString(PREF_TAB, PREF_TAB_DEFAULT) ?: PREF_TAB_DEFAULT) ?: Tab.RHYMER

    set(newValue) {
        sharedPreferences.edit { putString(PREF_TAB, newValue.name) }
    }
}
