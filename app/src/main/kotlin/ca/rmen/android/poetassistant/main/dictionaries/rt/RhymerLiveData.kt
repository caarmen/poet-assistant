/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.rt

import android.content.Context
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import ca.rmen.rhymer.RhymeResult
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import java.util.TreeSet

class RhymerLiveData(
    context: Context,
    coroutineScope: CoroutineScope,
    val query: String,
    val filter: String?,
) :
    ResultListLiveData<ResultListData<RTListItem>>(context, coroutineScope) {

    companion object {
        private val TAG = Constants.TAG + RhymerLiveData::class.java.simpleName
        private fun filter(rhymes: List<RhymeResult>, filter: Collection<String>): List<RhymeResult> {
            val filteredRhymes = ArrayList<RhymeResult>()
            rhymes.forEach {
                val filteredRhymeResult = filter(it, filter)
                if (filteredRhymeResult != null) filteredRhymes.add(filteredRhymeResult)
            }
            return filteredRhymes
        }

        private fun filter(rhyme: RhymeResult, filter: Collection<String>): RhymeResult? {
            val result = RhymeResult(rhyme.variantNumber,
                    RTUtils.filter(rhyme.strictRhymes, filter),
                    RTUtils.filter(rhyme.oneSyllableRhymes, filter),
                    RTUtils.filter(rhyme.twoSyllableRhymes, filter),
                    RTUtils.filter(rhyme.threeSyllableRhymes, filter))
            if (isEmpty(result)) return null
            return result
        }

        private fun isEmpty(rhymeResult: RhymeResult): Boolean {
            return rhymeResult.strictRhymes.isEmpty()
                    && rhymeResult.oneSyllableRhymes.isEmpty()
                    && rhymeResult.twoSyllableRhymes.isEmpty()
                    && rhymeResult.threeSyllableRhymes.isEmpty()
        }
    }

    private val mPrefs: SettingsPrefs
    private val mRhymer: Rhymer
    private val mThesaurus: Thesaurus
    private val mFavorites: Favorites

    init {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, NonAndroidEntryPoint::class.java)
        mRhymer = entryPoint.rhymer()
        mThesaurus = entryPoint.thesaurus()
        mPrefs = entryPoint.prefs()
        mFavorites = entryPoint.favorites()
    }

    override fun loadInBackground(): ResultListData<RTListItem> {
        Log.d(TAG, "loadInBackground: query=$query, filter=$filter")
        val before = System.currentTimeMillis()

        val data = ArrayList<RTListItem>()
        if (TextUtils.isEmpty(query)) return emptyResult()

        var rhymeResults = mRhymer.getRhymingWords(query, Constants.MAX_RESULTS)
                ?: return emptyResult()
        if (!TextUtils.isEmpty(filter)) {
            val synonyms = mThesaurus.getFlatSynonyms(filter!!, mPrefs.isThesaurusReverseLookupEnabled)
            if (synonyms.isEmpty()) return emptyResult()
            rhymeResults = filter(rhymeResults, synonyms)
        }

        val layout = SettingsPrefs.getLayout(mPrefs)
        val favorites = mFavorites.getFavorites()
        if (favorites.isNotEmpty()) {
            addResultSection(favorites, data, R.string.rhyme_section_favorites, getMatchingFavorites(rhymeResults, favorites), layout)
        }
        rhymeResults.forEach {
            // Add the word variant, if there are multiple pronunciations.
            if (rhymeResults.size > 1) {
                val heading = query + " (" + (it.variantNumber + 1) + ")"
                data.add(RTListItem(RTListItem.Type.HEADING, heading))
            }
            addResultSection(favorites, data, R.string.rhyme_section_stress_syllables, it.strictRhymes, layout)
            addResultSection(favorites, data, R.string.rhyme_section_three_syllables, it.threeSyllableRhymes, layout)
            addResultSection(favorites, data, R.string.rhyme_section_two_syllables, it.twoSyllableRhymes, layout)
            addResultSection(favorites, data, R.string.rhyme_section_one_syllable, it.oneSyllableRhymes, layout)
        }
        val result = ResultListData(query, data)
        val after = System.currentTimeMillis()
        Log.d(TAG, "loadInBackground finished in ${(after - before)} ms")
        return result
    }

    private fun getMatchingFavorites(rhymeResults: List<RhymeResult>, favorites: Set<String>): Array<String> {
        val matchingFavorites = TreeSet<String>()
        rhymeResults.forEach { rhymeResult ->
            matchingFavorites.addAll(rhymeResult.strictRhymes.filter { rhyme -> favorites.contains(rhyme) })
            matchingFavorites.addAll(rhymeResult.oneSyllableRhymes.filter { rhyme -> favorites.contains(rhyme) })
            matchingFavorites.addAll(rhymeResult.twoSyllableRhymes.filter { rhyme -> favorites.contains(rhyme) })
            matchingFavorites.addAll(rhymeResult.threeSyllableRhymes.filter { rhyme -> favorites.contains(rhyme) })
        }
        return matchingFavorites.toTypedArray()
    }

    private fun emptyResult(): ResultListData<RTListItem> {
        return ResultListData(query, emptyList())
    }

    private fun addResultSection(favorites: Set<String>, results: MutableList<RTListItem>, sectionHeadingResId: Int, rhymes: Array<String>, layout: ca.rmen.android.poetassistant.settings.SettingsPrefs.Layout) {
        if (rhymes.isNotEmpty()) {
            val wordsWithDefinitions = if (mPrefs.isAllRhymesEnabled) mRhymer.getWordsWithDefinitions(rhymes) else null
            results.add(RTListItem(RTListItem.Type.SUBHEADING, context.getString(sectionHeadingResId)))
            rhymes.forEach { rhyme ->
                val hasDefinition = wordsWithDefinitions == null || wordsWithDefinitions.contains(rhyme)
                results.add(RTListItem(
                        RTListItem.Type.WORD,
                        rhyme,
                        favorites.contains(rhyme),
                        hasDefinition,
                        layout == SettingsPrefs.Layout.EFFICIENT,
                ))
            }
            if (results.size >= Constants.MAX_RESULTS) {
                results.add(RTListItem(
                        RTListItem.Type.SUBHEADING,
                        context.getString(R.string.max_results, Constants.MAX_RESULTS)
                ))
            }
        }
    }

}
