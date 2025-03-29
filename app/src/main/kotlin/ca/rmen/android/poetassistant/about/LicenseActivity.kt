/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.theme.AppTheme

class LicenseActivity : AppCompatActivity() {
    companion object {
        private val TAG = Constants.TAG + LicenseActivity::class.java.simpleName
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_LICENSE_TEXT_ASSET_FILE = "license_text_asset_file"

        fun start(context: Context, title: String, licenseText: String) {
            context.startActivity(
                Intent(context, LicenseActivity::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_LICENSE_TEXT_ASSET_FILE, licenseText)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val licenseViewModel: LicenseViewModel by viewModels()
        val title = intent.getStringExtra(EXTRA_TITLE)!!
        val licenseFile = intent.getStringExtra(EXTRA_LICENSE_TEXT_ASSET_FILE)!!
        licenseViewModel.readLicenseText(licenseFile)
        setContent {
            val licenseText = licenseViewModel.licenseText.collectAsState()
            AppTheme {
                LicenseScreen(
                    title = title,
                    licenseText = licenseText.value,
                    onBack = onBackPressedDispatcher::onBackPressed
                )
            }
        }
    }

}
