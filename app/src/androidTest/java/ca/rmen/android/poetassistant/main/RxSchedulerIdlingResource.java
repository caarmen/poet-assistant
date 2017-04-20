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

import android.support.test.espresso.IdlingResource;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;

class RxSchedulerIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + RxSchedulerIdlingResource.class.getSimpleName();
    private final IdlingScheduler mScheduler;
    private ResourceCallback mCallback;


    RxSchedulerIdlingResource(IdlingScheduler scheduler) {
        mScheduler = scheduler;
    }

    @Override
    public String getName() {
        return TAG + "/" + mScheduler;
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = mScheduler.countingIdlingResource().isIdleNow();
        if (idle) mCallback.onTransitionToIdle();
        Log.v(TAG, "isIdleNow: " + idle);
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

}
