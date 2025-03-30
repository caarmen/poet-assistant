/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.di;

import android.app.Application;
import androidx.room.Room;

import javax.inject.Singleton;

import ca.rmen.android.poetassistant.UserDb;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.testing.TestInstallIn;

@TestInstallIn(components = {SingletonComponent.class}, replaces = {DbModule.class})
@Module
public class TestDbModule {

    @Provides
    @Singleton
    UserDb providesUserDb(Application application) {
        return Room.inMemoryDatabaseBuilder(application,
                UserDb.class).allowMainThreadQueries().build();
    }
}
