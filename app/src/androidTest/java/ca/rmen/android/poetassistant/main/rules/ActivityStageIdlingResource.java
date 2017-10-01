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

import android.support.test.espresso.IdlingResource;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.EnumSet;
import java.util.Set;

import ca.rmen.android.poetassistant.Constants;
import io.reactivex.Observable;

public class ActivityStageIdlingResource implements IdlingResource {
    private static final String TAG = Constants.TAG + ActivityStageIdlingResource.class.getSimpleName();
    private final String mTargetActivityClassName;

    private ResourceCallback mCallback;
    private Set<Stage> mMatchingStages;

    /**
     * Becomes idle when the given activity enters the given stage.
     */
    public ActivityStageIdlingResource(String targetActivityClassName, Stage stage) {
        this(targetActivityClassName, EnumSet.of(stage));
    }

    /**
     * Becomes idle when the given activity enters one of the given stages.
     */
    public ActivityStageIdlingResource(String targetActivityClassName, Set<Stage> stages) {
        mMatchingStages = stages;
        mTargetActivityClassName = targetActivityClassName;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        boolean isShowing = Observable.fromIterable(mMatchingStages)
                .flatMapIterable(stage -> ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))
                .any(matchedActivity -> mTargetActivityClassName.equals(matchedActivity.getClass().getName()))
                .blockingGet();
        if (isShowing && mCallback != null) mCallback.onTransitionToIdle();
        return isShowing;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

}
