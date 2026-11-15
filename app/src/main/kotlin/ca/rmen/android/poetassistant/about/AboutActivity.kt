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

package ca.rmen.android.poetassistant.about

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.theme.AppTheme

/**
 * The about screen: a thin Compose host for [AboutScreen].
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            // Should never happen
            ""
        }
        val appVersionText = getString(R.string.about_app_version, getString(R.string.app_name), versionName)
        setContent {
            AppTheme {
                AboutScreen(
                    appVersionText = appVersionText,
                    onNavigateUp = { onSupportNavigateUp() },
                    onOpenLicense = { title, licenseAssetFile ->
                        LicenseActivity.start(this, title, licenseAssetFile)
                    },
                )
            }
        }
    }

    /**
     * Honors the manifest parentActivityName for up navigation.
     */
    override fun onSupportNavigateUp(): Boolean {
        NavUtils.navigateUpFromSameTask(this)
        return true
    }
}
