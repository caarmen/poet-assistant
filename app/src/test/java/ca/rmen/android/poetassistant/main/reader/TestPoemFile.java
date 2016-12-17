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

package ca.rmen.android.poetassistant.main.reader;

import org.junit.Assert;
import org.junit.Test;

public class TestPoemFile {

    @Test
    public void testGenerateFileName() {
        Assert.assertNull(PoemFile.generateFileName(""));
        Assert.assertNull(PoemFile.generateFileName("& 2 !,$*-)°"));
        Assert.assertEquals("Unthrifty.txt", PoemFile.generateFileName("Unthrifty loveliness, why dost thou spend"));
        Assert.assertEquals("Against-my-love.txt", PoemFile.generateFileName("Against my love shall be as I am now,"));
        Assert.assertEquals("As-a-decrepit.txt", PoemFile.generateFileName("As a decrepit father takes delight"));
        Assert.assertEquals("Canst-thou-O.txt", PoemFile.generateFileName("Canst thou, O cruel! say I love thee not,"));
        Assert.assertEquals("Farewell-thou.txt", PoemFile.generateFileName("Farewell! thou art too dear for my possessing,"));
        Assert.assertEquals("Lo-in-the.txt", PoemFile.generateFileName("Lo! in the orient when the gracious light"));
        Assert.assertEquals("Roses-are-red.txt", PoemFile.generateFileName("Roses are red,\nviolets are blue"));
        Assert.assertEquals("Róses-àré-réd.txt", PoemFile.generateFileName("Róses àré réd,\nvïólèts áré blüë"));
        Assert.assertEquals("Short.txt", PoemFile.generateFileName("Short"));
        Assert.assertEquals("abcdefgh.txt", PoemFile.generateFileName("abcdefgh"));
        Assert.assertEquals("abcdefghi.txt", PoemFile.generateFileName("abcdefghi"));
        Assert.assertEquals("Short-poem.txt", PoemFile.generateFileName("Short poem"));
        Assert.assertEquals("Short-poem.txt", PoemFile.generateFileName("Short poem"));
        Assert.assertEquals("leading-symbols.txt", PoemFile.generateFileName(",! leading symbols"));
    }

}
