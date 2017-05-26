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
import android.app.Application;
import android.os.Bundle;
import android.support.test.espresso.IdlingResource;

import ca.rmen.android.poetassistant.Constants;

public class ActivityVisibleIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + ActivityVisibleIdlingResource.class.getSimpleName();
    private final Application mApplication;
    private final String mTargetActivityClassName;

    private ResourceCallback mCallback;
    private String mCurrentActivityClassName;

    /**
     * Keeps track of the currently visible activity by registering an {@link android.support.test.runner.lifecycle.ActivityLifecycleCallback}
     * with the {@link Application}.  Call {@link #destroy()} to unregister the lifecycle callback.
     */
    public ActivityVisibleIdlingResource(Application application, String targetActivityClassName) {
        mApplication = application;
        mTargetActivityClassName = targetActivityClassName;
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        boolean isShowing = mTargetActivityClassName.equals(mCurrentActivityClassName);
        if (isShowing && mCallback != null) mCallback.onTransitionToIdle();
        return isShowing;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

    public void destroy() {
        mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    }

    private final Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            mCurrentActivityClassName = activity.getClass().getName();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (activity.getClass().getName().equals(mCurrentActivityClassName)) {
                mCurrentActivityClassName = null;
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

}
