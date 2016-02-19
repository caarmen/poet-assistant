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

package ca.rmen.android.poetassistant.main;

public enum Tab {
    RHYMER, THESAURUS, DICTIONARY, READER;

    public static Tab parse(String value) {
        if (RHYMER.name().equalsIgnoreCase(value)) return RHYMER;
        if (THESAURUS.name().equalsIgnoreCase(value)) return THESAURUS;
        if (DICTIONARY.name().equalsIgnoreCase(value)) return DICTIONARY;
        if (READER.name().equalsIgnoreCase(value)) return READER;
        return null;
    }
}
