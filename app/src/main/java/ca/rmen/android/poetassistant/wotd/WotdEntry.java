/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.wotd;

import android.support.annotation.ColorInt;

public final class WotdEntry {

    public final String text;
    public final String date;
    public final @ColorInt int backgroundColor;
    public final boolean isFavorite;
    public final boolean showButtons;

    public WotdEntry(String text, String date, @ColorInt int backgroundColor, boolean isFavorite, boolean showButtons) {
        this.text = text;
        this.date = date;
        this.backgroundColor = backgroundColor;
        this.isFavorite = isFavorite;
        this.showButtons = showButtons;
    }
}
