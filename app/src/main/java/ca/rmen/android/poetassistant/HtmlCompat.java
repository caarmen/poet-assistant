/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.Html;

public final class HtmlCompat {
    private HtmlCompat() {
        // prevent instantiation
    }

    public static CharSequence fromHtml(String content) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            return fromHtml24(content);
        } else {
            //noinspection deprecation
            return Html.fromHtml(content);
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    private static CharSequence fromHtml24(String content) {
        return Html.fromHtml(content, 0);
    }

}
