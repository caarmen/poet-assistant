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
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.common.models.Share
import ca.rmen.android.poetassistant.main.wotd.WotdHistoryItem
import javax.inject.Inject

/**
 * Use case for creating shareable content from the words-of-the-day history.
 *
 * @param application The application for accessing string resources.
 */
class CreateWotdHistoryShareUseCase @Inject constructor(
    private val application: Application,
) {

    /**
     * Creates a Share object containing the formatted history.
     *
     * @param entries The history entries to share.
     * @return Share object with title and formatted content ready for sharing.
     */
    operator fun invoke(entries: List<WotdHistoryItem>): Share = Share(
        title = application.getString(R.string.share),
        content = buildString {
            append(application.getString(R.string.share_wotd_title))
            entries.forEach { entry ->
                append(application.getString(R.string.share_wotd_entry, entry.date, entry.word))
            }
        }
    )
}
