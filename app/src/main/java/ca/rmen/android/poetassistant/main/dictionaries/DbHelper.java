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
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.android.poetassistant.Constants;

public class DbHelper {
    private static final String TAG = Constants.TAG + DbHelper.class.getSimpleName();
    private static final int MAX_QUERY_ARGUMENT_COUNT = 500;

    private final Context mContext;
    private static final String DB_NAME = "poet_assistant";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase mDb;
    private final Object mLock = new Object();

    public DbHelper(Context context) {
        mContext = context;
    }

    public SQLiteDatabase getDb() {
        open();
        return mDb;
    }

    /**
     * SQLite doesn't support unlimited number of query arguments.  If we have to do a mega huge
     * query, for example with a clause like `WHERE word in (?, ?, ?, ?, ?, ... ?)', with thousands
     * of `?' args, we will need to perform multiple queries each with a clause of a subset of the args,
     * and aggregate the results.
     * @param totalArgCount the total number of arguments we have in our mega query.
     * @return the number of smaller queries which must be done.
     */
    public static int getQueryCount(int totalArgCount) {
        return getQueryCount(totalArgCount, MAX_QUERY_ARGUMENT_COUNT);
    }

    /**
     * @param allArgs the total set of arguments we need to process
     * @return the arguments in the query at index queryNumber (0-based index).
     * @see #getQueryCount(int)
     */
    public static String[] getArgsInQuery(String[] allArgs, int queryNumber) {
        return getArgsInQuery(allArgs, queryNumber, MAX_QUERY_ARGUMENT_COUNT);
    }

    /**
     * SQLite doesn't support unlimited number of query arguments.  If we have to do a mega huge
     * query, for example with a clause like `WHERE word in (?, ?, ?, ?, ?, ... ?)', with thousands
     * of `?' args, we will need to perform multiple queries each with a clause of a subset of the args,
     * and aggregate the results.
     * @param totalArgCount the total number of arguments we have in our mega query.
     * @param maxArgCountPerQuery the total number of arguments a single query can allow.
     * @return the number of smaller queries which must be done.
     */
    @VisibleForTesting
    static int getQueryCount(int totalArgCount, int maxArgCountPerQuery) {
        return (int) Math.ceil((double) totalArgCount / maxArgCountPerQuery);
    }

    /**
     * @return the number of arguments in the query at index queryNumber (0-based index).
     * @see #getQueryCount(int, int)
     */
    @VisibleForTesting
    static int getArgCountInQuery(int totalArgCount, int maxArgCountPerQuery, int queryNumber) {
        int argCountThisQuery = maxArgCountPerQuery;
        int queryCount = getQueryCount(totalArgCount, maxArgCountPerQuery);
        if (queryNumber == queryCount - 1) argCountThisQuery = totalArgCount % maxArgCountPerQuery;
        return argCountThisQuery;
    }

    /**
     * @param allArgs the total set of arguments we need to process
     * @return the arguments in the query at index queryNumber (0-based index).
     * @see #getQueryCount(int, int)
     */
    @VisibleForTesting
    static String[] getArgsInQuery(String[] allArgs, int queryNumber, int maxArgCount) {
        int argCountThisQuery = getArgCountInQuery(allArgs.length, maxArgCount, queryNumber);
        String[] argsThisQuery = new String[argCountThisQuery];
        System.arraycopy(allArgs, queryNumber * maxArgCount, argsThisQuery, 0, argCountThisQuery);
        return argsThisQuery;
    }

    private void open() {
        synchronized (mLock) {
            if (mDb == null) {
                Log.v(TAG, "Open db " + DB_NAME + ":" + DB_VERSION);
                copyDb();
                String dbFile = getDbFileName(DB_VERSION);
                File dbPath = new File(mContext.getDir("databases", Context.MODE_PRIVATE), dbFile);
                try {
                    int flags = SQLiteDatabase.OPEN_READONLY;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        // http://stackoverflow.com/questions/2528489/no-such-table-android-metadata-whats-the-problem
                        flags |= SQLiteDatabase.NO_LOCALIZED_COLLATORS;
                    }
                    mDb = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, flags);
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
