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
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.view.View;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ActivityMainBinding;

public final class AppBarLayoutHelper {
    private AppBarLayoutHelper() {
        // prevent instantiation
    }

    static void enableAutoHide(ActivityMainBinding binding) {
        Context context = binding.getRoot().getContext();
        if (context.getResources().getBoolean(R.bool.toolbar_auto_hide)) {
            enableAutoHide(binding.toolbar);
            enableAutoHide(binding.tabs);
        }
    }

    private static void enableAutoHide(View view) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) view.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        view.setLayoutParams(params);
    }

    public static void forceExpandAppBarLayout(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        AppBarLayout appBarLayout = (AppBarLayout) activity.findViewById(R.id.app_bar_layout);
        if (appBarLayout != null) {
            forceExpandAppBarLayout(appBarLayout);
        }
    }

    static void forceExpandAppBarLayout(final AppBarLayout appBarLayout) {
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
