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
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.android.poetassistant.Constants;

public class DbHelper {
    private static final String TAG = Constants.TAG + DbHelper.class.getSimpleName();

    private final Context mContext;
    private static final String DB_NAME = "poet_assistant";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase mDb;
    private final Object mLock = new Object();
    private static DbHelper sInstance;

    public static synchronized DbHelper getInstance(Context context) {
        if (sInstance == null) sInstance = new DbHelper(context);
        return sInstance;
    }

    private DbHelper(Context context) {
        mContext = context;
    }

    public SQLiteDatabase getDb() {
        open();
        return mDb;
    }

    private void open() {
        synchronized (mLock) {
            if (mDb == null) {
                Log.v(TAG, "Open db " + DB_NAME + ":" + DB_VERSION);
                copyDb();
                String dbFile = getDbFileName(DB_VERSION);
                File dbPath = new File(mContext.getDir("databases", Context.MODE_PRIVATE), dbFile);
                try {
                    mDb = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                } catch (SQLiteException e) {
                    Log.w(TAG, "Could not open database " + DB_NAME + ":" + DB_VERSION + ": " + e.getMessage(), e);
                }
            }
        }
    }

    private void copyDb() {
        String dbFileName = getDbFileName(DB_VERSION);
        File dbPath = getDbFile(dbFileName);
        if (!dbPath.exists()) {
            Log.v(TAG, dbPath + " not found");
            for (int i = 0; i < DB_VERSION; i++) {
                deleteDb(i);
            }
            deleteOldDbs("rhymes", 2);
            deleteOldDbs("thesaurus", 2);
            deleteOldDbs("dictionary", 2);

            try {
                InputStream is = mContext.getAssets().open(dbFileName);
                FileOutputStream os = new FileOutputStream(dbPath);
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                while (read > 0) {
                    os.write(buffer, 0, read);
                    read = is.read(buffer);
                }
                Log.v(TAG, "wrote " + dbPath);
            } catch (IOException e) {
                Log.e(TAG, "Error writing to " + dbPath + ": " + e.getMessage(), e);
                deleteDb(DB_VERSION);
            }
        }
    }

    private void deleteOldDbs(String name, int maxVersion) {
        for (int i = 0; i <= maxVersion; i++) {
            final String dbFileName;
            if (i == 1) dbFileName = name + ".db";
            else dbFileName = name + i + ".db";
            File dbPath = getDbFile(dbFileName);
            if (dbPath.exists()) {
                boolean deleted = dbPath.delete();
                Log.v(TAG, "dbDelete: deletion of " + dbPath.getAbsolutePath() + ": " + deleted);
            }
        }
    }

    private String getDbFileName(int version) {
        if (version == 1) return DB_NAME + ".db";
        return DB_NAME + version + ".db";
    }

    private void deleteDb(int version) {
        String dbFileName = getDbFileName(version);
        File dbPath = getDbFile(dbFileName);
        if (dbPath.exists()) {
            boolean deleted = dbPath.delete();
            Log.v(TAG, "dbDelete: deletion of " + dbPath.getAbsolutePath() + ": " + deleted);
        }
    }

    private File getDbFile(String filename) {
        File dbDir = mContext.getDir("databases", Context.MODE_PRIVATE);
        return new File(dbDir, filename);
    }

}
