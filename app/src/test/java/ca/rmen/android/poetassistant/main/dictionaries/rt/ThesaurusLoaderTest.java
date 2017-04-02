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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ThesaurusLoaderTest {


    @Test
    public void testHogwashSynonymsWhichRhymeWithCot() {
        Set<String> rhhymes = new HashSet<>();
        rhhymes.addAll(Arrays.asList(
                "allot", "baht", "blot", "clot", "dot", "hot", "jot", "khat", "knot", "lat", "lot", "lott", "lotte", "montserrat", "mott", "motte", "not", "plot", "polyglot", "pot", "rot", "sadat", "scot", "scott", "shot", "slot", "spot", "squat", "swat", "tot", "trot", "watt", "yacht"
        ));
        ThesaurusEntry.ThesaurusEntryDetails[] thesaurusEntryDetails = new ThesaurusEntry.ThesaurusEntryDetails[] {
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN,
                        new String[]{"garbage", "buncombe", "drivel", "bunk", "rot", "guff", "bunkim"}, new String[0])
        };

        ThesaurusEntry.ThesaurusEntryDetails[] actual = ThesaurusLoader.filter(thesaurusEntryDetails, rhhymes);
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertNotNull(actual[0].antonyms);
        assertEquals(0, actual[0].antonyms.length);
        assertNotNull(actual[0].synonyms);
        assertEquals(1, actual[0].synonyms.length);
        assertEquals("rot", actual[0].synonyms[0]);
    }

    @Test
    public void testPlayerSynonymsWhichRhymeWithDormer() {
        Set<String> rhymes = new HashSet<>();
        rhymes.addAll(Arrays.asList(
                "former", "informer", "outperformer", "performer", "reformer", "transformer", "warmer"
        ));
        ThesaurusEntry.ThesaurusEntryDetails[] thesaurusEntryDetails = new ThesaurusEntry.ThesaurusEntryDetails[] {
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, new String[]{"contestant", "participant"}, new String[0]),
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, new String[]{"musician", "instrumentalist", "performing artist", "performer"}, new String[0])
        };

        ThesaurusEntry.ThesaurusEntryDetails[] actual = ThesaurusLoader.filter(thesaurusEntryDetails, rhymes);
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertNotNull(actual[0].antonyms);
        assertEquals(0, actual[0].antonyms.length);
        assertNotNull(actual[0].synonyms);
        assertEquals(1, actual[0].synonyms.length);
        assertEquals("performer", actual[0].synonyms[0]);
    }

    @Test
    public void testLaughAntonymsWhichRhymeWithDry() {
        Set<String> rhymes = new HashSet<>();
        rhymes.addAll(Arrays.asList(
                "buy", "bye", "cai", "chi", "comply", "cry", "csi", "dai", "decry", "defy"
        ));
        ThesaurusEntry.ThesaurusEntryDetails[] thesaurusEntryDetails = new ThesaurusEntry.ThesaurusEntryDetails[] {
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, new String[]{"vocalization", "utterance", "laughter"}, new String[0]),
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN,
                        new String[]{"express emotion", "laugh off", "express feelings", "express joy", "express mirth", "laugh at", "laugh away"},
                        new String[]{"cry"})
        };

        ThesaurusEntry.ThesaurusEntryDetails[] actual = ThesaurusLoader.filter(thesaurusEntryDetails, rhymes);
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertNotNull(actual[0].antonyms);
        assertEquals(1, actual[0].antonyms.length);
        assertEquals("cry", actual[0].antonyms[0]);
        assertNotNull(actual[0].synonyms);
        assertEquals(0, actual[0].synonyms.length);
    }
}
