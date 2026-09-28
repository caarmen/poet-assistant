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

import android.content.Context
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.dictionaries.thesaurus.ThesaurusListItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for creating shareable content from the thesaurus results.
 *
 * @param context The application context for accessing string resources.
 */
class CreateThesaurusShareUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Creates a Share object containing the formatted thesaurus results.
     *
     * @param word The matched word.
     * @param filter The active filter, if any.
     * @param entries The result list items.
     * @return Share object with title and formatted content ready for sharing.
     */
    operator fun invoke(word: String, filter: String?, entries: List<ThesaurusListItem>): Share = Share(
        title = context.getString(R.string.share),
        content = buildString {
            append(
                if (filter.isNullOrEmpty()) context.getString(R.string.share_thesaurus_title, word)
                else context.getString(R.string.share_thesaurus_title_with_filter, word, filter)
            )
            entries.forEach { item ->
                when (item) {
                    is ThesaurusListItem.Heading -> append(context.getString(R.string.share_rt_heading, item.text))
                    is ThesaurusListItem.Subheading -> append(context.getString(R.string.share_rt_subheading, item.text))
                    is ThesaurusListItem.Word -> append(context.getString(R.string.share_rt_entry, item.word))
                }
            }
        }
    )
}
