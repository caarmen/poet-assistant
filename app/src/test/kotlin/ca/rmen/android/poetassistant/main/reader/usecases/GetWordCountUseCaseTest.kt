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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests the word and character counting rules of GetWordCountUseCase.
 */
class GetWordCountUseCaseTest {

    private val useCase = GetWordCountUseCase()

    @Test
    fun testCountWords() {
        assertEquals(2, useCase.invoke("Hello there")?.words)
        // Hyphens, apostrophes and underscores do not split words.
        assertEquals(3, useCase.invoke("good-hearted don't variable_name")?.words)
    }

    @Test
    fun testCountCharacters() {
        assertEquals(11, useCase.invoke("Hello there")?.characters)
    }

    @Test
    fun testWordCountHiddenForZeroWords() {
        assertNull(useCase.invoke(""))
        assertNull(useCase.invoke(" "))
        assertNull(useCase.invoke("   "))
        assertNull(useCase.invoke(null))
    }
}
