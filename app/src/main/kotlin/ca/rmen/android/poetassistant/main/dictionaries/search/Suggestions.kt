/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.search

import androidx.annotation.WorkerThread

class Suggestions(private val suggestionDao: SuggestionDao) {

    @WorkerThread
    fun getSuggestions(): List<String> = suggestionDao.getSuggestions().map(Suggestion::getWord)

    @WorkerThread
    fun addSuggestion(suggestion: String) = suggestionDao.insertAll(Suggestion(suggestion))

    @WorkerThread
    fun clear() = suggestionDao.deleteAll()
}
