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
import androidx.test.espresso.intent.rule.IntentsTestRule;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class PoetAssistantIntentsTestRule<T extends Activity> extends IntentsTestRule<T> {

    public PoetAssistantIntentsTestRule(Class<T> clazz) {
        super(clazz, false, true);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        ActivityTestRules.beforeActivityLaunched(getInstrumentation().getTargetContext());
    }

    @Override
    protected void afterActivityFinished() {
        ActivityTestRules.afterActivityFinished(getInstrumentation().getTargetContext());
        super.afterActivityFinished();
    }
}
