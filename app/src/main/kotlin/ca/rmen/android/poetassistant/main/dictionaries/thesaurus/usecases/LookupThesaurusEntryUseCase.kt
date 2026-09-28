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

package ca.rmen.android.poetassistant.main.dictionaries.thesaurus.usecases

import android.app.Application
import androidx.annotation.StringRes
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordSimilarityCalculator
import ca.rmen.android.poetassistant.main.dictionaries.rhymes.RhymesRepository
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusEntry
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.data.ThesaurusRepository
import ca.rmen.android.poetassistant.main.favorites.data.FavoritesRepository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for looking up a word in the thesaurus.
 *
 * The lookup pipeline:
 * 1. Forward lookup. When the word has no thesaurus row, run the
 *    closest-word fallback on the stem candidates and re-run the forward
 *    lookup on the best match.
 * 2. Reverse lookup (when enabled): entries of words listing the lookup
 *    word among their synonyms or antonyms, excluding the forward results.
 * 3. When the final entries list is empty, the result word is the original
 *    query — the UI shows the query for the header and the not-found
 *    message.
 * 4. When a filter is set, keep only the synonyms and antonyms which rhyme
 *    with the filter word.
 * 5. The list items: per word type a heading, then a synonyms section and
 *    an antonyms section, each with its result words.
 *
 * The lookup runs once; the result flow re-emits when the favorites change,
 * re-decorating the favorite flags without a new lookup.
 *
 * @param thesaurusRepository The thesaurus repository.
 * @param rhymesRepository The rhymes repository, for the filter.
 * @param favoritesRepository The favorites repository, for the favorite flags.
 * @param application The application for accessing string resources.
 */
class LookupThesaurusEntryUseCase @Inject constructor(
    private val thesaurusRepository: ThesaurusRepository,
    private val rhymesRepository: RhymesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val application: Application,
) {

    /**
     * The settings which affect the thesaurus lookup.
     *
     * @param isThesaurusReverseLookupEnabled Whether to include entries of
     * words listing the lookup word among their synonyms or antonyms.
     * @param isAllRhymesEnabled Whether to include words without definitions
     * in the filter rhyme set.
     * @param isAOAAMatchEnabled Whether to apply the AO->AA phonetic
     * substitution in the filter rhyme set.
     * @param isAORAOMatchEnabled Whether to apply the AOR->AO phonetic
     * substitution in the filter rhyme set.
     */
    data class ThesaurusLookupSettings(
        val isThesaurusReverseLookupEnabled: Boolean,
        val isAllRhymesEnabled: Boolean,
        val isAOAAMatchEnabled: Boolean,
        val isAORAOMatchEnabled: Boolean,
    )

    // Pure utility class with no dependencies to mock.
    private val wordSimilarityCalculator = WordSimilarityCalculator()

    /**
     * One lookup result.
     *
     * @param word The matched word, or the query when nothing was found.
     * @param isFavorite Whether the word is a favorite.
     * @param items The result list items, empty when nothing was found.
     */
    data class ThesaurusLookupResult(
        val word: String,
        val isFavorite: Boolean,
        val items: List<ThesaurusListItem>,
    )

    suspend operator fun invoke(
        word: String,
        filter: String?,
        lookupSettings: ThesaurusLookupSettings,
    ): Flow<ThesaurusLookupResult> {

        // 1. Forward lookup and closest-word fallback.
        var lookupWord = word
        var entry = thesaurusRepository.lookupForward(word)
        if (entry == null) {
            val stem = wordSimilarityCalculator.stem(word)
            val candidates = thesaurusRepository.getWordsByStem(stem)
            val bestMatch = wordSimilarityCalculator.findBestMatch(word, candidates)
            if (bestMatch != null) {
                lookupWord = bestMatch
                entry = thesaurusRepository.lookupForward(lookupWord)
            }
        }

        // 2. Reverse lookup on the (possibly fallback-resolved) lookup word,
        // excluding the forward results.
        val forwardEntries = entry?.entries ?: emptyList()
        val entries = forwardEntries.toMutableList()
        if (lookupSettings.isThesaurusReverseLookupEnabled) {
            val forwardSynonyms = forwardEntries.flatMap { it.synonyms }.toSet()
            val forwardAntonyms = forwardEntries.flatMap { it.antonyms }.toSet()
            entries.addAll(thesaurusRepository.lookupReverseSynonyms(lookupWord, forwardSynonyms))
            entries.addAll(thesaurusRepository.lookupReverseAntonyms(lookupWord, forwardAntonyms))
        }

        // 3. Nothing found: the result word is the original query.
        val lookupEntry = if (entries.isEmpty()) ThesaurusEntry(word, emptyList())
        // 4. Filter: keep only words rhyming with the filter word.
        else if (!filter.isNullOrBlank()) {
            val rhymes = rhymesRepository.getFlatRhymes(
                filter,
                lookupSettings.isAllRhymesEnabled,
                lookupSettings.isAOAAMatchEnabled,
                lookupSettings.isAORAOMatchEnabled,
            )
            ThesaurusEntry(lookupWord, filterEntries(entries, rhymes))
        } else ThesaurusEntry(lookupWord, entries)

        // 5. Build the list items, decorated with the favorite flags.
        // Re-emits when the favorites change, without a new lookup.
        return favoritesRepository.getFavoritesFlow().map { favorites ->
            val favoriteWords = favorites.map { it.getWord() }.toSet()
            ThesaurusLookupResult(
                word = lookupEntry.word,
                isFavorite = favoriteWords.contains(lookupEntry.word),
                items = buildItems(lookupEntry, favoriteWords),
            )
        }
    }

    /**
     * Keeps only the synonyms and antonyms present in the rhyme set,
     * dropping entries which become empty.
     */
    private fun filterEntries(
        entries: List<ThesaurusEntry.ThesaurusEntryDetails>,
        rhymes: Set<String>,
    ): List<ThesaurusEntry.ThesaurusEntryDetails> = entries.mapNotNull { entry ->
        val filteredEntry = ThesaurusEntry.ThesaurusEntryDetails(
            entry.wordType,
            entry.synonyms.filter(rhymes::contains),
            entry.antonyms.filter(rhymes::contains),
        )
        if (filteredEntry.synonyms.isEmpty() && filteredEntry.antonyms.isEmpty()) null
        else filteredEntry
    }

    /**
     * Builds the list items from the thesaurus entry: per word type a
     * heading, then a synonyms section and an antonyms section, each with
     * its result words.
     */
    private fun buildItems(entry: ThesaurusEntry, favoriteWords: Set<String>): List<ThesaurusListItem> {
        val items = mutableListOf<ThesaurusListItem>()
        entry.entries.forEach { details ->
            items.add(ThesaurusListItem.Heading(details.wordType.name.lowercase(Locale.US)))
            addResultSection(items, R.string.thesaurus_section_synonyms, details.synonyms, favoriteWords)
            addResultSection(items, R.string.thesaurus_section_antonyms, details.antonyms, favoriteWords)
        }
        return items
    }

    private fun addResultSection(
        items: MutableList<ThesaurusListItem>,
        @StringRes sectionHeadingResId: Int,
        words: List<String>,
        favoriteWords: Set<String>,
    ) {
        if (words.isNotEmpty()) {
            items.add(ThesaurusListItem.Subheading(application.getString(sectionHeadingResId)))
            words.forEach { word ->
                items.add(ThesaurusListItem.Word(word, favoriteWords.contains(word)))
            }
        }
    }
}
