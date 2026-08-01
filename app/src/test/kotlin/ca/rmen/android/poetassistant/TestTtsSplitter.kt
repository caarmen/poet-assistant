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

package ca.rmen.android.poetassistant

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TestTtsSplitter {

    @Test
    fun testSplit1() {
        testSplit("To be or not to be",
                "To be or not to be")
    }

    @Test
    fun testSplit2() {
        testSplit("To be or not to be.. that is the question",
                "To be or not to be. that is the question")
    }

    @Test
    fun testSplit3() {
        testSplit("To be or not to be... that is the question",
                "To be or not to be",
                "",
                " that is the question")
    }

    @Test
    fun testSplit4() {
        testSplit("To be or not to be.... that is the question",
                "To be or not to be",
                "",
                "",
                " that is the question")
    }

    @Test
    fun testSplit5() {
        testSplit("To be or not to be..... that is the question",
                "To be or not to be",
                "",
                "",
                "",
                " that is the question")
    }

    @Test
    fun testSplit6() {
        testSplit("To be or not to be...... that is the question",
                "To be or not to be",
                "",
                "",
                "",
                "",
                " that is the question")
    }

    @Test
    fun testSplit7() {
        testSplit("To be  ... or not to be... that is the question",
                "To be  ",
                "",
                " or not to be",
                "",
                " that is the question")
    }

    @Test
    fun testSplit8() {
        testSplit("To be or not to be. That is the question",
                "To be or not to be. That is the question")
    }

    @Test
    fun testSplit9() {
        testSplit("To be or not to be. That. is. the. question",
                "To be or not to be. That. is. the. question")
    }

    @Test
    fun testSplit10() {
        testSplit("To be or not to be.. That.. is.. the.. question",
                "To be or not to be. That. is. the. question")
    }

    @Test
    fun testSplit11() {
        testSplit("To be or not to be.\nThat..\nis.\n the\nquestion",
                "To be or not to be.\nThat.\nis.\n the\nquestion")
    }

    @Test
    fun testSplitDotsOnly1() {
        testSplit(".")
    }

    @Test
    fun testSplitDotsOnly2() {
        testSplit("..")
    }

    @Test
    fun testSplitDotsOnly3() {
        testSplit("...", "")
    }

    @Test
    fun testSplitDotsOnly4() {
        testSplit("....", "", "")
    }

    @Test
    fun testEmpty() {
        testSplit("")
    }

    private fun testSplit(input: String, vararg expectedTokens: String) {
        val tokens = TtsSplitter.split(input)
        assertEquals(expectedTokens.asList(), tokens)
    }
}
