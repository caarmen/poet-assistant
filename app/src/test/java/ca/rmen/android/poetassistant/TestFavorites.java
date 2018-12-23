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

package ca.rmen.android.poetassistant;

import androidx.room.Room;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class TestFavorites {
    @Test
    public void importTest() throws IOException {
        UserDb db =  Room.databaseBuilder(Environment.getApplication(),
                UserDb.class, "userdata.db")
                .allowMainThreadQueries()
                .addMigrations(UserDb.MIGRATION_1_2).build();
        Threading threading = new JunitThreading();
        Favorites favorites = new Favorites(threading, db.favoriteDao());
        Set<String> favoriteWords = favorites.getFavorites();
        assertEquals(0, favoriteWords.size());
        Uri uri = createFavoritesFile();
        shadowOf(Environment.getApplication().getContentResolver()).registerInputStream(uri, openInputStream(uri));
        favorites.importFavorites(Environment.getApplication(), uri);
        favoriteWords = favorites.getFavorites();
        assertEquals(2, favoriteWords.size());
        db.close();
    }

    private Uri createFavoritesFile() throws IOException {
        ShadowEnvironment.setExternalStorageState(android.os.Environment.MEDIA_MOUNTED);
        File file = new File(Environment.getApplication().getExternalFilesDir(null), "my-favorite-words.txt");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        writer.write("hello");
        writer.newLine();
        writer.write("tired");
        writer.newLine();
        writer.close();
        return Uri.fromFile(file);
    }

    private InputStream openInputStream(Uri uri) throws FileNotFoundException {
        if ("file".equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            return new FileInputStream(file);
        }
        return null;
    }

}
