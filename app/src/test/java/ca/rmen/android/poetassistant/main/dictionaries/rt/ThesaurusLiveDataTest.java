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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ThesaurusLiveDataTest {


    @Test
    public void testHogwashSynonymsWhichRhymeWithCot() {
        Set<String> rhymes = new HashSet<>();
        rhymes.addAll(Arrays.asList(
                "allot", "baht", "blot", "clot", "dot", "hot", "jot", "khat", "knot", "lat", "lot", "lott", "lotte", "montserrat", "mott", "motte", "not", "plot", "polyglot", "pot", "rot", "sadat", "scot", "scott", "shot", "slot", "spot", "squat", "swat", "tot", "trot", "watt", "yacht"
        ));
        List<ThesaurusEntry.ThesaurusEntryDetails> thesaurusEntryDetails = Collections.singletonList(
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN,
                        Arrays.asList("garbage", "buncombe", "drivel", "bunk", "rot", "guff", "bunkim"), Collections.emptyList()));

        List<ThesaurusEntry.ThesaurusEntryDetails> actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertNotNull(actual.get(0).antonyms);
        assertEquals(0, actual.get(0).antonyms.size());
        assertNotNull(actual.get(0).synonyms);
        assertEquals(1, actual.get(0).synonyms.size());
        assertEquals("rot", actual.get(0).synonyms.get(0));
    }

    @Test
    public void testPlayerSynonymsWhichRhymeWithDormer() {
        Set<String> rhymes = new HashSet<>();
        rhymes.addAll(Arrays.asList(
                "former", "informer", "outperformer", "performer", "reformer", "transformer", "warmer"
        ));
        List<ThesaurusEntry.ThesaurusEntryDetails> thesaurusEntryDetails = Arrays.asList(
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, Arrays.asList("contestant", "participant"), Collections.emptyList()),
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, Arrays.asList("musician", "instrumentalist", "performing artist", "performer"), Collections.emptyList()));

        List<ThesaurusEntry.ThesaurusEntryDetails> actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertNotNull(actual.get(0).antonyms);
        assertEquals(0, actual.get(0).antonyms.size());
        assertNotNull(actual.get(0).synonyms);
        assertEquals(1, actual.get(0).synonyms.size());
        assertEquals("performer", actual.get(0).synonyms.get(0));
    }

    @Test
    public void testLaughAntonymsWhichRhymeWithDry() {
        Set<String> rhymes = new HashSet<>();
        rhymes.addAll(Arrays.asList(
                "buy", "bye", "cai", "chi", "comply", "cry", "csi", "dai", "decry", "defy"
        ));
        List<ThesaurusEntry.ThesaurusEntryDetails> thesaurusEntryDetails = Arrays.asList(
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN, Arrays.asList("vocalization", "utterance", "laughter"), Collections.emptyList()),
                new ThesaurusEntry.ThesaurusEntryDetails(ThesaurusEntry.WordType.NOUN,
                        Arrays.asList("express emotion", "laugh off", "express feelings", "express joy", "express mirth", "laugh at", "laugh away"),
                        Collections.singletonList("cry")));

        List<ThesaurusEntry.ThesaurusEntryDetails> actual = ThesaurusLiveData.filter(thesaurusEntryDetails, rhymes);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertNotNull(actual.get(0).antonyms);
        assertEquals(1, actual.get(0).antonyms.size());
        assertEquals("cry", actual.get(0).antonyms.get(0));
        assertNotNull(actual.get(0).synonyms);
        assertEquals(0, actual.get(0).synonyms.size());
    }
}
