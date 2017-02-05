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

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ActivityMainBinding;

final class AppBarLayoutHelper {
    private AppBarLayoutHelper() {
        // prevent instantiation
    }

    static void enableAutoHide(ActivityMainBinding binding) {
        Context context = binding.getRoot().getContext();
        if (context.getResources().getBoolean(R.bool.toolbar_autohide)) {
            enableAutoHide(binding.toolbar);
            enableAutoHide(binding.tabs);
        }
    }

    static void disableAutoHide(ActivityMainBinding binding) {
        Context context = binding.getRoot().getContext();
        if (context.getResources().getBoolean(R.bool.toolbar_autohide)) {
            disableAutoHide(binding.toolbar);
            disableAutoHide(binding.tabs);
        }
    }


    private static void enableAutoHide(View view) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) view.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        view.setLayoutParams(params);
    }

    private static void disableAutoHide(View view) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) view.getLayoutParams();
        params.setScrollFlags(0);
        view.setLayoutParams(params);
    }

    /**
     * @return true if the given fragment can function with the AppBarLayout hidden.
     */
    static boolean shouldAllowAutoHide(Fragment fragment) {
        // If we're in a fragment which doesn't have a list of data, show the app bar layout again (in case it's hiding).
        View fragmentView = fragment.getView();
        if (fragmentView != null) {
            RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
            return recyclerView != null
                    && recyclerView.getAdapter() != null
                    && recyclerView.getAdapter().getItemCount() > 0;
        }
        return false;
    }
}
