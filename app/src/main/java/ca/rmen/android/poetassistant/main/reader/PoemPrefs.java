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

package ca.rmen.android.poetassistant.main.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;

class PoemPrefs {
    private static final String TAG = Constants.TAG + PoemPrefs.class.getSimpleName();
    private final SharedPreferences mSharedPreferences;
    private static final String PREF_POEM_TEXT = "poem_text";
    private static final String PREF_POEM_URI = "poem_uri";
    private static final String PREF_POEM_NAME = "poem_name";

    PoemPrefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    boolean hasTempPoem() {
        return !mSharedPreferences.contains(PREF_POEM_URI)
                && mSharedPreferences.contains(PREF_POEM_TEXT);
    }

    String getTempPoem() {
        return mSharedPreferences.getString(PREF_POEM_TEXT, null);
    }

    boolean hasSavedPoem() {
        return mSharedPreferences.contains(PREF_POEM_URI);
    }

    void setSavedPoem(PoemFile poemFile) {
        Log.v(TAG, "setSavedPoem " + poemFile);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (poemFile.uri != null)
            editor.putString(PREF_POEM_URI, poemFile.uri.toString());
        else
            editor.remove(PREF_POEM_URI);
        editor.putString(PREF_POEM_TEXT, poemFile.text);
        editor.putString(PREF_POEM_NAME, poemFile.name);
        editor.apply();
    }

    void updatePoemText(String text) {
        Log.v(TAG, "updatePoemText " + text);
        mSharedPreferences.edit().putString(PREF_POEM_TEXT, text).apply();
    }

    @Nullable
    PoemFile getSavedPoem() {
        String uri = mSharedPreferences.getString(PREF_POEM_URI, null);
        if (uri != null) {
            String text = mSharedPreferences.getString(PREF_POEM_TEXT, null);
            String name = mSharedPreferences.getString(PREF_POEM_NAME, null);
            return new PoemFile(Uri.parse(uri), name, text);
        }
        return null;
    }

    void clear() {
        Log.v(TAG, "clear");
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_POEM_TEXT);
        editor.remove(PREF_POEM_URI);
        editor.remove(PREF_POEM_NAME);
        editor.apply();
    }

}

