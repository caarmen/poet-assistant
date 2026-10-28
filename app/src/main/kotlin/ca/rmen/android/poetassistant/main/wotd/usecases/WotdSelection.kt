/*
 * Copyright (c) 2026-present Carmen Alvarez
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

import java.util.Calendar
import java.util.Random
import java.util.TimeZone

/**
 * Single source of truth for which word is the word of the day: the
 * candidate bounds, the UTC-day logic, and the seeded pick.
 *
 * Everything that defines the word of the day must come from here, so that
 * the notification and the WOTD history tab show the same word for the same
 * day and database.
 */
object WotdSelection {

    /**
     * Words less frequent than this are not interesting (too rare).
     */
    const val MIN_INTERESTING_FREQUENCY = 1500

    /**
     * Words more frequent than this are not interesting (too common).
     */
    const val MAX_INTERESTING_FREQUENCY = 25000

    /**
     * Picks the word of the day for the given day from the candidate words.
     *
     * The pick depends only on the UTC-day instant and the candidate list.
     * Do not change the seed, the Random implementation, the bounds, or the
     * candidate ordering: the pick must stay identical to the word already
     * shown on devices for the same day and database.
     *
     * @param candidates Candidate words, sorted by word ascending. The
     * caller decides the candidate set; use [MIN_INTERESTING_FREQUENCY] and
     * [MAX_INTERESTING_FREQUENCY] to build it.
     * @param day The day for which to pick the word. Read-only: it is not
     * modified.
     * @return The word of the day for the given day.
     */
    fun pick(candidates: List<String>, day: Calendar): String {
        val random = Random()
        random.setSeed(day.timeInMillis)
        return candidates[random.nextInt(candidates.size)]
    }

    /**
     * Returns a calendar for the start of the current UTC day (00:00:00.000).
     */
    fun todayUtc(): Calendar {
        val now = Calendar.getInstance()
        now.timeZone = TimeZone.getTimeZone("UTC")
        now[Calendar.HOUR_OF_DAY] = 0
        now[Calendar.MINUTE] = 0
        now[Calendar.SECOND] = 0
        now[Calendar.MILLISECOND] = 0
        return now
    }
}
