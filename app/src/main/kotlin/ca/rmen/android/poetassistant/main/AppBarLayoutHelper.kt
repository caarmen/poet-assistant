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

package ca.rmen.android.poetassistant.main

import android.app.Activity
import com.google.android.material.appbar.AppBarLayout
import android.util.Log
import android.view.View
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R

object AppBarLayoutHelper {
    private val TAG = Constants.TAG + AppBarLayoutHelper::class.java.simpleName
    fun enableAutoHide(activity: Activity?) {
        Log.v(TAG, "enableAutoHide $activity")
        if (activity == null || activity.isFinishing) return
        if (activity.resources.getBoolean(R.bool.toolbar_auto_hide)) {
            enableAutoHide(activity.findViewById<View>(R.id.toolbar))
            enableAutoHide(activity.findViewById<View>(R.id.tabs))
        }
    }

    fun disableAutoHide(activity: Activity?) {
        Log.v(TAG, "disableAutoHide $activity")
        if (activity == null || activity.isFinishing) return
        if (activity.resources.getBoolean(R.bool.toolbar_auto_hide)) {
            disableAutoHide(activity.findViewById<View>(R.id.toolbar))
            disableAutoHide(activity.findViewById<View>(R.id.tabs))
        }
    }

    private fun enableAutoHide(view: View?) {
        Log.v(TAG, "enableAutoHide $view")
        if (view == null) return
        val params = view.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        view.layoutParams = params
    }

    private fun disableAutoHide(view: View?) {
        Log.v(TAG, "disableAutoHide $view")
        if (view == null) return
        val params = view.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = params.scrollFlags and (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP).inv()
        view.layoutParams = params
    }

    fun forceExpandAppBarLayout(activity: Activity?) {
        Log.v(TAG, "forceExpandAppBarLayout $activity")
        if (activity == null || activity.isFinishing) return
        val appBarLayout = activity.findViewById<AppBarLayout>(R.id.app_bar_layout)
        if (appBarLayout != null) {
            forceExpandAppBarLayout(appBarLayout)
        }
    }

    fun forceExpandAppBarLayout(appBarLayout: AppBarLayout) {
        Log.v(TAG, "forceExpandAppBarLayout $appBarLayout")
        // Add a 100ms delay to prevent this issue:
        // * The user is in the reader tab, with the keyboard open
        // * The user swipes quickly right to the empty favorites tab
        // * While we try to display the app bar layout, the soft keyboard is hidden by the app
        // * We have a glitch: the app bar layout seems to appear briefly but becomes hidden again.
        // With a small delay we try to make sure the event to show the app bar layout is done after
        // the soft keyboard is hidden.
        // I don't like this arbitrary delay :(
        appBarLayout.postDelayed({ appBarLayout.setExpanded(true, true) }, 100)
    }
}
