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

package ca.rmen.android.poetassistant.main.dictionaries.patterns.usecases

import android.content.Context
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.dictionaries.patterns.PatternEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for creating shareable content from pattern search results.
 * Replaces the legacy PatternListExporter.
 *
 * @param context The application context for accessing string resources.
 */
class CreatePatternShareUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Creates a Share object containing formatted pattern search results.
     *
     * @param pattern The pattern that was searched for.
     * @param entries The list of pattern entries to share.
     * @return Share object with title and formatted content ready for sharing.
     */
    operator fun invoke(pattern: String, entries: List<PatternEntry>): Share = Share(
        title = context.getString(R.string.share_patterns_title, pattern),
        content = buildString {
            entries.forEach { entry ->
                append(context.getString(R.string.share_rt_entry, entry.word))
            }
        }
    )
}
