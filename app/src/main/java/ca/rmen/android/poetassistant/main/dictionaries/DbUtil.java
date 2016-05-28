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
import android.database.sqlite.SQLiteException;
import android.support.annotation.Nullable;
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

    /**
     * @return null if the database could not be opened
     */
    @Nullable
    public static SQLiteDatabase open(Context context, String dbName, int version) {
        DbUtil.copyDb(context, dbName, version);
        String dbFile = getDbFileName(dbName, version);
        File dbPath = new File(context.getDir("databases", Context.MODE_PRIVATE), dbFile);
        try {
            return SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.w(TAG, "Could not open database " + dbName + ":" + version + ": " + e.getMessage(), e);
            return null;
        }
    }

    private static void copyDb(Context context, String dbName, int version) {
        String dbFileName = getDbFileName(dbName, version);
        File dbPath = getDbFile(context, dbFileName);
        if (!dbPath.exists()) {
            Log.v(TAG, dbPath + " not found");
            for (int i = 0; i < version; i++) {
                deleteDb(context, dbName, i);
            }

            try {
                InputStream is = context.getAssets().open(dbFileName);
                FileOutputStream os = new FileOutputStream(dbPath);
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                while (read > 0) {
                    os.write(buffer, 0, read);
                    read = is.read(buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing to " + dbPath + ": " + e.getMessage(), e);
                deleteDb(context, dbName, version);
            }
            Log.v(TAG, "wrote " + dbPath);
        }
    }

    private static String getDbFileName(String dbName, int version) {
        if (version == 1) return dbName + ".db";
        return dbName + version + ".db";
    }

    private static void deleteDb(Context context, String dbName, int version) {
        String dbFileName = getDbFileName(dbName, version);
        File dbPath = getDbFile(context, dbFileName);
        if (dbPath.exists()) {
            boolean deleted = dbPath.delete();
            Log.v(TAG, "dbDelete: deletion of " + dbPath.getAbsolutePath() + ": " + deleted);
        }
    }

    private static File getDbFile(Context context, String filename) {
        File dbDir = context.getDir("databases", Context.MODE_PRIVATE);
        return new File(dbDir, filename);
    }

}
