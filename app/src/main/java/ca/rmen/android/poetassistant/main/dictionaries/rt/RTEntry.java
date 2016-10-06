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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

public class RTEntry {
    enum Type {
        HEADING,
        SUBHEADING,
        WORD
    }

    public final Type type;
    public final String text;
    public final @ColorInt int backgroundColor;
    public final @DrawableRes int favoriteIcon;

    public RTEntry(Type type, String text) {
        this(type, text, 0, 0);
    }

    public RTEntry(Type type, String text, @ColorInt int backgroundColor, @DrawableRes int favoriteIcon) {
        this.type = type;
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.favoriteIcon = favoriteIcon;
    }

    @Override
    public String toString() {
        return "RTEntry{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
