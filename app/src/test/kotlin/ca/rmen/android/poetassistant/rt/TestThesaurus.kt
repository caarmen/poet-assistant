/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.rt

import ca.rmen.android.poetassistant.main.dictionaries.rt.Thesaurus
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusEntry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Arrays
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class TestThesaurus {

    @get:Rule
    val hiltTestRule = HiltAndroidRule(this)

    @Inject
    lateinit var thesaurus: Thesaurus

    @Before
    fun setup() {
        hiltTestRule.inject()
    }

    @Test
    fun testReverseLookupDisabledMistake() {
        val thesaurusEntry = thesaurus.lookup("mistake", false)
        assertEquals(5, thesaurusEntry.entries.size)
        assertExpectedForwardThesaurusEntryForMistake(thesaurusEntry)
    }

    @Test
    fun testReverseLookupEnabledMistake() {
        val thesaurusEntry = thesaurus.lookup("mistake", true)
        assertEquals(7, thesaurusEntry.entries.size)
        assertExpectedForwardThesaurusEntryForMistake(thesaurusEntry)
        var index = 5
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList(
                "balls-up", "ballup", "betise", "bloomer", "blooper", "blot", "blunder", "boner",
                "boo-boo", "botch", "bungle", "cockup", "confusion", "corrigendum", "distortion",
                "erratum", "flub", "folly", "foolishness", "foul-up", "fuckup", "imbecility",
                "incursion", "lapse", "literal", "literal error", "mess-up", "miscalculation",
                "miscue", "misestimation", "misprint", "misreckoning", "mix-up", "offside", "omission",
                "oversight", "parapraxis", "pratfall", "renege", "revoke", "skip", "slip-up", "smear",
                "smirch", "spot", "stain", "stupidity", "typo", "typographical error"),
                thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.VERB, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("confound", "confuse", "fall for", "misjudge", "misremember",
                "stumble", "trip up"),
                thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())
    }

    @Test
    fun testReverseLookupDisabledNonattendance() {
        val thesaurusEntry = thesaurus.lookup("nonattendance", false)
        assertEquals(1, thesaurusEntry.entries.size)
        assertExpectedForwardThesaurusEntryForNonattendance(thesaurusEntry)

    }

    @Test
    fun testReverseLookupEnabledNonattendance() {
        val thesaurusEntry = thesaurus.lookup("nonattendance", true)
        assertEquals(3, thesaurusEntry.entries.size)
        assertExpectedForwardThesaurusEntryForNonattendance(thesaurusEntry)
        var index = 1
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList( "absence", "hooky", "nonappearance", "truancy"),
                thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertTrue(thesaurusEntry.entries[index].synonyms.isEmpty())
        assertEquals(Arrays.asList( "attending"),
                thesaurusEntry.entries[index].antonyms)

    }
    private fun assertExpectedForwardThesaurusEntryForMistake(thesaurusEntry: ThesaurusEntry) {
        var index = 0
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("nonaccomplishment", "nonachievement", "error", "fault"), thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("misunderstanding", "misapprehension", "misconception"), thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("misstatement", "error"), thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.VERB, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("misidentify", "identify"), thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

        index++
        assertEquals(ThesaurusEntry.WordType.VERB, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("slip up", "err", "slip"), thesaurusEntry.entries[index].synonyms)
        assertTrue(thesaurusEntry.entries[index].antonyms.isEmpty())

    }

    private fun assertExpectedForwardThesaurusEntryForNonattendance(thesaurusEntry: ThesaurusEntry) {
        val index = 0
        assertEquals(ThesaurusEntry.WordType.NOUN, thesaurusEntry.entries[index].wordType)
        assertEquals(Arrays.asList("group action"), thesaurusEntry.entries[index].synonyms)
        assertEquals(Arrays.asList("attendance"), thesaurusEntry.entries[index].antonyms)
    }

}
