/*
 * Copyright (c) 2016-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.shared.main

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusEntry
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.ThesaurusRepository
import ca.rmen.android.poetassistant.main.rules.ActivityTestRules
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class EmbeddedDbThesaurusRepositoryTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var thesaurusRepository: ThesaurusRepository

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityTestRules.beforeActivityLaunched(context)
        hiltTestRule.inject()
    }

    @After
    fun tearDown() {
        ActivityTestRules.afterActivityFinished(context)
    }

    /**
     * The forward synonyms of an entry, to exclude from the reverse
     * lookups.
     */
    private fun forwardSynonyms(entry: ThesaurusEntry): Set<String> =
        entry.entries.flatMap { it.synonyms }.toSet()

    /**
     * The forward antonyms of an entry, to exclude from the reverse
     * lookups.
     */
    private fun forwardAntonyms(entry: ThesaurusEntry): Set<String> =
        entry.entries.flatMap { it.antonyms }.toSet()

    /**
     * Given "mistake",
     * When the forward lookup runs,
     * Then the entry has one entry per word type, with the exact synonyms.
     */
    @Test
    fun testForwardLookupMistake() = runBlocking {
        val entry = thesaurusRepository.lookupForward("mistake")
        assertNotNull(entry)
        val entries = entry!!.entries
        assertEquals(5, entries.size)

        assertEquals(ThesaurusEntry.WordType.NOUN, entries[0].wordType)
        assertEquals(listOf("nonaccomplishment", "nonachievement", "error", "fault"), entries[0].synonyms)
        assertTrue(entries[0].antonyms.isEmpty())

        assertEquals(ThesaurusEntry.WordType.NOUN, entries[1].wordType)
        assertEquals(listOf("misunderstanding", "misapprehension", "misconception"), entries[1].synonyms)
        assertTrue(entries[1].antonyms.isEmpty())

        assertEquals(ThesaurusEntry.WordType.NOUN, entries[2].wordType)
        assertEquals(listOf("misstatement", "error"), entries[2].synonyms)
        assertTrue(entries[2].antonyms.isEmpty())

        assertEquals(ThesaurusEntry.WordType.VERB, entries[3].wordType)
        assertEquals(listOf("misidentify", "identify"), entries[3].synonyms)
        assertTrue(entries[3].antonyms.isEmpty())

        assertEquals(ThesaurusEntry.WordType.VERB, entries[4].wordType)
        assertEquals(listOf("slip up", "err", "slip"), entries[4].synonyms)
        assertTrue(entries[4].antonyms.isEmpty())
    }

    /**
     * Given the forward synonyms of "mistake" as excluded words,
     * When the reverse synonyms lookup runs,
     * Then the entries are merged per word type, and the excluded words
     * are not in the results.
     */
    @Test
    fun testReverseSynonymsLookupMistake() = runBlocking {
        val forwardEntry = thesaurusRepository.lookupForward("mistake")
        assertNotNull(forwardEntry)
        val excludeWords = forwardSynonyms(forwardEntry!!)
        val reverseEntries = thesaurusRepository.lookupReverseSynonyms("mistake", excludeWords)
        assertEquals(2, reverseEntries.size)

        assertEquals(ThesaurusEntry.WordType.NOUN, reverseEntries[0].wordType)
        assertEquals(
            listOf(
                "balls-up", "ballup", "betise", "bloomer", "blooper", "blot", "blunder", "boner",
                "boo-boo", "botch", "bungle", "cockup", "confusion", "corrigendum", "distortion",
                "erratum", "flub", "folly", "foolishness", "foul-up", "fuckup", "imbecility",
                "incursion", "lapse", "literal", "literal error", "mess-up", "miscalculation",
                "miscue", "misestimation", "misprint", "misreckoning", "mix-up", "offside", "omission",
                "oversight", "parapraxis", "pratfall", "renege", "revoke", "skip", "slip-up", "smear",
                "smirch", "spot", "stain", "stupidity", "typo", "typographical error",
            ),
            reverseEntries[0].synonyms,
        )
        assertTrue(reverseEntries[0].antonyms.isEmpty())

        assertEquals(ThesaurusEntry.WordType.VERB, reverseEntries[1].wordType)
        assertEquals(
            listOf("confound", "confuse", "fall for", "misjudge", "misremember", "stumble", "trip up"),
            reverseEntries[1].synonyms,
        )
        assertTrue(reverseEntries[1].antonyms.isEmpty())

        assertTrue(reverseEntries.all { entry -> entry.synonyms.none { it in excludeWords } })
    }

    /**
     * Given "nonattendance",
     * When the forward lookup runs,
     * Then the entry has its synonyms and its antonyms.
     */
    @Test
    fun testForwardLookupNonattendance() = runBlocking {
        val entry = thesaurusRepository.lookupForward("nonattendance")
        assertNotNull(entry)
        val entries = entry!!.entries
        assertEquals(1, entries.size)

        assertEquals(ThesaurusEntry.WordType.NOUN, entries[0].wordType)
        assertEquals(listOf("group action"), entries[0].synonyms)
        assertEquals(listOf("attendance"), entries[0].antonyms)
    }

    /**
     * Given the forward synonyms and antonyms of "nonattendance" as
     * excluded words,
     * When the reverse synonyms and antonyms lookups run,
     * Then the entries are merged per word type, and the excluded words
     * are not in the results.
     */
    @Test
    fun testReverseLookupsNonattendance() = runBlocking {
        val forwardEntry = thesaurusRepository.lookupForward("nonattendance")
        assertNotNull(forwardEntry)
        val excludeSynonyms = forwardSynonyms(forwardEntry!!)
        val excludeAntonyms = forwardAntonyms(forwardEntry)

        val reverseSynonymEntries = thesaurusRepository.lookupReverseSynonyms("nonattendance", excludeSynonyms)
        assertEquals(1, reverseSynonymEntries.size)
        assertEquals(ThesaurusEntry.WordType.NOUN, reverseSynonymEntries[0].wordType)
        assertEquals(listOf("absence", "hooky", "nonappearance", "truancy"), reverseSynonymEntries[0].synonyms)
        assertTrue(reverseSynonymEntries[0].antonyms.isEmpty())

        val reverseAntonymEntries = thesaurusRepository.lookupReverseAntonyms("nonattendance", excludeAntonyms)
        assertEquals(1, reverseAntonymEntries.size)
        assertEquals(ThesaurusEntry.WordType.NOUN, reverseAntonymEntries[0].wordType)
        assertTrue(reverseAntonymEntries[0].synonyms.isEmpty())
        assertEquals(listOf("attending"), reverseAntonymEntries[0].antonyms)

        assertTrue(reverseSynonymEntries.all { entry -> entry.synonyms.none { it in excludeSynonyms } })
        assertTrue(reverseAntonymEntries.all { entry -> entry.antonyms.none { it in excludeAntonyms } })
    }
}
