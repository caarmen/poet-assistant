/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary.usecases

import android.content.Context
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for creating shareable content from dictionary entries.
 * Formats the dictionary entries into a shareable string with proper formatting.
 *
 * @param context The application context for accessing string resources.
 */
class CreateDictionaryShareUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    /**
     * Creates a Share object containing formatted dictionary content.
     *
     * @param entry The dictionary entry to format and share.
     * @return Share object with title and formatted content ready for sharing.
     */
    operator fun invoke(entry: DictionaryEntry): Share = Share(
        title = context.getString(R.string.share),
        content = buildString {
            append(context.getString(R.string.share_dictionary_title, entry.word))
            entry.details.forEach { detail ->
                append(context.getString(
                    R.string.share_dictionary_entry,
                    detail.partOfSpeech,
                    detail.definition
                ))
            }
        }
    )
}
