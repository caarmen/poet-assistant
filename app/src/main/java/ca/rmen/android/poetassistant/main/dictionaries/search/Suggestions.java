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

package ca.rmen.android.poetassistant.main.dictionaries.search;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ca.rmen.android.poetassistant.UserDb;

public class Suggestions {

    private static final String TABLE_SUGGESTION = "SUGGESTION";
    private static final String COLUMN_WORD = "WORD";
    private final UserDb mUserDb;

    public Suggestions(UserDb userDb) {
        mUserDb = userDb;
    }

    @WorkerThread
    List<String> getSuggestions() {
        List<String> result = new ArrayList<>();
        Cursor cursor = mUserDb.getReadableDatabase().query(
                TABLE_SUGGESTION,
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
    void addSuggestion(final String suggestion) {
        executeDbOperation(() -> {
            // We should have defined a unique constraint in v1 of the db,
            // but we didn't :(  Too late now.  Don't want to do a db upgrade
            // for that.  So instead we check if a suggestion exists before adding
            // it.
            if (!hasSuggestion(suggestion)) {
                ContentValues values = new ContentValues(1);
                values.put(COLUMN_WORD, suggestion);
                mUserDb.getWritableDatabase().insert(
                        TABLE_SUGGESTION, null, values);
            }
        });
    }

    @MainThread
    void clear() {
        executeDbOperation(() -> mUserDb.getWritableDatabase()
                .delete(TABLE_SUGGESTION, null, null));
    }

    @WorkerThread
    private boolean hasSuggestion(String suggestion) {
        Cursor cursor = mUserDb.getReadableDatabase().query(
                TABLE_SUGGESTION,
                null, // projection
                COLUMN_WORD + "=?",
                new String[]{suggestion},
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

    @MainThread
    private static void executeDbOperation(final Runnable dbOperation) {
        AsyncTask.execute(dbOperation);
    }

    public static void createTable(Context context, SQLiteDatabase db) {
        db.execSQL(String.format(Locale.US, "CREATE TABLE \"%s\" (\"%s\" TEXT NOT NULL)",
                TABLE_SUGGESTION,
                COLUMN_WORD));

        // Migrate our data from shared prefs to the db.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> suggestions = prefs.getStringSet("pref_suggestions", null);
        if (suggestions != null) {
            for (String suggestion : suggestions) {
                ContentValues values = new ContentValues(1);
                values.put(COLUMN_WORD, suggestion);
                db.insert(TABLE_SUGGESTION, null, values);
            }
        }
        prefs.edit().remove("pref_suggestions").apply();
    }
}
