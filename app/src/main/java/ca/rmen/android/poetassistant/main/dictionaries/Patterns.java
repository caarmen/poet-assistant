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
package ca.rmen.android.poetassistant.main.dictionaries;

public class Patterns {

    public static final int MAX_RESULTS = 500;

    /**
     * USER_PATTERN_SYMBOLS are symbols the user can type for pattern matching.
     * These are mapped 1-to-1 to SQLITE_PATTERN_SYMBOLS which will be used in
     * the 'LIKE' clause.  We provide this mapping because ? and * are usually
     * easier for a user to type, than _ or %.
     */
    private static final String[] USER_PATTERN_SYMBOLS = new String[]{"?", "*"};
    private static final String[] SQLITE_PATTERN_SYMBOLS = new String[]{"_", "%"};

    private Patterns() {
        // prevent instantiation
    }

    /**
     * @return true if the given input contains symbols that can be used with pattern matching
     */
    static boolean isPattern(String input) {
        for (String patternSymbol : USER_PATTERN_SYMBOLS) {
            if (input.contains(patternSymbol)) return true;
        }
        return false;
    }

    /**
     * @return a pattern string that can be used in an SQLite query.
     */
    public static String convertForSqlite(String input) {
        for (int i = 0; i < USER_PATTERN_SYMBOLS.length; i++) {
            input = input.replace(USER_PATTERN_SYMBOLS[i], SQLITE_PATTERN_SYMBOLS[i]);
        }
        return input;
    }
}
