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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary

/**
 * Domain policy for the interesting-words pool: words too rare or too
 * common are not interesting.
 *
 * The bounds are the single source of truth for the candidate pool
 * shared by the random word feature and the word-of-the-day pipeline
 * (the notification and the history tab). The values must not change:
 * the pool decides which words can be picked.
 */
object InterestingWords {

    /**
     * Words less frequent than this are not interesting (too rare).
     */
    const val MIN_INTERESTING_FREQUENCY = 1500

    /**
     * Words more frequent than this are not interesting (too common).
     */
    const val MAX_INTERESTING_FREQUENCY = 25000
}
