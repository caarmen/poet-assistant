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
package ca.rmen.android.poetassistant;

import android.app.Application;

import ca.rmen.android.poetassistant.dagger.AppComponent;
import ca.rmen.android.poetassistant.dagger.AppModule;
import ca.rmen.android.poetassistant.dagger.DaggerAppComponent;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class PoetAssistantApplication extends Application {
    private AppComponent mAppComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        Theme.setThemeFromSettings(SettingsPrefs.get(this));
    }

    public AppComponent getAppComponent() {
        if (mAppComponent == null) {
            mAppComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }
        return mAppComponent;
    }
}
