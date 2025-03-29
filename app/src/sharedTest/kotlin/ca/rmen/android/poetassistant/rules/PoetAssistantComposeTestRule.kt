/*
 * Copyright (c) 2017 - present Carmen Alvarez
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
package ca.rmen.android.poetassistant.rules;


import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ca.rmen.android.poetassistant.main.rules.ActivityTestRules
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class PoetAssistantComposeTestRule<T : androidx.activity.ComponentActivity>(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<T>, T>
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        ActivityTestRules.beforeActivityLaunched(composeTestRule.activity);
    }

    override fun finished(description: Description?) {
        super.finished(description)
        ActivityTestRules.beforeActivityLaunched(composeTestRule.activity);
    }
}
