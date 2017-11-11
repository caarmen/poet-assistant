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
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class PoetAssistantActivityTestRule<T extends Activity> extends ActivityTestRule<T> {

    private boolean mClearOnLaunch = true;
    public PoetAssistantActivityTestRule(Class<T> clazz, boolean launchActivity) {
        super(clazz, false, launchActivity);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        if (mClearOnLaunch) ActivityTestRules.beforeActivityLaunched(getInstrumentation().getTargetContext());
    }

    @Override
    protected void afterActivityFinished() {
        if(mClearOnLaunch) ActivityTestRules.afterActivityFinished(getInstrumentation().getTargetContext());
        super.afterActivityFinished();
    }

    public void relaunch() {
        mClearOnLaunch = false;
        finishActivity();
        launchActivity(getActivityIntent());
        mClearOnLaunch = true;
    }
}
