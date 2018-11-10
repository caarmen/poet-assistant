/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

import java.util.Collections

object RTUtils {

    /**
     * @return all the Strings in the words which are present in the Set filter
     */
    fun filter(words : Array<String>, filter: Collection<String>) : Array<String> {
        return filter(words.toList(), filter).toTypedArray()
    }

    /**
     * @return all the Strings in the words which are present in the Set filter
     */
    fun filter(words : List<String>?, filter: Collection<String>) : List<String> {
        if (words == null) return Collections.emptyList()
        return words.filter(filter::contains)
    }
}
