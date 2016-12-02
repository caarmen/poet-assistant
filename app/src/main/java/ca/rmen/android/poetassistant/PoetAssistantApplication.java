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
package ca.rmen.android.poetassistant;

import android.app.Application;

public class PoetAssistantApplication extends Application {
    private DaggerHelper.AppComponent mAppComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        Theme.setThemeFromSettings(this);
        mAppComponent = DaggerDaggerHelper_AppComponent.builder()
                .appModule(new DaggerHelper.AppModule(this))
                .build();
    }

    DaggerHelper.AppComponent getAppComponent() {
        return mAppComponent;
    }
}
