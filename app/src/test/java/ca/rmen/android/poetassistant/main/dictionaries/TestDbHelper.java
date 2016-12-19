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


public class TestDbHelper {

    private static final int MAX_ARGS_PER_QUERY = 5;

    @Test
    public void testQueryCount() {
        Assert.assertEquals(1, EmbeddedDb.getQueryCount(1, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(1, EmbeddedDb.getQueryCount(2, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(1, EmbeddedDb.getQueryCount(3, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(1, EmbeddedDb.getQueryCount(4, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(1, EmbeddedDb.getQueryCount(5, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(2, EmbeddedDb.getQueryCount(6, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(2, EmbeddedDb.getQueryCount(7, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(2, EmbeddedDb.getQueryCount(8, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(2, EmbeddedDb.getQueryCount(9, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(2, EmbeddedDb.getQueryCount(10, MAX_ARGS_PER_QUERY));
        Assert.assertEquals(3, EmbeddedDb.getQueryCount(11, MAX_ARGS_PER_QUERY));
    }

    @Test
    public void testArgCountInQuery() {
        Assert.assertEquals(MAX_ARGS_PER_QUERY, EmbeddedDb.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 0));
        Assert.assertEquals(MAX_ARGS_PER_QUERY, EmbeddedDb.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 1));
        Assert.assertEquals(1, EmbeddedDb.getArgCountInQuery(11, MAX_ARGS_PER_QUERY, 2));
    }


    @Test
    public void testArgsInQuery() {
        String[] args = new String[]{"a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j",
                "k"
        };
        Assert.assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, EmbeddedDb.getArgsInQuery(args, 0, MAX_ARGS_PER_QUERY));
        Assert.assertArrayEquals(new String[]{"f", "g", "h", "i", "j"}, EmbeddedDb.getArgsInQuery(args, 1, MAX_ARGS_PER_QUERY));
        Assert.assertArrayEquals(new String[]{"k"}, EmbeddedDb.getArgsInQuery(args, 2, MAX_ARGS_PER_QUERY));
    }
}
