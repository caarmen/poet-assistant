/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.settings.usecases

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import ca.rmen.android.poetassistant.settings.Theme
import javax.inject.Inject

/**
 * Applies a theme change: maps the [Theme] setting to the
 * [AppCompatDelegate] night mode. Replaces the legacy
 * Theme.setThemeFromSettings call, without SettingsPrefs.
 */
class ApplyThemeUseCase @Inject constructor() {

    /**
     * Applies the given theme. The AppCompatDelegate night mode change
     * triggers the uiMode configuration change and recreates the
     * activity: the explicit "restart settings activity" of legacy is
     * preserved as automatic recreation.
     */
    @MainThread
    operator fun invoke(theme: Theme) {
        when (theme) {
            Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
