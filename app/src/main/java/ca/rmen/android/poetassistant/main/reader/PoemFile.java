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

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ca.rmen.android.poetassistant.Constants;


class PoemFile {
    private static final String TAG = Constants.TAG + PoemFile.class.getSimpleName();
    private final PoemFileCallback mCallback;

    public interface PoemFileCallback {
        void onPoemLoaded(File file, String text);

        void onPoemSaved(File file, String text);
    }

    public PoemFile(PoemFileCallback callback) {
        mCallback = callback;
    }

    public void open(final File file) {
        Log.d(TAG, "open() called with: " + "file = [" + file + "]");
        new AsyncTask<File, Void, String>() {
            @Override
            protected String doInBackground(File... params) {
                try {
                    FileReader reader = new FileReader(params[0]);
                    StringBuilder builder = new StringBuilder();
                    char[] buffer = new char[1024];
                    for (int read = reader.read(buffer); read > 0; read = reader.read(buffer)) {
                        builder.append(buffer, 0, read);
                    }
                    reader.close();
                    return builder.toString();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't open file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String text) {
                mCallback.onPoemLoaded(file, text);
            }
        }.execute(file);
    }

    public void save(final File file, final String text) {
        Log.d(TAG, "save() called with: " + "file = [" + file + "], text = [" + text + "]");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(text);
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "Couldn't save file", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (file != null) mCallback.onPoemSaved(file, text);
            }

        }.execute();
    }


}
