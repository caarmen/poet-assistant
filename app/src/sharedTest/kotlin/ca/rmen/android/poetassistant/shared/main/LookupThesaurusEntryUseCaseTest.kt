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

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymesRepository
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusEntry
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.ThesaurusRepository
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.usecases.LookupThesaurusEntryUseCase
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import ca.rmen.android.poetassistant.main.rules.ActivityTestRules
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
class LookupThesaurusEntryUseCaseTest {

    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var thesaurusRepository: ThesaurusRepository

    @Inject
    lateinit var rhymesRepository: RhymesRepository

    @Inject
    lateinit var favoritesRepository: FavoritesRepository

    private lateinit var context: Context

    private lateinit var useCase: LookupThesaurusEntryUseCase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityTestRules.beforeActivityLaunched(context)
        hiltTestRule.inject()
        useCase = LookupThesaurusEntryUseCase(
            thesaurusRepository = thesaurusRepository,
            rhymesRepository = rhymesRepository,
            favoritesRepository = favoritesRepository,
            application = context.applicationContext as Application,
        )
    }

    @After
    fun tearDown() {
        ActivityTestRules.afterActivityFinished(context)
    }

    /**
     * The result words, in list order.
     */
    private fun words(result: LookupThesaurusEntryUseCase.ThesaurusLookupResult): List<String> =
        result.items.filterIsInstance<ThesaurusListItem.Word>().map { it.word }

    private fun lookupSettings(
        isThesaurusReverseLookupEnabled: Boolean = false,
    ) = LookupThesaurusEntryUseCase.ThesaurusLookupSettings(
        isThesaurusReverseLookupEnabled = isThesaurusReverseLookupEnabled,
        isAllRhymesEnabled = false,
        isAOAAMatchEnabled = false,
        isAORAOMatchEnabled = false,
    )

    /**
     * Given the reverse-lookup setting enabled,
     * When "mistake" is looked up,
     * Then "blunder", which lists "mistake" among its synonyms, is in the results.
     */
    @Test
    fun testReverseLookup() = runBlocking {
        val result = useCase("mistake", null, lookupSettings(isThesaurusReverseLookupEnabled = true)).first()
        val words = words(result)
        assertTrue("blunder not in $words", words.contains("blunder"))
    }

    /**
     * Given the reverse-lookup setting disabled,
     * When "mistake" is looked up,
     * Then "blunder" is not in the results.
     */
    @Test
    fun testNoReverseLookup() = runBlocking {
        val result = useCase("mistake", null, lookupSettings()).first()
        val words = words(result)
        assertTrue("blunder should not be in $words", !words.contains("blunder"))
    }

    /**
     * Given a filter,
     * When "cloudy" is looked up,
     * Then only synonyms and antonyms rhyming with the filter word are kept:
     * "muddy" rhymes with "bloody".
     */
    @Test
    fun testFilter() = runBlocking {
        val result = useCase("cloudy", "bloody", lookupSettings()).first()
        val words = words(result)
        assertTrue("muddy not in $words", words.contains("muddy"))
        val rhymes = rhymesRepository.getFlatRhymes("bloody", false, false, false)
        assertTrue(words.all { rhymes.contains(it) })
    }

    /**
     * The three tests below exercise the filter with crafted entries and
     * rhyme sets, through the use case, with fake repositories.
     */

    /**
     * Given synonyms of which only "rot" rhymes with the filter word,
     * When the entry is filtered,
     * Then only "rot" remains in the synonyms.
     */
    @Test
    fun testHogwashSynonymsWhichRhymeWithCot() = runBlocking {
        val rhymes = setOf(
            "allot", "baht", "blot", "clot", "dot", "hot", "jot", "khat", "knot", "lat", "lot", "lott", "lotte", "montserrat", "mott", "motte", "not", "plot", "polyglot", "pot", "rot", "sadat", "scot", "scott", "shot", "slot", "spot", "squat", "swat", "tot", "trot", "watt", "yacht"
        )
        val entry = ThesaurusEntry(
            word = "hogwash",
            entries = listOf(
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("garbage", "buncombe", "drivel", "bunk", "rot", "guff", "bunkim"),
                    emptyList(),
                )
            ),
        )
        val useCase = useCaseWith(entry, rhymes)
        val result = useCase("hogwash", "cot", lookupSettings()).first()
        assertEquals("hogwash", result.word)
        assertEquals(listOf("rot"), words(result))
    }

    /**
     * Given two entries of which the first has no word rhyming with the
     * filter word,
     * When the entries are filtered,
     * Then the first entry is dropped, and the second keeps only its
     * rhyming word.
     */
    @Test
    fun testPlayerSynonymsWhichRhymeWithDormer() = runBlocking {
        val rhymes = setOf(
            "former", "informer", "outperformer", "performer", "reformer", "transformer", "warmer"
        )
        val entry = ThesaurusEntry(
            word = "player",
            entries = listOf(
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("contestant", "participant"),
                    emptyList(),
                ),
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("musician", "instrumentalist", "performing artist", "performer"),
                    emptyList(),
                ),
            ),
        )
        val useCase = useCaseWith(entry, rhymes)
        val result = useCase("player", "dormer", lookupSettings()).first()
        assertEquals("player", result.word)
        assertEquals(listOf("performer"), words(result))
    }

    /**
     * Given an entry whose synonyms do not rhyme with the filter word but
     * whose antonym "cry" does,
     * When the entry is filtered,
     * Then the entry is kept, with only its antonym.
     */
    @Test
    fun testLaughAntonymsWhichRhymeWithDry() = runBlocking {
        val rhymes = setOf(
            "buy", "bye", "cai", "chi", "comply", "cry", "csi", "dai", "decry", "defy"
        )
        val entry = ThesaurusEntry(
            word = "laugh",
            entries = listOf(
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("vocalization", "utterance", "laughter"),
                    emptyList(),
                ),
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("express emotion", "laugh off", "express feelings", "express joy", "express mirth", "laugh at", "laugh away"),
                    listOf("cry"),
                ),
            ),
        )
        val useCase = useCaseWith(entry, rhymes)
        val result = useCase("laugh", "dry", lookupSettings()).first()
        assertEquals("laugh", result.word)
        assertEquals(listOf("cry"), words(result))
        // The kept antonym is in the Antonyms section.
        assertTrue(
            result.items.any { it is ThesaurusListItem.Subheading && it.text == context.getString(R.string.thesaurus_section_antonyms) }
        )
    }

    /**
     * A use case over fake repositories, for testing with crafted entries
     * and rhyme sets.
     */
    private fun useCaseWith(
        entry: ThesaurusEntry,
        rhymes: Set<String>,
        stemCandidates: List<String> = emptyList(),
    ): LookupThesaurusEntryUseCase =
        LookupThesaurusEntryUseCase(
            thesaurusRepository = FakeThesaurusRepository(entry, stemCandidates),
            rhymesRepository = FakeRhymesRepository(rhymes),
            favoritesRepository = favoritesRepository,
            application = context.applicationContext as Application,
        )

    /**
     * Given a query with no thesaurus entry, and stem candidates,
     * When the query is looked up,
     * Then the lookup falls back to the best-matching candidate:
     * "runner" scores higher than "run" and "rubber" against "runny".
     */
    @Test
    fun testClosestWordFallback() = runBlocking {
        val entry = ThesaurusEntry(
            word = "runner",
            entries = listOf(
                ThesaurusEntry.ThesaurusEntryDetails(
                    ThesaurusEntry.WordType.NOUN,
                    listOf("jogger", "sprinter"),
                    emptyList(),
                ),
            ),
        )
        val useCase = useCaseWith(
            entry,
            rhymes = emptySet(),
            stemCandidates = listOf("run", "rubber", "runner"),
        )
        val result = useCase("runny", null, lookupSettings()).first()
        assertEquals("runner", result.word)
        assertEquals(listOf("jogger", "sprinter"), words(result))
    }

    private class FakeThesaurusRepository(
        private val entry: ThesaurusEntry,
        private val stemCandidates: List<String>,
    ) : ThesaurusRepository {
        override suspend fun lookupForward(word: String): ThesaurusEntry? = entry.takeIf { it.word == word }
        override suspend fun lookupReverseSynonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails> = emptyList()
        override suspend fun lookupReverseAntonyms(word: String, excludeWords: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails> = emptyList()
        override suspend fun getWordsByStem(stem: String): List<String> = stemCandidates
    }

    private class FakeRhymesRepository(private val rhymes: Set<String>) : RhymesRepository {
        override suspend fun getFlatRhymes(
            word: String,
            allRhymesEnabled: Boolean,
            aoToAaMatchEnabled: Boolean,
            aorToAoMatchEnabled: Boolean,
        ): Set<String> = rhymes
    }
}
