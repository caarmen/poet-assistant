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

package ca.rmen.android.poetassistant.reader;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import ca.rmen.android.poetassistant.main.reader.WordCounter;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TestWordCounter {
    @Test
    public void testEmpty() {
        assertEquals(0, WordCounter.INSTANCE.countWords(""));
    }

    @Test
    public void testSimpleSentence() {
        assertEquals(3, WordCounter.INSTANCE.countWords("See spot run."));
    }

    @Test
    public void testApostrophe() {
        assertEquals(3, WordCounter.INSTANCE.countWords("I can't even"));
    }

    @Test
    public void testTaleOfTwoCities() {
        assertEquals(59, WordCounter.INSTANCE.countWords("we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way— in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only."));
    }

    @Test
    public void testHuckleberryFinn() {
        String text = "“Did I give you the letter?”\n" +
                "“What letter?”\n" +
                "“The one I got yesterday out of the post-office.”\n" +
                "“No, you didn’t give me no letter.”\n" +
                "“Well, I must a forgot it.”";
        assertEquals(30, WordCounter.INSTANCE.countWords(text));
    }

    @Test
    public void testWarAndPeace() {
        String text = "The count came waddling in to see his wife with a rather guilty look as usual.\n" +
                "“Well, little countess? What a sauté of game au madère we are to have, my dear! I tasted it. The thousand rubles I paid for Tarás were not ill-spent. He is worth it!”";
        assertEquals(49, WordCounter.INSTANCE.countWords(text));
    }

    @Test
    public void testShakespeare() {
        String text = "Where wasteful Time debateth with decay\n" +
                "To change your day of youth to sullied night,\n" +
                "   And all in war with Time for love of you,\n" +
                "   As he takes from you, I engraft you new.\n";
        assertEquals(34, WordCounter.INSTANCE.countWords(text));
    }

    @Test
    public void testDracula() {
        String text = "4 May.—I found that my landlord had got a letter from the Count";
        assertEquals(14, WordCounter.INSTANCE.countWords(text));
    }

    @Test
    public void testImportanceEarnest() {
        String text = "Algernon.  And, speaking of the science of Life, have you got the cucumber sandwiches cut for Lady Bracknell?\n" +
                "\n" +
                "Lane.  Yes, sir.  [Hands them on a salver.]";
        assertEquals(26, WordCounter.INSTANCE.countWords(text));
    }

    @Test
    public void testDate1() {
        assertEquals(9, WordCounter.INSTANCE.countWords("On the 5th of November, I wrote this test."));
    }

    @Test
    public void testDate2() {
        assertEquals(8, WordCounter.INSTANCE.countWords("On 11/5/2017, I wrote this test."));
    }
}
