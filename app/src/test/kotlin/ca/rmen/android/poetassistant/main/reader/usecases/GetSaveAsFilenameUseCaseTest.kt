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
package ca.rmen.android.poetassistant.main.reader.usecases

import android.net.Uri
import ca.rmen.android.poetassistant.main.reader.data.Poem
import ca.rmen.android.poetassistant.main.reader.data.PoemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests the suggested save-as file name rule: the saved poem's name
 * when one exists, else a name generated from the poem text.
 */
@RunWith(RobolectricTestRunner::class)
class GetSaveAsFilenameUseCaseTest {

    @Test
    fun testSavedPoemName() {
        val useCase = GetSaveAsFilenameUseCase(
            fakePoemRepository(Poem.PersistedPoem(Uri.parse("content://poem"), "my-poem.txt", "text")),
        )
        assertEquals("my-poem.txt", useCase.invoke("Roses are red"))
    }

    @Test
    fun testTempPoemUsesGeneratedName() {
        val useCase = GetSaveAsFilenameUseCase(fakePoemRepository(Poem.TempPoem("Roses are red")))
        assertEquals("Roses-are-red.txt", useCase.invoke("Roses are red,\nviolets are blue"))
    }

    @Test
    fun testNoPoemUsesGeneratedName() {
        val useCase = GetSaveAsFilenameUseCase(fakePoemRepository(null))
        assertEquals("Unthrifty.txt", useCase.invoke("Unthrifty loveliness, why dost thou spend"))
        assertEquals("Against-my-love.txt", useCase.invoke("Against my love shall be as I am now,"))
        assertEquals("As-a-decrepit.txt", useCase.invoke("As a decrepit father takes delight"))
        assertEquals("Canst-thou-O.txt", useCase.invoke("Canst thou, O cruel! say I love thee not,"))
        assertEquals("Farewell-thou.txt", useCase.invoke("Farewell! thou art too dear for my possessing,"))
        assertEquals("Lo-in-the.txt", useCase.invoke("Lo! in the orient when the gracious light"))
        assertEquals("Roses-are-red.txt", useCase.invoke("Roses are red,\nviolets are blue"))
        assertEquals("Róses-àré-réd.txt", useCase.invoke("Róses àré réd,\nvïólèts áré blüë"))
        assertEquals("Short.txt", useCase.invoke("Short"))
        assertEquals("abcdefgh.txt", useCase.invoke("abcdefgh"))
        assertEquals("abcdefghi.txt", useCase.invoke("abcdefghi"))
        assertEquals("Short-poem.txt", useCase.invoke("Short poem"))
        assertEquals("leading-symbols.txt", useCase.invoke(",! leading symbols"))
    }

    @Test
    fun testNoSuggestionForEmptyOrSymbolText() {
        val useCase = GetSaveAsFilenameUseCase(fakePoemRepository(null))
        assertNull(useCase.invoke(""))
        assertNull(useCase.invoke("& 2 !,$*-)°"))
    }

    private fun fakePoemRepository(poem: Poem?): PoemRepository = object : PoemRepository {
        override val poem: StateFlow<Poem?> = MutableStateFlow(poem)
        override fun setPoem(poem: Poem) = Unit
        override fun updatePoemText(text: String) = Unit
        override fun clear() = Unit
    }
}
