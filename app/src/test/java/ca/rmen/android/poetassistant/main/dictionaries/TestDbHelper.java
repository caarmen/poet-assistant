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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestDbHelper {

    private static final int MAX_ARGS_PER_QUERY = 5;

    @Test
    public void testQueryCount() {
        assertEquals(1, EmbeddedDb.Companion.getQueryCount(1, MAX_ARGS_PER_QUERY));
        assertEquals(1, EmbeddedDb.Companion.getQueryCount(2, MAX_ARGS_PER_QUERY));
        assertEquals(1, EmbeddedDb.Companion.getQueryCount(3, MAX_ARGS_PER_QUERY));
        assertEquals(1, EmbeddedDb.Companion.getQueryCount(4, MAX_ARGS_PER_QUERY));
        assertEquals(1, EmbeddedDb.Companion.getQueryCount(5, MAX_ARGS_PER_QUERY));
        assertEquals(2, EmbeddedDb.Companion.getQueryCount(6, MAX_ARGS_PER_QUERY));
        assertEquals(2, EmbeddedDb.Companion.getQueryCount(7, MAX_ARGS_PER_QUERY));
        assertEquals(2, EmbeddedDb.Companion.getQueryCount(8, MAX_ARGS_PER_QUERY));
        assertEquals(2, EmbeddedDb.Companion.getQueryCount(9, MAX_ARGS_PER_QUERY));
        assertEquals(2, EmbeddedDb.Companion.getQueryCount(10, MAX_ARGS_PER_QUERY));
        assertEquals(3, EmbeddedDb.Companion.getQueryCount(11, MAX_ARGS_PER_QUERY));
    }

    @Test
    public void testArgCountInQuery() {
        assertEquals(MAX_ARGS_PER_QUERY, EmbeddedDb.Companion.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 0));
        assertEquals(MAX_ARGS_PER_QUERY, EmbeddedDb.Companion.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 1));
        assertEquals(1, EmbeddedDb.Companion.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 2));
    }


    @Test
    public void testArgsInQuery() {
        String[] args = new String[]{"a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j",
                "k"
        };
        Assert.assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, EmbeddedDb.Companion.getArgsInQuery(args, 0, MAX_ARGS_PER_QUERY));
        Assert.assertArrayEquals(new String[]{"f", "g", "h", "i", "j"}, EmbeddedDb.Companion.getArgsInQuery(args, 1, MAX_ARGS_PER_QUERY));
        Assert.assertArrayEquals(new String[]{"k"}, EmbeddedDb.Companion.getArgsInQuery(args, 2, MAX_ARGS_PER_QUERY));
    }

    @Test
    public void testBuildInClause() {
        String clause = EmbeddedDb.Companion.buildInClause(5);
        assertEquals("(?,?,?,?,?)", clause);
    }
}
