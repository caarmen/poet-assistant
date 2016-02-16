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
import android.net.Uri;
import android.os.AsyncTask;
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
    private final Context mContext;
    private final PoemFileCallback mCallback;

    public interface PoemFileCallback {
        void onPoemLoaded(Uri uri, String text);

        void onPoemSaved(Uri uri, String text);
    }

    public PoemFile(Context context, PoemFileCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public void open(final Uri uri) {
        Log.d(TAG, "open() called with: " + "uri = [" + uri + "]");
        new AsyncTask<Uri, Void, String>() {
            @Override
            protected String doInBackground(Uri... params) {
                try {
                    InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                    if (inputStream == null) return null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    reader.close();
                    return stringBuilder.toString();

                } catch (IOException e) {
                    Log.w(TAG, "Couldn't open file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String text) {
                mCallback.onPoemLoaded(uri, text);
            }
        }.execute(uri);
    }

    public void save(final Uri uri, final String text) {
        Log.d(TAG, "save() called with: " + "uri = [" + uri + "], text = [" + text + "]");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OutputStream outputStream = mContext.getContentResolver().openOutputStream(uri, "w");
                    if (outputStream == null) return null;
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write(text);
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't save file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (uri != null) mCallback.onPoemSaved(uri, text);
            }

        }.execute();
    }
}
