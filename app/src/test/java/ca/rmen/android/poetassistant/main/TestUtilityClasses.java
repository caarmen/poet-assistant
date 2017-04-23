/*
 * Copyright (c) 2016 Carmen Alvarez
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


import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import ca.rmen.android.poetassistant.compat.HtmlCompat;
import ca.rmen.android.poetassistant.compat.VectorCompat;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory;
import ca.rmen.android.poetassistant.main.dictionaries.Share;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTUtils;
import ca.rmen.android.poetassistant.main.dictionaries.search.Patterns;
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter;
import ca.rmen.android.poetassistant.widget.PopupMenuHelper;
import ca.rmen.android.poetassistant.wotd.Wotd;
import ca.rmen.android.poetassistant.wotd.WotdAlarm;
import ca.rmen.android.poetassistant.wotd.WotdJob;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUtilityClasses {

    @Test
    public void testHtmlCompat() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(HtmlCompat.class);
    }

    @Test
    public void testVectorCompat() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(VectorCompat.class);
    }

    @Test
    public void testAppBarLayoutHelper() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(AppBarLayoutHelper.class);
    }

    @Test
    public void testTextPopupMenu() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(TextPopupMenu.class);
    }

    @Test
    public void testResultListFactory() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(ResultListFactory.class);
    }

    @Test
    public void testShare() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(Share.class);
    }

    @Test
    public void testRTUtils() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(RTUtils.class);
    }

    @Test
    public void testPatterns() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(Patterns.class);
    }

    @Test
    public void testProcessTextRouter() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(ProcessTextRouter.class);
    }

    @Test
    public void testPopupMenuHelper() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(PopupMenuHelper.class);
    }

    @Test
    public void testWotd() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(Wotd.class);
    }

    @Test
    public void testWotdAlarm() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(WotdAlarm.class);
    }

    @Test
    public void testWotdJob() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assertUtilityClassVisibility(WotdJob.class);
    }

    private void assertUtilityClassVisibility(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        assertTrue(Modifier.isFinal(clazz.getModifiers()));
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        // For code coverage we have to invoke the constructor:
        // http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
