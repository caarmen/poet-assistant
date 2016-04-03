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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

public class DictionaryEntry {
    public final String word;
    public final DictionaryEntryDetails[] details;

    public DictionaryEntry(String word, DictionaryEntryDetails[] details) {
        this.word = word;
        this.details = details;
    }

    public static class DictionaryEntryDetails {
        public final String partOfSpeech;
        public final String definition;

        public DictionaryEntryDetails(String partOfSpeech, String definition) {
            this.partOfSpeech = partOfSpeech;
            this.definition = definition;
        }
    }
}
