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

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

import com.google.android.material.appbar.AppBarLayout;

public final class CustomViewActions {
    private CustomViewActions() {
        // prevent instantiation
    }

    // thanks to http://stackoverflow.com/questions/33382344/espresso-test-click-x-y-coordinates
    public static ViewAction longTap(final int x, final int y) {
        return new GeneralClickAction(
                Tap.LONG,
                view -> {
                    final int[] screenPos = new int[2];
                    view.getLocationOnScreen(screenPos);
                    final float screenX = screenPos[0] + x;
                    final float screenY = screenPos[1] + y;

                    return new float[]{screenX, screenY};
                },
                Press.FINGER,
                0, 0);
    }

    static ViewAction scrollToEnd() {
        return new ViewAction() {

            @SuppressWarnings("unchecked")
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(AdapterView.class), isDisplayed());
            }

            @Override
            public String getDescription() {
                return "scroll AdapterView to the end";
            }

            @Override
            public void perform(UiController uiController, View view) {
                AdapterView adapterView = (AdapterView) view;
                int count = adapterView.getAdapter().getCount();
                adapterView.setSelection(count - 1);
            }
        };
    }

    static ViewAction clickLastChild() {
        return new ViewAction() {

            @SuppressWarnings("unchecked")
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(ViewGroup.class), isDisplayed());
            }

            @Override
            public String getDescription() {
                return "Click the last child in a ViewGroup";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ViewGroup viewGroup = (ViewGroup) view;
                View lastChild = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
                new GeneralClickAction(Tap.SINGLE,
                        view1 -> GeneralLocation.CENTER.calculateCoordinates(lastChild),
                        Press.FINGER, 0, 0)
                        .perform(uiController, view);
            }
        };
    }

    static ViewAction expand() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(AppBarLayout.class));
            }

            @Override
            public String getDescription() {
                return "Expand an AppBarLayout";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((AppBarLayout)view).setExpanded(true);
            }
        };
    }

}
