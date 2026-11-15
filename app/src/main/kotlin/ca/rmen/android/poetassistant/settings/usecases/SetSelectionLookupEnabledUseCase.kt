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

package ca.rmen.android.poetassistant.settings.usecases

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ca.rmen.android.poetassistant.main.dictionaries.search.DictionaryRouterActivity
import ca.rmen.android.poetassistant.main.dictionaries.search.RhymerRouterActivity
import ca.rmen.android.poetassistant.main.dictionaries.search.ThesaurusRouterActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Enables or disables the process-text routers (the "look up selected
 * text" behavior): the router activities are enabled or disabled as
 * manifest components, so the system text-selection menu shows or
 * hides the Poet Assistant entries.
 *
 * @param context The application context.
 */
class SetSelectionLookupEnabledUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Enables or disables the process-text router activities.
     */
    operator fun invoke(enabled: Boolean) {
        setEnabled(RhymerRouterActivity::class.java, enabled)
        setEnabled(ThesaurusRouterActivity::class.java, enabled)
        setEnabled(DictionaryRouterActivity::class.java, enabled)
    }

    private fun setEnabled(activityClass: Class<out Activity>, enabled: Boolean) {
        val componentName = ComponentName(context, activityClass)
        context.packageManager.setComponentEnabledSetting(
            componentName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
