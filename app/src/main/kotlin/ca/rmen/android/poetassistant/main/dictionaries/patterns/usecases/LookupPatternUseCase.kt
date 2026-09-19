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

package ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases

import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternEntry
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternMatcher
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for looking up words matching a pattern.
 * Orchestrates pattern search: fetches matches from repository, fetches favorites,
 * sorts with favorites first then alphabetically, and applies MAX_RESULTS limit.
 *
 * @param dictionaryRepository The dictionary repository for data access.
 * @param favoritesRepository The favorites repository for favorite status.
 * @param patternMatcher The pattern matcher utility for pattern conversion.
 */
class LookupPatternUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val patternMatcher: PatternMatcher,
) {

    /**
     * Result of a pattern lookup operation.
     *
     * @param entries The list of matching pattern entries, sorted with favorites first.
     * @param isCapped Whether the results were capped at Constants.MAX_RESULTS.
     */
    data class Result(
        val entries: Flow<List<PatternEntry>>,
        val isCapped: Boolean
    )

    /**
     * Looks up words matching the given pattern.
     *
     * @param pattern The pattern to search for (user input with ? and * symbols).
     * @return Result containing the sorted list of matching entries and whether
     *         the results were capped at the maximum limit.
     */
    suspend operator fun invoke(pattern: String): Result {
        val sqlitePattern = patternMatcher.convertForSqlite(pattern)
        val allMatches = dictionaryRepository.findByPattern(sqlitePattern)
        val favoritesFlow = favoritesRepository.getFavoritesFlow()
            .map { favorites -> favorites.map { it.getWord() } }

        val entries = favoritesFlow.map { favorites ->
            allMatches.map { word ->
                PatternEntry(word, favorites.contains(word))
            }.sortedWith(
                // Sort favorites first, then alphabetically
                compareBy<PatternEntry> { !it.isFavorite }.thenBy { it.word }
            ).take(Constants.MAX_RESULTS)
        }
        val isCapped = allMatches.size > Constants.MAX_RESULTS
        return Result(entries, isCapped)

    }
}
