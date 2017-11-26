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

package ca.rmen.android.poetassistant;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TestTts {
    @Test
    public void testSplit1() {
        testSplit("To be or not to be",
                "To be or not to be");
    }

    @Test
    public void testSplit2() {
        testSplit("To be or not to be.. that is the question",
                "To be or not to be. that is the question");
    }

    @Test
    public void testSplit3() {
        testSplit("To be or not to be... that is the question",
                "To be or not to be",
                "",
                " that is the question");
    }

    @Test
    public void testSplit4() {
        testSplit("To be or not to be.... that is the question",
                "To be or not to be",
                "",
                "",
                " that is the question");
    }

    @Test
    public void testSplit5() {
        testSplit("To be or not to be..... that is the question",
                "To be or not to be",
                "",
                "",
                "",
                " that is the question");
    }

    @Test
    public void testSplit6() {
        testSplit("To be or not to be...... that is the question",
                "To be or not to be",
                "",
                "",
                "",
                "",
                " that is the question");
    }

    @Test
    public void testSplit7() {
        testSplit("To be  ... or not to be... that is the question",
                "To be  ",
                "",
                " or not to be",
                "",
                " that is the question");
    }

    @Test
    public void testSplit8() {
        testSplit("To be or not to be. That is the question",
                "To be or not to be. That is the question");
    }

    @Test
    public void testSplit9() {
        testSplit("To be or not to be. That. is. the. question",
                "To be or not to be. That. is. the. question");
    }

    @Test
    public void testSplit10() {
        testSplit("To be or not to be.. That.. is.. the.. question",
                "To be or not to be. That. is. the. question");
    }

    @Test
    public void testSplit11() {
        testSplit("To be or not to be.\nThat..\nis.\n the\nquestion",
                "To be or not to be.\nThat.\nis.\n the\nquestion");
    }

    @Test
    public void testSplitDotsOnly1() {
        testSplit(".");
    }

    @Test
    public void testSplitDotsOnly2() {
        testSplit("..");
    }

    @Test
    public void testSplitDotsOnly3() {
        testSplit("...", "");
    }

    @Test
    public void testSplitDotsOnly4() {
        testSplit("....", "", "");
    }

    @Test
    public void testEmpty() {
        testSplit("");
    }

    private void testSplit(String input, String... expectedTokens) {
        List<String> tokens = Tts.Companion.split(input);
        assertEquals(Arrays.asList(expectedTokens), tokens);
    }
}
