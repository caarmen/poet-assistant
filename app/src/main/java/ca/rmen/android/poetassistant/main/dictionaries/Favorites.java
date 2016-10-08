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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class Favorites {

    /**
     * Subscribe to this using EventBus to know when favorites are changed.
     */
    public static class OnFavoritesChanged {
        OnFavoritesChanged() {
        }
    }

    // Maybe some day this will be backed by a DB instead of shared prefs.
    private final SettingsPrefs mPrefs;

    public Favorites(Context context) {
        mPrefs = SettingsPrefs.get(context.getApplicationContext());
    }

    public Set<String> getFavorites() {
        return mPrefs.getFavoriteWords();
    }

    public void addFavorite(String favorite) {
        // We need to make a copy of the string set, or our changes
        // won't be saved:
        // https://code.google.com/p/android/issues/detail?id=27801
        Set<String> favorites = new HashSet<>(mPrefs.getFavoriteWords());
        favorites.add(favorite);
        mPrefs.putFavoriteWords(favorites);
        EventBus.getDefault().post(new OnFavoritesChanged());
    }
    public void removeFavorite(String favorite) {
        Set<String> favorites = new HashSet<>(mPrefs.getFavoriteWords());
        favorites.remove(favorite);
        mPrefs.putFavoriteWords(favorites);
        EventBus.getDefault().post(new OnFavoritesChanged());
    }

    public void clear() {
        mPrefs.removeFavoriteWords();
        EventBus.getDefault().post(new OnFavoritesChanged());
    }

}
