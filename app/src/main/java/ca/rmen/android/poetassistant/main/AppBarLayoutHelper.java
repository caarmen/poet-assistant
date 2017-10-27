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

package ca.rmen.android.poetassistant.main;

import android.app.Activity;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.View;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;

public final class AppBarLayoutHelper {
    private static final String TAG = Constants.TAG + AppBarLayoutHelper.class.getSimpleName();

    private AppBarLayoutHelper() {
        // prevent instantiation
    }

    public static void enableAutoHide(Activity activity) {
        Log.v(TAG, "enableAutohide " + activity);
        if (activity == null || activity.isFinishing()) return;
        if (activity.getResources().getBoolean(R.bool.toolbar_auto_hide)) {
            enableAutoHide(activity.findViewById(R.id.toolbar));
            enableAutoHide(activity.findViewById(R.id.tabs));
        }
    }

    public static void disableAutoHide(Activity activity) {
        Log.v(TAG, "disableAutohide " + activity);
        if (activity == null || activity.isFinishing()) return;
        if (activity.getResources().getBoolean(R.bool.toolbar_auto_hide)) {
            disableAutoHide(activity.findViewById(R.id.toolbar));
            disableAutoHide(activity.findViewById(R.id.tabs));
        }
    }

    private static void enableAutoHide(View view) {
        Log.v(TAG, "enableAutohide " + view);
        if (view == null) return;
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) view.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        view.setLayoutParams(params);
    }

    private static void disableAutoHide(View view) {
        Log.v(TAG, "disableAutohide " + view);
        if (view == null) return;
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) view.getLayoutParams();
        params.setScrollFlags(params.getScrollFlags() & ~(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP));
        view.setLayoutParams(params);
    }

    public static void forceExpandAppBarLayout(Activity activity) {
        Log.v(TAG, "forceExpandAppBarLayout " + activity);
        if (activity == null || activity.isFinishing()) return;
        AppBarLayout appBarLayout = activity.findViewById(R.id.app_bar_layout);
        if (appBarLayout != null) {
            forceExpandAppBarLayout(appBarLayout);
        }
    }

    static void forceExpandAppBarLayout(AppBarLayout appBarLayout) {
        Log.v(TAG, "forceExpandAppBarLayout " + appBarLayout);
        if (!appBarLayout.getContext().getResources().getBoolean(R.bool.toolbar_auto_hide)) return;
        // Add a 100ms delay to prevent this issue:
        // * The user is in the reader tab, with the keyboard open
        // * The user swipes quickly right to the empty favorites tab
        // * While we try to display the app bar layout, the soft keyboard is hidden by the app
        // * We have a glitch: the app bar layout seems to appear briefly but becomes hidden again.
        // With a small delay we try to make sure the event to show the app bar layout is done after
        // the soft keyboard is hidden.
        // I don't like this arbitrary delay :(
        appBarLayout.postDelayed(()->appBarLayout.setExpanded(true, true), 100);
    }
}
