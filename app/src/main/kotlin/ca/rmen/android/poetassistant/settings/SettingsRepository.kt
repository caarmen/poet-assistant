package ca.rmen.android.poetassistant.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
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
    )

    fun setLayout(layout: Layout) {
        sharedPreferences.edit().putString(
            PREF_LAYOUT,
            when (layout) {
                Layout.CLEAN -> PREF_LAYOUT_CLEAN
                else -> PREF_LAYOUT_EFFICIENT
            }
        ).apply()
    }

    fun setTheme(theme: Theme) {
        sharedPreferences.edit().putString(
            PREF_THEME,
            when (theme) {
                Theme.LIGHT -> PREF_THEME_LIGHT
                Theme.DARK -> PREF_THEME_DARK
                else -> PREF_THEME_AUTO
            }
        ).apply()
    }
}