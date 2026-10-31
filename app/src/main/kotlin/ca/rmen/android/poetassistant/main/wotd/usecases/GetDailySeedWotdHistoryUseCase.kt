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

package ca.rmen.android.poetassistant.main.wotd.usecases

import android.app.Application
import android.text.format.DateUtils
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.wotd.WotdHistoryItem
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

/**
 * Generates the words-of-the-day history: for each of the last
 * [HISTORY_SIZE] days, [WotdSelection] picks one word from the candidate
 * words in the interesting-frequency range.
 *
 * The word selection uses UTC days, while the date is formatted for display
 * in the default timezone.
 *
 * @param dictionaryRepository The dictionary repository for candidate words.
 * The repository performs its queries on the IO dispatcher.
 * @param application The application for formatting dates.
 */
class GetDailySeedWotdHistoryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val application: Application,
) : GetWotdHistoryUseCase {
    companion object {
        /**
         * Number of days of history to show.
         */
        private const val HISTORY_SIZE = 100
    }

    override suspend operator fun invoke(favoriteWords: Set<String>): List<WotdHistoryItem> =
        invoke(favoriteWords, WotdSelection.todayUtc())

    /**
     * Generates the history of words of the day for the days ending at the
     * given date. The given calendar is read-only for this use case: it is
     * not modified.
     *
     * @param favoriteWords The current set of favorite words.
     * @param asOf The last day of the history.
     * @return one history entry per day, starting with the given day; an
     *         empty list if the dictionary DB is not loaded.
     */
    suspend operator fun invoke(favoriteWords: Set<String>, asOf: Calendar): List<WotdHistoryItem> {
        val candidates = dictionaryRepository.getWordsInFrequencyRange(
            WotdSelection.MIN_INTERESTING_FREQUENCY,
            WotdSelection.MAX_INTERESTING_FREQUENCY
        )
        if (candidates.isEmpty()) return emptyList()

        // The word for each day is derived from the start of the UTC day.
        val calendar = asOf.clone() as Calendar
        // The displayed date uses the same instant, in the default timezone.
        val calendarDisplay = asOf.clone() as Calendar
        calendarDisplay.timeZone = TimeZone.getDefault()
        return buildList {
            repeat(HISTORY_SIZE) {
                val date = DateUtils.formatDateTime(
                    application,
                    calendarDisplay.timeInMillis,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL
                )
                val word = WotdSelection.pick(candidates, calendar)
                add(WotdHistoryItem(word, date, favoriteWords.contains(word)))
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendarDisplay.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
    }
}
