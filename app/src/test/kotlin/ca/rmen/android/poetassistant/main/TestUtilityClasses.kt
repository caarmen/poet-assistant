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

package ca.rmen.android.poetassistant.main

import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.compat.VectorCompat
import ca.rmen.android.poetassistant.main.dictionaries.ResultListFactory
import ca.rmen.android.poetassistant.main.dictionaries.Share
import ca.rmen.android.poetassistant.main.dictionaries.search.Patterns
import ca.rmen.android.poetassistant.main.dictionaries.search.ProcessTextRouter
import ca.rmen.android.poetassistant.widget.PopupMenuHelper
import ca.rmen.android.poetassistant.wotd.Wotd
import ca.rmen.android.poetassistant.wotd.WotdAlarm
import ca.rmen.android.poetassistant.wotd.WotdJob
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class TestUtilityClasses {

    @Test
    @Throws(Exception::class)
    fun testHtmlCompat() {
        assertUtilityClassVisibility(HtmlCompat::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testVectorCompat() {
        assertUtilityClassVisibility(VectorCompat::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testAppBarLayoutHelper() {
        assertUtilityClassVisibility(AppBarLayoutHelper::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testTextPopupMenu() {
        assertUtilityClassVisibility(TextPopupMenu::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testResultListFactory() {
        assertUtilityClassVisibility(ResultListFactory::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testShare() {
        assertUtilityClassVisibility(Share::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testRTUtils() {
        assertUtilityClassVisibility(ca.rmen.android.poetassistant.main.dictionaries.rt.RTUtils::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testPatterns() {
        assertUtilityClassVisibility(Patterns::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testProcessTextRouter() {
        assertUtilityClassVisibility(ProcessTextRouter::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testPopupMenuHelper() {
        assertUtilityClassVisibility(PopupMenuHelper::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testWotd() {
        assertUtilityClassVisibility(Wotd::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testWotdAlarm() {
        assertUtilityClassVisibility(WotdAlarm::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testWotdJob() {
        assertUtilityClassVisibility(WotdJob::class.java)
    }

    @Throws(Exception::class)
    private fun assertUtilityClassVisibility(clazz: Class<*>) {
        val constructor = clazz.getDeclaredConstructor()
        assertTrue(Modifier.isFinal(clazz.modifiers))
        assertTrue(Modifier.isPrivate(constructor.modifiers))
        // For code coverage we have to invoke the constructor:
        // http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
        constructor.isAccessible = true
        assertNotNull(constructor.newInstance())
    }
}
