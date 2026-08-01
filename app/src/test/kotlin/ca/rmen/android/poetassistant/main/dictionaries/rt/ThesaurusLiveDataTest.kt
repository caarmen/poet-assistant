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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusEntry.ThesaurusEntryDetails
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusEntry.WordType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThesaurusLiveDataTest {

    @Test
    fun testHogwashSynonymsWhichRhymeWithCot() {
        val rhymes = setOf(
                "allot", "baht", "blot", "clot", "dot", "hot", "jot", "khat", "knot", "lat", "lot", "lott", "lotte", "montserrat", "mott", "motte", "not", "plot", "polyglot", "pot", "rot", "sadat", "scot", "scott", "shot", "slot", "spot", "squat", "swat", "tot", "trot", "watt", "yacht"
        )
        val thesaurusEntryDetails = listOf(
                ThesaurusEntryDetails(WordType.NOUN,
                        listOf("garbage", "buncombe", "drivel", "bunk", "rot", "guff", "bunkim"), emptyList()))

        val actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes)
        assertNotNull(actual)
        assertEquals(1, actual.size)
        assertNotNull(actual[0].antonyms)
        assertEquals(0, actual[0].antonyms.size)
        assertNotNull(actual[0].synonyms)
        assertEquals(1, actual[0].synonyms.size)
        assertEquals("rot", actual[0].synonyms[0])
    }

    @Test
    fun testPlayerSynonymsWhichRhymeWithDormer() {
        val rhymes = setOf(
                "former", "informer", "outperformer", "performer", "reformer", "transformer", "warmer"
        )
        val thesaurusEntryDetails = listOf(
                ThesaurusEntryDetails(WordType.NOUN, listOf("contestant", "participant"), emptyList()),
                ThesaurusEntryDetails(WordType.NOUN, listOf("musician", "instrumentalist", "performing artist", "performer"), emptyList()))

        val actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes)
        assertNotNull(actual)
        assertEquals(1, actual.size)
        assertNotNull(actual[0].antonyms)
        assertEquals(0, actual[0].antonyms.size)
        assertNotNull(actual[0].synonyms)
        assertEquals(1, actual[0].synonyms.size)
        assertEquals("performer", actual[0].synonyms[0])
    }

    @Test
    fun testLaughAntonymsWhichRhymeWithDry() {
        val rhymes = setOf(
                "buy", "bye", "cai", "chi", "comply", "cry", "csi", "dai", "decry", "defy"
        )
        val thesaurusEntryDetails = listOf(
                ThesaurusEntryDetails(WordType.NOUN, listOf("vocalization", "utterance", "laughter"), emptyList()),
                ThesaurusEntryDetails(WordType.NOUN,
                        listOf("express emotion", "laugh off", "express feelings", "express joy", "express mirth", "laugh at", "laugh away"),
                        listOf("cry")))

        val actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes)
        assertNotNull(actual)
        assertEquals(1, actual.size)
        assertNotNull(actual[0].antonyms)
        assertEquals(1, actual[0].antonyms.size)
        assertEquals("cry", actual[0].antonyms[0])
        assertNotNull(actual[0].synonyms)
        assertEquals(0, actual[0].synonyms.size)
    }
}
