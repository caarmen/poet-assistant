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

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.support.annotation.ColorInt;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;

public final class WotdEntryViewModel {

    public final String text;
    public final String date;
    public final @ColorInt int backgroundColor;
    public final ObservableBoolean isFavorite = new ObservableBoolean();
    public final boolean showButtons;

    @Inject Favorites mFavorites;

    public WotdEntryViewModel(Context context, String text, String date, @ColorInt int backgroundColor, boolean isFavorite, boolean showButtons) {
        DaggerHelper.getWotdComponent(context).inject(this);
        this.text = text;
        this.date = date;
        this.backgroundColor = backgroundColor;
        this.isFavorite.set(isFavorite);
        this.showButtons = showButtons;
        this.isFavorite.addOnPropertyChangedCallback(
                new BindingCallbackAdapter(() -> mFavorites.saveFavorite(text, this.isFavorite.get())));
    }
}
