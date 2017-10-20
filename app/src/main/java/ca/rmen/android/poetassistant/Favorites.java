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

package ca.rmen.android.poetassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Favorites {

    private static final String TAG = Constants.TAG + Favorites.class.getSimpleName();

    private final FavoriteDao mFavoriteDao;

    public Favorites(FavoriteDao favoriteDao) {
        mFavoriteDao = favoriteDao;
    }

    public LiveData<Boolean> getIsFavoriteLiveData(String word) {
        return Transformations.map(mFavoriteDao.getCountLiveData(word), count -> count > 0);
    }

    public LiveData<Boolean> observeIsFavorite(String word) {
        return Transformations.map(mFavoriteDao.observeFavoriteCount(word), count -> count > 0);
    }

    public LiveData<List<Favorite>> observeFavorites() {
        return mFavoriteDao.observeFavorites();
    }

    @WorkerThread
    public Set<String> getFavorites() {
        return new HashSet<>(
                Observable.fromIterable(mFavoriteDao.getFavorites())
                        .map(Favorite::getWord)
                        .toList().blockingGet());
    }

    @WorkerThread
    public void exportFavorites(Context context, Uri uri) throws IOException {
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
        if (outputStream == null) throw new IOException("Can't open null output stream");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            Set<String> favorites = getFavorites();
            for (String favorite : favorites) {
                writer.write(favorite);
                writer.newLine();
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    @MainThread
    public void saveFavorite(String word, boolean isFavorite) {
        if (isFavorite) Schedulers.io().scheduleDirect(() -> mFavoriteDao.insert(new Favorite(word)));
        else removeFavorite(word);
    }

    @WorkerThread
    public void importFavorites(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Set<String> favorites = getFavorites();
        Set<Favorite> favoritesToAdd = new HashSet<>();
        if (inputStream == null) throw new IOException("Can't open null input stream");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (!TextUtils.isEmpty(line)) {
                    String favorite = line.trim().toLowerCase(Locale.getDefault());
                    if (!TextUtils.isEmpty(favorite) && !favorites.contains(favorite)) {
                        favorites.add(favorite);
                        favoritesToAdd.add(new Favorite(favorite));
                    }
                }
            }
            mFavoriteDao.insertAll(favoritesToAdd);
        } finally {
            if (reader != null) reader.close();
        }
    }

    @MainThread
    private void removeFavorite(String favorite) {
        Log.v(TAG, "removeFavorite " + favorite);
        Schedulers.io().scheduleDirect(() -> mFavoriteDao.delete(new Favorite(favorite)));
    }

    @MainThread
    public void clear() {
        Schedulers.io().scheduleDirect(mFavoriteDao::deleteAll);
    }

}
