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

package ca.rmen.android.poetassistant.main

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

// thanks to http://stackoverflow.com/questions/33382344/espresso-test-click-x-y-coordinates
object CustomViewActions {

    @JvmStatic
    fun longTap(x: Int, y: Int): ViewAction {
        return GeneralClickAction(
                Tap.LONG,
                { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)
                    val screenX = screenPos[0].toFloat() + x
                    val screenY = screenPos[1].toFloat() + y

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER,
                0, 0)
    }

    @JvmStatic
    fun scrollToEnd(): ViewAction {
        return object : ViewAction {

            @Suppress("UNCHECKED_CAST")
            override fun getConstraints(): Matcher<View> {
                return allOf(isAssignableFrom(AdapterView::class.java), isDisplayed())
            }

            override fun getDescription(): String {
                return "scroll AdapterView to the end"
            }

            override fun perform(uiController: UiController, view: View) {
                val adapterView = view as AdapterView<*>
                val count = adapterView.adapter.count
                adapterView.setSelection(count - 1)
            }
        }
    }

    @JvmStatic
    fun clickLastChild(): ViewAction {
        return object : ViewAction {

            @Suppress("UNCHECKED_CAST")
            override fun getConstraints(): Matcher<View> {
                return allOf(isAssignableFrom(ViewGroup::class.java), isDisplayed())
            }

            override fun getDescription(): String {
                return "Click the last child in a ViewGroup"
            }

            override fun perform(uiController: UiController, view: View) {
                val viewGroup = view as ViewGroup
                val lastChild = viewGroup.getChildAt(viewGroup.childCount - 1)
                GeneralClickAction(Tap.SINGLE,
                        { _ -> GeneralLocation.CENTER.calculateCoordinates(lastChild) },
                        Press.FINGER, 0, 0)
                        .perform(uiController, view)
            }
        }
    }
}
