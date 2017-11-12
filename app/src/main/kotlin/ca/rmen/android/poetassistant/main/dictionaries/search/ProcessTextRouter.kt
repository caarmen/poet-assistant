/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.Tab
import java.util.Locale

object ProcessTextRouter {
    private val TAG = Constants.TAG + ProcessTextRouter::class.java.simpleName

    fun handleIntent(context: Context, intent: Intent, tab: Tab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Intent.ACTION_PROCESS_TEXT == intent.action) {
                val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
                if (!TextUtils.isEmpty(text)) {
                    val query = text.toString().trim().toLowerCase(Locale.US)
                    val uri = Uri.withAppendedPath(
                            Uri.parse("poetassistant://${tab.name.toLowerCase(Locale.US)}"),
                            query)
                    val mainActivityIntent = Intent(Intent.ACTION_VIEW)
                    mainActivityIntent.data = uri
                    Log.v(TAG, "Launching intent $mainActivityIntent")
                    context.startActivity(mainActivityIntent)
                }
            }
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        setEnabled(context, RhymerRouterActivity::class.java, enabled)
        setEnabled(context, ThesaurusRouterActivity::class.java, enabled)
        setEnabled(context, DictionaryRouterActivity::class.java, enabled)
    }

    private fun setEnabled(context: Context, clazz: Class<out Activity>, enabled: Boolean) {
        val pm = context.applicationContext.packageManager
        val componentName = ComponentName(context.packageName, clazz.name)
        pm.setComponentEnabledSetting(componentName,
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
    }
}
