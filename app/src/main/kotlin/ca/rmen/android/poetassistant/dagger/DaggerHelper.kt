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

package ca.rmen.android.poetassistant.dagger

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting

object DaggerHelper {
    private var sAppComponent: AppComponent? = null

    fun getMainScreenComponent(context: Context): AppComponent.MainScreenComponent
            = getAppComponent(context).getMainScreenComponent()

    fun getMainScreenComponent(application: Application): AppComponent.MainScreenComponent
            = getAppComponent(application).getMainScreenComponent()

    fun getSettingsComponent(context: Context): AppComponent.SettingsComponent
            = getAppComponent(context).getSettingsComponent()

    fun getSettingsComponent(application: Application): AppComponent.SettingsComponent
            = getAppComponent(application).getSettingsComponent()

    fun getWotdComponent(context: Context): AppComponent.WotdComponent
            = getAppComponent(context).getWotdComponent()

    fun getWotdComponent(application: Application): AppComponent.WotdComponent
            = getAppComponent(application).getWotdComponent()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getAppComponent(context: Context): AppComponent
            = getAppComponent(context.applicationContext as Application)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setAppComponent(appComponent: AppComponent) {
        sAppComponent = appComponent
    }

    private fun getAppComponent(application: Application): AppComponent {
        if (sAppComponent == null) {
            sAppComponent = DaggerAppComponent.builder()
                    .appModule(AppModule(application))
                    .dbModule(DbModule(application))
                    .build()
        }
        return sAppComponent!!
    }

}
