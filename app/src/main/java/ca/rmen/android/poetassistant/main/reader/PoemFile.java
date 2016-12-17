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
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ca.rmen.android.poetassistant.Constants;


class PoemFile {
    private static final String TAG = Constants.TAG + PoemFile.class.getSimpleName();

    interface PoemFileCallback {
        void onPoemLoaded(PoemFile poemFile);

        void onPoemSaved(PoemFile poemFile);
    }

    final Uri uri;
    final String name;
    final String text;

    PoemFile(Uri uri, String name, String text) {
        this.uri = uri;
        this.name = name;
        this.text = text;
    }

    public static void open(final Context context, final Uri uri, final PoemFileCallback callback) {
        Log.d(TAG, "open() called with: " + "uri = [" + uri + "]");
        new AsyncTask<Void, Void, PoemFile>() {
            @Override
            protected PoemFile doInBackground(Void... params) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream == null) return null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    String text = stringBuilder.toString();
                    reader.close();
                    String displayName = readDisplayName(context, uri);
                    return new PoemFile(uri, displayName, text);

                } catch (IOException e) {
                    Log.w(TAG, "Couldn't open file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(PoemFile poemFile) {
                callback.onPoemLoaded(poemFile);
            }
        }.execute();
    }

    static void save(final Context context, final Uri uri, final String text, final PoemFileCallback callback) {
        Log.d(TAG, "save() called with: " + "uri = [" + uri + "], text = [" + text + "]");
        new AsyncTask<Void, Void, PoemFile>() {

            @Override
            protected PoemFile doInBackground(Void... params) {
                try {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "w");
                    if (outputStream == null) return null;
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write(text);
                    writer.close();
                    String displayName = readDisplayName(context, uri);
                    return new PoemFile(uri, displayName, text);
                }
                catch (IOException | SecurityException e) {
                    // Catch the SecurityException because of some crash
                    // reported which I couldn't reproduce: https://github.com/caarmen/poet-assistant/issues/18
                    Log.w(TAG, "Couldn't save file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(PoemFile poemFile) {
                if (uri != null) callback.onPoemSaved(poemFile);
            }

        }.execute();
    }

    /**
     * Generate a suggested filename based on the first few words of the poem text.
     */
    @VisibleForTesting
    static String generateFileName(String text) {
        final int minLength = 8;
        final int maxLength = 16;
        String textStart;
        try {
            textStart = Pattern.compile("[^\\p{L}]+").matcher(text).replaceAll("-");
            textStart = textStart.replaceAll("^-", "");
        } catch (PatternSyntaxException e) {
            // Not sure why \\p{IsAlphabetic} worked on unit tests but not on an android device.
            // \\p{L} worked on a couple of devices, but let's not take any chances.
            Log.v(TAG, "Couldn't generate file name for " + text + ": " + e.getMessage(), e);
            return null;
        }
        textStart = textStart.substring(0, Math.min(maxLength, textStart.length()));
        int lastWordBegin = textStart.length();
        for (int i = textStart.length() - 1; i > minLength; i--) {
            if (!Character.isLetter(textStart.charAt(i))) {
                lastWordBegin = i;
                break;
            }
        }
        // replace trailing hyphen
        textStart = textStart.replaceAll("-$", "");
        // If there's nothing left, give up.
        if (textStart.length() == 0) return null;
        if (textStart.length() <= minLength) return textStart + ".txt";
        lastWordBegin = Math.min(lastWordBegin, textStart.length());
        textStart = textStart.substring(0, lastWordBegin) + ".txt";
        return textStart;
    }

    private static String readDisplayName(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
}
