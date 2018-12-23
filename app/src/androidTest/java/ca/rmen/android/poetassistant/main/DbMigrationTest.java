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

package ca.rmen.android.poetassistant.main;


import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.room.testing.MigrationTestHelper;
import android.database.Cursor;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import ca.rmen.android.poetassistant.UserDb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DbMigrationTest {

    @Rule
    public MigrationTestHelper helper;

    public DbMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                UserDb.class.getName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase("userdata.db", 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.execSQL("INSERT INTO FAVORITE (WORD) VALUES ('hello')");
        db.execSQL("INSERT INTO FAVORITE (WORD) VALUES ('bye')");
        db.execSQL("INSERT INTO FAVORITE (WORD) VALUES ('bye')");
        db.execSQL("INSERT INTO SUGGESTION (WORD) VALUES ('cat')");
        db.execSQL("INSERT INTO SUGGESTION (WORD) VALUES ('dog')");

        // Check that the v1 database has duplicates in the FAVORITE table.
        // There won't be duplicates after migration.
        Cursor cursor = db.query("SELECT * from FAVORITE ORDER BY WORD");
        assertNotNull(cursor);
        assertEquals(3, cursor.getCount());
        cursor.close();
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate("userdata.db", 2, true, UserDb.MIGRATION_1_2);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        cursor = db.query("SELECT * from FAVORITE ORDER BY WORD");
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToPosition(0);
        assertEquals("bye", cursor.getString(0));
        cursor.moveToPosition(1);
        assertEquals("hello", cursor.getString(0));
        cursor.close();

        cursor = db.query("SELECT * from SUGGESTION ORDER BY WORD");
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());
        cursor.moveToPosition(0);
        assertEquals("cat", cursor.getString(0));
        cursor.moveToPosition(1);
        assertEquals("dog", cursor.getString(0));
        cursor.close();
    }
}

