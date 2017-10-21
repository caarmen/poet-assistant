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
import android.support.annotation.IdRes;
import android.support.test.espresso.IdlingResource;
import android.util.Log;
import android.view.View;

import ca.rmen.android.poetassistant.Constants;

public class DisplayedViewIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + DisplayedViewIdlingResource.class.getSimpleName();
    private ResourceCallback mCallback;
    private final Activity mActivity;
    @IdRes
    private final int mViewId;

    /**
     * Becomes idle when a view for the given matcher is displayed;
     */
    public DisplayedViewIdlingResource(Activity activity, @IdRes int viewId) {
        mActivity = activity;
        mViewId = viewId;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        Log.v(TAG, "isIdleNow");
        View view = mActivity.findViewById(mViewId);
        boolean isDisplayed = view != null && view.getVisibility() == View.VISIBLE && view.isShown();

        if (isDisplayed && mCallback != null) mCallback.onTransitionToIdle();
        return isDisplayed;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

}
