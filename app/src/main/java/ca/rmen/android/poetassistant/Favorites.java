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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Favorites {

    private static final String TAG = Constants.TAG + Favorites.class.getSimpleName();
    private static final String TABLE_FAVORITE = "FAVORITE";
    private static final String COLUMN_WORD = "WORD";

    private final UserDb mUserDb;

    /**
     * Subscribe to this using EventBus to know when favorites are changed.
     */
    public static class OnFavoritesChanged {
        private OnFavoritesChanged() {
        }
    }

    public Favorites(UserDb userDb) {
        mUserDb = userDb;
    }

    @WorkerThread
    public boolean isFavorite(String word) {
        if (TextUtils.isEmpty(word)) return false;
        Cursor cursor = mUserDb.getReadableDatabase().query(
                TABLE_FAVORITE,
                new String[]{COLUMN_WORD}, // projection
                COLUMN_WORD + " = ?", // selection
                new String[] {word}, // selectionArgs
                null, // groupBy
                null, // having
                null); // orderBy
        if (cursor != null) {
            try {
                return cursor.getCount() > 0;
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    @WorkerThread
    public Set<String> getFavorites() {
        TreeSet<String> result = new TreeSet<>();
        Cursor cursor = mUserDb.getReadableDatabase().query(
                TABLE_FAVORITE,
                new String[]{COLUMN_WORD}, // projection
                null, // selection
                null, // selectionArgs
                null, // groupBy
                null, // having
                null); // orderBy
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    @MainThread
    public void saveFavorite(String word, boolean isFavorite) {
        if (isFavorite) addFavorite(word);
        else removeFavorite(word);
    }

    @MainThread
    private void addFavorite(final String favorite) {
        Log.v(TAG, "addFavorite " + favorite);
        executeDbOperation(() -> {
            ContentValues values = new ContentValues(1);
            values.put(COLUMN_WORD, favorite);
            mUserDb.getWritableDatabase().insert(
                    TABLE_FAVORITE, null, values);
        });
    }

    @MainThread
    private void removeFavorite(final String favorite) {
        Log.v(TAG, "removeFavorite " + favorite);
        executeDbOperation(() -> {
            ContentValues values = new ContentValues(1);
            values.put(COLUMN_WORD, favorite);
            mUserDb.getWritableDatabase().delete(
                    TABLE_FAVORITE,
                    COLUMN_WORD + "=?",
                    new String[]{favorite});
        });
    }

    @MainThread
    public void clear() {
        executeDbOperation(() -> mUserDb.getWritableDatabase()
                .delete(TABLE_FAVORITE, null, null));
    }

    @MainThread
    private static void executeDbOperation(Runnable dbOperation) {
        Completable.fromRunnable(dbOperation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> EventBus.getDefault().post(new OnFavoritesChanged()));
    }

    static void createTable(Context context, SQLiteDatabase db) {
        db.execSQL(String.format(Locale.US, "CREATE TABLE \"%s\" (\"%s\" TEXT NOT NULL)",
                TABLE_FAVORITE,
                COLUMN_WORD));

        // Migrate our data from shared prefs to the db.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favorites = prefs.getStringSet("PREF_FAVORITE_WORDS", null);
        if (favorites != null) {
            for (String favorite : favorites) {
                ContentValues values = new ContentValues(1);
                values.put(COLUMN_WORD, favorite);
                db.insert(TABLE_FAVORITE, null, values);
            }
        }
        prefs.edit().remove("PREF_FAVORITE_WORDS").apply();

    }
}
