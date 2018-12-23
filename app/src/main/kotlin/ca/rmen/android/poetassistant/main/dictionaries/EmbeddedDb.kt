/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.annotation.VisibleForTesting
import ca.rmen.android.poetassistant.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class EmbeddedDb(val context: Context) {
    companion object {
        private val TAG = Constants.TAG + EmbeddedDb::class.java.simpleName
        private const val MAX_QUERY_ARGUMENT_COUNT = 500
        private const val DB_NAME = "poet_assistant"
        private const val DB_VERSION = 1
        private const val MAX_DB_RESTORE_ATTEMPTS = 3
        /**
         * SQLite doesn't support unlimited number of query arguments.  If we have to do a mega huge
         * query, for example with a clause like `WHERE word in (?, ?, ?, ?, ?, ... ?)', with thousands
         * of `?' args, we will need to perform multiple queries each with a clause of a subset of the args,
         * and aggregate the results.
         * @param totalArgCount the total number of arguments we have in our mega query.
         * @return the number of smaller queries which must be done.
         */
        fun getQueryCount(totalArgCount: Int): Int {
            return getQueryCount(totalArgCount, MAX_QUERY_ARGUMENT_COUNT)
        }

        /**
         * @param allArgs the total set of arguments we need to process
         * @return the arguments in the query at index queryNumber (0-based index).
         * @see #getQueryCount(int)
         */
        fun getArgsInQuery(allArgs: Array<String>, queryNumber: Int): Array<String> {
            return getArgsInQuery(allArgs, queryNumber, MAX_QUERY_ARGUMENT_COUNT)
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
        fun getQueryCount(totalArgCount: Int, maxArgCountPerQuery: Int): Int {
            return Math.ceil(totalArgCount.toDouble() / maxArgCountPerQuery).toInt()
        }

        /**
         * @return the number of arguments in the query at index queryNumber (0-based index).
         * @see #getQueryCount(int, int)
         */
        @VisibleForTesting
        fun getArgCountInQuery(totalArgCount: Int, maxArgCountPerQuery: Int, queryNumber: Int): Int {
            var argCountThisQuery = maxArgCountPerQuery
            val queryCount = getQueryCount(totalArgCount, maxArgCountPerQuery)
            if (queryNumber == queryCount - 1) argCountThisQuery = totalArgCount % maxArgCountPerQuery
            return argCountThisQuery
        }

        /**
         * @param allArgs the total set of arguments we need to process
         * @return the arguments in the query at index queryNumber (0-based index).
         * @see #getQueryCount(int, int)
         */
        @VisibleForTesting
        fun getArgsInQuery(allArgs: Array<String>, queryNumber: Int, maxArgCount: Int): Array<String> {
            val argCountThisQuery = getArgCountInQuery(allArgs.size, maxArgCount, queryNumber)
            return Array(argCountThisQuery) { i ->
                allArgs[queryNumber * maxArgCount + i]
            }
        }

        fun buildInClause(size: Int): String {
            val builder = StringBuilder(size * 2 + 1)
            builder.append('(')
            for (i in 0 until size) {
                builder.append('?')
                if (i != size - 1) builder.append(',')
            }
            builder.append(')')
            return builder.toString()
        }
    }

    private var mDb: SQLiteDatabase? = null
    private val mLock = Object()
    private var mDbRestoreAttemptCount = 0

    fun isLoaded(): Boolean = getDb() != null

    private fun getDb(): SQLiteDatabase? {
        open()
        return mDb
    }

    @SuppressLint("Recycle") // The caller must close the Cursor
    fun query(table: String, projection: Array<String>, selection: String, selectionArgs: Array<String>): Cursor? {
        val db = getDb() ?: return null
        return try {
            db.query(table, projection, selection, selectionArgs,
                    null, null, null)
        } catch (e: SQLiteDatabaseCorruptException) {
            handleDbCorruptException(e)
            null
        }
    }

    @SuppressLint("Recycle") // The caller must close the Cursor
    fun query(distinct: Boolean, table: String, projection: Array<String>, selection: String, selectionArgs: Array<String>,
              orderBy: String?,
              limit: String?): Cursor? {
        val db = getDb() ?: return null
        return try {
            db.query(distinct, table, projection, selection, selectionArgs,
                    null, null,
                    orderBy, limit)
        } catch (e: SQLiteDatabaseCorruptException) {
            handleDbCorruptException(e)
            null
        }
    }

    fun close() {
        mDb?.let {
            it.close()
            mDb = null
        }
    }

    /**
     * Issue #44 (https://github.com/caarmen/poet-assistant/issues/44):
     * Some users reported an {@link SQLiteDatabaseCorruptException}.  If this happens, we try to
     * recopy the db, up to a limited number of times, before giving up.
     */
    private fun handleDbCorruptException(e: SQLiteDatabaseCorruptException) {
        Log.v(TAG, "Error querying db ${e.message}", e)
        if (mDbRestoreAttemptCount >= MAX_DB_RESTORE_ATTEMPTS) {
            // Make the app crash.  We'll hopefully see if this is actually happening, by receiving crash reports.
            throw RuntimeException("Tried to recover the db $mDbRestoreAttemptCount times. Giving up :(", e)
        }
        synchronized(mLock) {
            deleteDb(DB_VERSION)
            mDb = null
            open()
            mDbRestoreAttemptCount++
        }
    }

    private fun open() {
        synchronized(mLock) {
            if (mDb == null) {
                Log.v(TAG, "Open db $DB_NAME:$DB_VERSION")
                copyDb()
                val dbFile = getDbFileName(DB_VERSION)
                val dbPath = File(context.getDir("databases", Context.MODE_PRIVATE), dbFile)
                try {
                    val flags = SQLiteDatabase.OPEN_READONLY
                    mDb = SQLiteDatabase.openDatabase(dbPath.absolutePath, null, flags)
                } catch (e: SQLiteException) {
                    Log.w(TAG, "Could not open database $DB_NAME:$DB_VERSION: ${e.message}", e)
                }
            }
        }
    }

    private fun copyDb() {
        val dbFileName = getDbFileName(DB_VERSION)
        val dbPath = getDbFile(dbFileName)
        if (!dbPath.exists()) {
            Log.v(TAG, "$dbPath not found")
            for (i in 0 until DB_VERSION) {
                deleteDb(i)
            }
            deleteOldDbs("rhymes")
            deleteOldDbs("thesaurus")
            deleteOldDbs("dictionary")
            var input: InputStream? = null
            var output: FileOutputStream? = null
            try {
                input = context.assets.open(dbFileName)
                output = FileOutputStream(dbPath)
                val buffer = ByteArray(1024)
                var read = input.read(buffer)
                while (read > 0) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }
                Log.v(TAG, "write $dbPath")
            } catch (e: IOException) {
                Log.e(TAG, "Error writing to $dbPath:${e.message}", e)
                deleteDb(DB_VERSION)
            } finally {
                try {
                    input?.close()
                    output?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Couldn't close stream", e)
                }
            }
        }
    }

    private fun deleteOldDbs(name: String) {
        // the max version of all the separate dbs
        // (rhymes, thesaurus, dictionary) was 2.
        val maxVersion = 2
        for (i in 0..maxVersion) {
            val dbFileName: String = if (i == 1) "$name.db"
            else "$name$i.db"
            val dbPath = getDbFile(dbFileName)
            if (dbPath.exists()) {
                val deleted = dbPath.delete()
                Log.v(TAG, "deletion of ${dbPath.absolutePath}: $deleted")
            }

        }
    }

    private fun getDbFileName(version: Int): String {
        if (version == 1) return "$DB_NAME.db"
        return "$DB_NAME$version.db"
    }

    private fun deleteDb(version: Int) {
        val dbFileName = getDbFileName(version)
        val dbPath = getDbFile(dbFileName)
        if (dbPath.exists()) {
            val deleted = dbPath.delete()
            Log.v(TAG, "deleteDb: deletion of ${dbPath.absolutePath}: $deleted")
        }
    }

    private fun getDbFile(filename: String): File {
        val dbDir = context.getDir("databases", Context.MODE_PRIVATE)
        return File(dbDir, filename)
    }
}
