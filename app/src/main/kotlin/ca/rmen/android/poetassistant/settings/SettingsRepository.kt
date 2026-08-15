package ca.rmen.android.poetassistant.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SettingsRepository(application: Application) {

    companion object {
        private const val PREF_LAYOUT = "PREF_LAYOUT"
        private const val PREF_LAYOUT_CLEAN = "Clean"
        private const val PREF_LAYOUT_EFFICIENT = "Efficient"

        private const val PREF_THEME = "PREF_THEME"
        private const val PREF_THEME_LIGHT = "Light"
        private const val PREF_THEME_DARK = "Dark"
        private const val PREF_THEME_AUTO = "Auto"

    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    val settingsFlow: StateFlow<Settings> field = MutableStateFlow<Settings>(getSettings())

    private val sharedPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener = {_, _ ->
        settingsFlow.tryEmit(getSettings())
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPrefsListener)
    }

    private fun getSettings() = Settings(
        layout = when (sharedPreferences.getString(PREF_LAYOUT, null)) {
            PREF_LAYOUT_CLEAN -> Layout.CLEAN
            else -> Layout.EFFICIENT
        },
        theme = when (sharedPreferences.getString(PREF_THEME, null)) {
            PREF_THEME_LIGHT -> Theme.LIGHT
            PREF_THEME_DARK -> Theme.DARK
            else -> Theme.AUTO
        }
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