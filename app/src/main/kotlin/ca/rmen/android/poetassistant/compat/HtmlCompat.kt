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

package ca.rmen.android.poetassistant.compat

import android.annotation.TargetApi
import android.os.Build
import android.text.Html

object HtmlCompat {
    fun fromHtml(content: String): CharSequence {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            return fromHtml24(content)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(content)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun fromHtml24(content: String): CharSequence {
        return Html.fromHtml(content, 0)
    }
}
