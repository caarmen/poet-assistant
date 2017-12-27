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
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import java.util.Locale
import javax.inject.Inject

class ThesaurusLiveData constructor(context: Context, private val query: String, private val filter: String?) : ResultListLiveData<ResultListData<RTEntryViewModel>>(context) {
    companion object {
        private val TAG = Constants.TAG + ThesaurusLiveData::class.java.simpleName
        @VisibleForTesting
        fun filter(entries: List<ThesaurusEntry.ThesaurusEntryDetails>, filter: Set<String>): List<ThesaurusEntry.ThesaurusEntryDetails> {
            val filteredEntries = ArrayList<ThesaurusEntry.ThesaurusEntryDetails>()
            entries.forEach {
                val filteredEntry = filter(it, filter)
                if (filteredEntry != null) filteredEntries.add(filteredEntry)
            }
            return filteredEntries
        }

        private fun filter(entry: ThesaurusEntry.ThesaurusEntryDetails, filter: Set<String>): ThesaurusEntry.ThesaurusEntryDetails? {
            val filteredEntry = ThesaurusEntry.ThesaurusEntryDetails(entry.wordType,
                    RTUtils.filter(entry.synonyms, filter),
                    RTUtils.filter(entry.antonyms, filter))
            if (isEmpty(filteredEntry)) return null
            return filteredEntry
        }

        private fun isEmpty(entry: ThesaurusEntry.ThesaurusEntryDetails): Boolean {
            return entry.synonyms.isEmpty() && entry.antonyms.isEmpty()
        }

    }

    @Inject
    lateinit var mRhymer: Rhymer
    @Inject
    lateinit var mThesaurus: Thesaurus
    @Inject
    lateinit var mPrefs: SettingsPrefs
    @Inject
    lateinit var mFavorites: Favorites

    init {
        DaggerHelper.getMainScreenComponent(context).inject(this)
    }

    override fun loadInBackground(): ResultListData<RTEntryViewModel> {
        Log.d(TAG, "loadInBackground: query=$query, filter=$filter")

        val data = ArrayList<RTEntryViewModel>()
        if (TextUtils.isEmpty(query)) return emptyResult()
        val result = mThesaurus.lookup(query, mPrefs.isIsThesaurusReverseLookupEnabled)
        var entries = result.entries
        if (entries.isEmpty()) return emptyResult()

        if (!TextUtils.isEmpty(filter)) {
            val rhymes = mRhymer.getFlatRhymes(filter!!)
            entries = filter(entries, rhymes)
        }

        val layout = Settings.getLayout(mPrefs)
        val favorites = mFavorites.getFavorites()
        entries.forEach {
            data.add(RTEntryViewModel(context, RTEntryViewModel.Type.HEADING, it.wordType.name.toLowerCase(Locale.US)))
            addResultSection(favorites, data, R.string.thesaurus_section_synonyms, it.synonyms, layout)
            addResultSection(favorites, data, R.string.thesaurus_section_antonyms, it.antonyms, layout)
        }
        return ResultListData(result.word, data)
    }

    private fun emptyResult(): ResultListData<RTEntryViewModel> {
        return ResultListData(query, emptyList())
    }

    private fun addResultSection(favorites: Set<String>, results: MutableList<RTEntryViewModel>, @StringRes sectionHeadingResId: Int, words: List<String>, layout: Settings.Layout) {
        if (words.isNotEmpty()) {
            results.add(RTEntryViewModel(context, RTEntryViewModel.Type.SUBHEADING, context.getString(sectionHeadingResId)))
            words.forEachIndexed { i, word ->
                /*@ColorRes */
                val color = if (i % 2 == 0) R.color.row_background_color_even else R.color.row_background_color_odd
                results.add(RTEntryViewModel(
                        context,
                        RTEntryViewModel.Type.WORD,
                        word,
                        ContextCompat.getColor(context, color),
                        favorites.contains(word),
                        layout == Settings.Layout.EFFICIENT
                ))
            }
        }
    }

}
