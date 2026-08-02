/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant

import android.net.Uri
import androidx.room.Room
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowEnvironment
import org.robolectric.Shadows.shadowOf
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter

@RunWith(RobolectricTestRunner::class)
class TestFavorites {

    @Test
    @Throws(Exception::class)
    fun importTest() = runTest {
        val db = Room.databaseBuilder(Environment.getApplication(),
                UserDb::class.java, "userdata.db")
                .allowMainThreadQueries()
                .addMigrations(UserDb.MIGRATION_1_2)
                .build()
        val favorites = Favorites(StandardTestDispatcher(testScheduler), db.favoriteDao())
        var favoriteWords = favorites.getFavorites()
        assertEquals(0, favoriteWords.size)
        val uri = createFavoritesFile()
        shadowOf(Environment.getApplication().contentResolver).registerInputStream(uri, openInputStream(uri))
        favorites.importFavorites(Environment.getApplication(), uri)
        favoriteWords = favorites.getFavorites()
        assertEquals(2, favoriteWords.size)
        db.close()
    }

    @Throws(Exception::class)
    private fun createFavoritesFile(): Uri {
        ShadowEnvironment.setExternalStorageState(android.os.Environment.MEDIA_MOUNTED)
        val file = File(Environment.getApplication().getExternalFilesDir(null), "my-favorite-words.txt")
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file)))
        writer.write("hello")
        writer.newLine()
        writer.write("tired")
        writer.newLine()
        writer.close()
        return Uri.fromFile(file)
    }

    @Throws(Exception::class)
    private fun openInputStream(uri: Uri): InputStream? {
        if ("file" == uri.scheme) {
            val file = File(uri.path!!)
            return FileInputStream(file)
        }
        return null
    }
}
