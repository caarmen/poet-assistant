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

package ca.rmen.android.poetassistant.main.rules;

import android.app.Activity;
import android.support.test.espresso.IdlingResource;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import java.util.Collection;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class SearchResultReadyIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + SearchResultReadyIdlingResource.class.getSimpleName();
    private ResourceCallback mCallback;

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        Log.v(TAG, "isIdleNow");

        boolean isDisplayed = Single.create(
                (SingleOnSubscribe<Boolean>) singleEmitter ->
                        getInstrumentation().runOnMainSync(() -> singleEmitter.onSuccess(isSearchResultListVisible())))
                .blockingGet();

        if (isDisplayed && mCallback != null) mCallback.onTransitionToIdle();
        return isDisplayed;
    }

    private boolean isSearchResultListVisible() {
        Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        for (Activity activity : resumedActivities) {
            if (activity instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) activity;
                ViewPager viewPager = mainActivity.findViewById(R.id.view_pager);
                Fragment selectedTab = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                View rootView = selectedTab.getView();
                if (rootView == null) return false;
                View filterIcon = rootView.findViewById(R.id.btn_filter);
                Log.v(TAG, "Found filter icon in tab " + viewPager.getCurrentItem());
                return filterIcon != null && filterIcon.getVisibility() == View.VISIBLE && filterIcon.isShown();
            }
        }
        return false;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

}
