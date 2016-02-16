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
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import ca.rmen.android.poetassistant.Constants;


class PoemFile {
    private static final String TAG = Constants.TAG + PoemFile.class.getSimpleName();

    public interface PoemFileCallback {
        void onPoemLoaded(PoemFile poemFile);

        void onPoemSaved(PoemFile poemFile);
    }

    public final Uri uri;
    public final String name;
    public final String text;

    public PoemFile(Uri uri, String name, String text) {
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

    public static void save(final Context context, final Uri uri, final String text, final PoemFileCallback callback) {
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
                } catch (IOException e) {
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
