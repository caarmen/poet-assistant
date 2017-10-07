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

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import ca.rmen.android.poetassistant.main.dictionaries.search.Suggestion;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionDao;

// https://medium.com/google-developers/7-steps-to-room-27a5fe5f99b2
@Database(entities = {Favorite.class, Suggestion.class}, version = 2)
public abstract class UserDb extends RoomDatabase {

    public abstract FavoriteDao favoriteDao();

    public abstract SuggestionDao suggestionDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `FAVORITE_TEMP` AS SELECT * FROM `FAVORITE`");
            database.execSQL("CREATE TABLE `SUGGESTION_TEMP` AS SELECT * FROM `SUGGESTION`");
            database.execSQL("DROP TABLE `FAVORITE`");
            database.execSQL("DROP TABLE `SUGGESTION`");
            database.execSQL("CREATE TABLE `FAVORITE` (`WORD` TEXT NOT NULL, PRIMARY KEY(`WORD`))");
            database.execSQL("CREATE TABLE `SUGGESTION` (`WORD` TEXT NOT NULL, PRIMARY KEY(`WORD`))");
            database.execSQL("CREATE UNIQUE INDEX `index_FAVORITE_WORD` ON `FAVORITE` (`WORD`)");
            database.execSQL("CREATE UNIQUE INDEX `index_SUGGESTION_WORD` ON `SUGGESTION` (`WORD`)");
            database.execSQL("INSERT OR IGNORE INTO `FAVORITE` SELECT * FROM `FAVORITE_TEMP`");
            database.execSQL("INSERT OR IGNORE INTO `SUGGESTION` SELECT * FROM `SUGGESTION_TEMP`");
            database.execSQL("DROP TABLE `FAVORITE_TEMP`");
            database.execSQL("DROP TABLE `SUGGESTION_TEMP`");
        }
    };
}
