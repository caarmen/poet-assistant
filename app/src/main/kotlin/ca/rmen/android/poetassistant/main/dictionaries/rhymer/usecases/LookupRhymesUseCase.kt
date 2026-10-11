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

package ca.rmen.android.poetassistant.main.dictionaries.rhymer.usecases

import android.app.Application
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordSimilarityCalculator
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymeResult
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymesRepository
import ca.rmen.android.poetassistant.main.dictionaries.rhymer.RhymerListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.ThesaurusRepository
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository
import java.util.TreeSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for looking up the rhymes of a word.
 *
 * The lookup pipeline:
 * 1. Rhyme lookup: one result per pronunciation variant. Null means the
 *    word has no pronunciation variants — an unknown word. There is no
 *    closest-word fallback for the rhymes (unlike the dictionary and
 *    thesaurus tabs).
 * 2. Synonym filter (when set): the filter word is resolved with the
 *    closest-word fallback (legacy parity: a near-miss filter word such
 *    as "happyness" resolves to "happiness"), then the results keep only
 *    the words which are synonyms of the resolved filter word. An empty
 *    synonym set is a not-found result.
 * 3. The words with definitions (when all-rhymes is on): computed once
 *    over the union of all displayed words.
 * 4. The list items: the favorites section first (when favorites match
 *    the rhymes), then per variant the strict and syllable-match sections.
 *
 * The lookup runs once; the result flow re-emits when the favorites
 * change, rebuilding the full item list from the cached results without
 * a new lookup.
 *
 * @param rhymesRepository The rhymes repository.
 * @param thesaurusRepository The thesaurus repository, for the synonym
 * filter.
 * @param favoritesRepository The favorites repository, for the favorites
 * section and the favorite flags.
 * @param application The application for accessing string resources.
 */
class LookupRhymesUseCase @Inject constructor(
    private val rhymesRepository: RhymesRepository,
    private val thesaurusRepository: ThesaurusRepository,
    private val favoritesRepository: FavoritesRepository,
    private val application: Application,
) {

    /**
     * The settings which affect the rhyme lookup.
     *
     * @param isThesaurusReverseLookupEnabled Whether the synonym filter
     * includes reverse-lookup synonyms.
     * @param isAllRhymesEnabled Whether to include words without
     * definitions in the rhymes.
     * @param isAOAAMatchEnabled Whether to apply the AO->AA phonetic
     * substitution in the rhymes.
     * @param isAORAOMatchEnabled Whether to apply the AOR->AO phonetic
     * substitution in the rhymes.
     */
    data class RhymerLookupSettings(
        val isThesaurusReverseLookupEnabled: Boolean,
        val isAllRhymesEnabled: Boolean,
        val isAOAAMatchEnabled: Boolean,
        val isAORAOMatchEnabled: Boolean,
    )

    /**
     * One lookup result.
     *
     * @param word The query — the rhymer has no closest-word fallback.
     * @param isFavorite Whether the word is a favorite.
     * @param items The result list items, empty when nothing was found.
     */
    data class RhymerLookupResult(
        val word: String,
        val isFavorite: Boolean,
        val items: List<RhymerListItem>,
    )

    // Pure utility class with no dependencies to mock.
    private val wordSimilarityCalculator = WordSimilarityCalculator()

    suspend operator fun invoke(
        word: String,
        synonymFilter: String?,
        lookupSettings: RhymerLookupSettings,
    ): Flow<RhymerLookupResult> {

        // 1. Rhyme lookup. Null: the word has no pronunciation variants.
        var rhymeResults = rhymesRepository.getRhymingWords(
            word,
            lookupSettings.isAllRhymesEnabled,
            lookupSettings.isAOAAMatchEnabled,
            lookupSettings.isAORAOMatchEnabled,
        ) ?: return notFoundResult(word)

        // 2. Synonym filter: keep only the words which are synonyms of
        // the filter word. The filter word is resolved with the
        // closest-word fallback first.
        if (!synonymFilter.isNullOrBlank()) {
            val resolvedFilter = resolveFilterWord(synonymFilter)
            val synonyms = thesaurusRepository.getFlatSynonyms(
                resolvedFilter,
                lookupSettings.isThesaurusReverseLookupEnabled,
            )
            if (synonyms.isEmpty()) return notFoundResult(word)
            rhymeResults = filterRhymes(rhymeResults, synonyms.toSet())
        }

        // 3. The words with definitions, computed once over the union of
        // all displayed words. With all-rhymes off, every displayed word
        // has a definition.
        val wordsWithDefinitions = if (lookupSettings.isAllRhymesEnabled) {
            rhymesRepository.getWordsWithDefinitions(rhymeResults.flatMap(::allRhymesOf))
        } else {
            null
        }

        // 4. The favorites emissions rebuild the full item list from the
        // cached results, without a new lookup.
        return favoritesRepository.getFavoritesFlow().map { favorites ->
            val favoriteWords = favorites.map { it.getWord() }.toSet()
            RhymerLookupResult(
                word = word,
                isFavorite = favoriteWords.contains(word),
                items = buildItems(word, rhymeResults, favoriteWords, wordsWithDefinitions),
            )
        }
    }

    /**
     * A not-found result: the query with no items. The favorite flag of
     * the query word is still observed by the UI.
     */
    private suspend fun notFoundResult(word: String): Flow<RhymerLookupResult> =
        favoritesRepository.getFavoritesFlow().map { favorites ->
            val favoriteWords = favorites.map { it.getWord() }.toSet()
            RhymerLookupResult(
                word = word,
                isFavorite = favoriteWords.contains(word),
                items = emptyList(),
            )
        }

    /**
     * Resolves the filter word with the closest-word fallback: when the
     * thesaurus table has no row for the filter word, the stem candidates
     * provide the best match, which becomes the lookup word.
     */
    private suspend fun resolveFilterWord(filter: String): String {
        var lookupWord = filter
        val entry = thesaurusRepository.lookupForward(filter)
        if (entry == null) {
            val stem = wordSimilarityCalculator.stem(filter)
            val candidates = thesaurusRepository.getWordsByStem(stem)
            val bestMatch = wordSimilarityCalculator.findBestMatch(filter, candidates)
            if (bestMatch != null) {
                lookupWord = bestMatch
            }
        }
        return lookupWord
    }

    /**
     * Keeps only the rhymes present in the synonym set, dropping the
     * variants whose four rhyme lists all become empty.
     */
    private fun filterRhymes(rhymeResults: List<RhymeResult>, synonyms: Set<String>): List<RhymeResult> =
        rhymeResults.mapNotNull { rhymeResult ->
            val filtered = RhymeResult(
                variantNumber = rhymeResult.variantNumber,
                strictRhymes = rhymeResult.strictRhymes.filter(synonyms::contains),
                oneSyllableRhymes = rhymeResult.oneSyllableRhymes.filter(synonyms::contains),
                twoSyllableRhymes = rhymeResult.twoSyllableRhymes.filter(synonyms::contains),
                threeSyllableRhymes = rhymeResult.threeSyllableRhymes.filter(synonyms::contains),
            )
            if (isEmpty(filtered)) null else filtered
        }

    private fun isEmpty(rhymeResult: RhymeResult): Boolean =
        rhymeResult.strictRhymes.isEmpty()
            && rhymeResult.oneSyllableRhymes.isEmpty()
            && rhymeResult.twoSyllableRhymes.isEmpty()
            && rhymeResult.threeSyllableRhymes.isEmpty()

    private fun allRhymesOf(rhymeResult: RhymeResult): List<String> =
        rhymeResult.strictRhymes +
            rhymeResult.oneSyllableRhymes +
            rhymeResult.twoSyllableRhymes +
            rhymeResult.threeSyllableRhymes

    /**
     * Builds the result list items: the favorites section first (when
     * favorites match the rhymes), then per variant the variant heading
     * (when several variants) and the strict and syllable-match sections,
     * in the order strict, 3, 2, 1 syllables.
     */
    private fun buildItems(
        word: String,
        rhymeResults: List<RhymeResult>,
        favoriteWords: Set<String>,
        wordsWithDefinitions: Set<String>?,
    ): List<RhymerListItem> {
        val items = mutableListOf<RhymerListItem>()
        val matchingFavorites = matchingFavorites(rhymeResults, favoriteWords)
        if (favoriteWords.isNotEmpty() && matchingFavorites.isNotEmpty()) {
            addResultSection(items, R.string.rhyme_section_favorites, matchingFavorites, favoriteWords, wordsWithDefinitions)
        }
        rhymeResults.forEach { rhymeResult ->
            // Add the variant heading only when there are several variants.
            if (rhymeResults.size > 1) {
                items.add(RhymerListItem.VariantHeading("$word (${rhymeResult.variantNumber + 1})"))
            }
            addResultSection(items, R.string.rhyme_section_stress_syllables, rhymeResult.strictRhymes, favoriteWords, wordsWithDefinitions)
            addResultSection(items, R.string.rhyme_section_three_syllables, rhymeResult.threeSyllableRhymes, favoriteWords, wordsWithDefinitions)
            addResultSection(items, R.string.rhyme_section_two_syllables, rhymeResult.twoSyllableRhymes, favoriteWords, wordsWithDefinitions)
            addResultSection(items, R.string.rhyme_section_one_syllable, rhymeResult.oneSyllableRhymes, favoriteWords, wordsWithDefinitions)
        }
        return items
    }

    /**
     * The favorites which appear among the rhymes, sorted.
     */
    private fun matchingFavorites(rhymeResults: List<RhymeResult>, favoriteWords: Set<String>): List<String> {
        val matchingFavorites = TreeSet<String>()
        rhymeResults.forEach { rhymeResult ->
            allRhymesOf(rhymeResult).forEach { rhyme ->
                if (favoriteWords.contains(rhyme)) matchingFavorites.add(rhyme)
            }
        }
        return matchingFavorites.toList()
    }

    /**
     * Adds a section: its heading, its word rows, and the max-results
     * notice once the list has reached the limit. Legacy parity: after
     * the threshold is passed, every non-empty section appends the notice.
     */
    private fun addResultSection(
        items: MutableList<RhymerListItem>,
        sectionHeadingResId: Int,
        rhymes: List<String>,
        favoriteWords: Set<String>,
        wordsWithDefinitions: Set<String>?,
    ) {
        if (rhymes.isNotEmpty()) {
            items.add(RhymerListItem.SectionHeading(application.getString(sectionHeadingResId)))
            rhymes.forEach { rhyme ->
                items.add(
                    RhymerListItem.Word(
                        word = rhyme,
                        isFavorite = favoriteWords.contains(rhyme),
                        hasDefinition = wordsWithDefinitions == null || wordsWithDefinitions.contains(rhyme),
                    )
                )
            }
            if (items.size >= Constants.MAX_RESULTS) {
                items.add(
                    RhymerListItem.MaxResultsNotice(
                        application.getString(R.string.max_results, Constants.MAX_RESULTS)
                    )
                )
            }
        }
    }
}
