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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.android.poetassistant.Constants;

public class DbUtil {
    private static final String TAG = Constants.TAG + DbUtil.class.getSimpleName();

    private DbUtil() {
    }

    public static SQLiteDatabase open(Context context, String file) {
        DbUtil.copyDb(context, file);
        File dbPath = new File(context.getDir("databases", Context.MODE_PRIVATE), file);
        return SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    private static void copyDb(Context context, String dbFile) {
        File dbPath = context.getDatabasePath(dbFile);
        if (!dbPath.exists()) {
            Log.v(TAG, dbPath + " not found");
            try {
                InputStream is = context.getAssets().open(dbFile);
                File dbDir = context.getDir("databases", Context.MODE_PRIVATE);
                FileOutputStream os = new FileOutputStream(new File(dbDir, dbFile));
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                while (read > 0) {
                    os.write(buffer, 0, read);
                    read = is.read(buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing to " + dbPath + ": " + e.getMessage(), e);
            }
            Log.v(TAG, "wrote " + dbPath);
        }
    }
}
