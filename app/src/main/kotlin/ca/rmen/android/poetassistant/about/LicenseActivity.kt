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

package ca.rmen.android.poetassistant.about

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.databinding.ActivityLicenseBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LicenseActivity : AppCompatActivity() {
    companion object {
        private val TAG = Constants.TAG + LicenseActivity::class.java.simpleName
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_LICENSE_TEXT_ASSET_FILE = "license_text_asset_file"

        fun start(context: Context, title: String, licenseText: String) {
            context.startActivity(Intent(context, LicenseActivity::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_LICENSE_TEXT_ASSET_FILE, licenseText))
        }
    }

    private lateinit var mBinding: ActivityLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_license)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.license_title)
        }

        val title = intent.getStringExtra(EXTRA_TITLE)
        val licenseFile = intent.getStringExtra(EXTRA_LICENSE_TEXT_ASSET_FILE)
        mBinding.tvTitle.text = title
        val threading = DaggerHelper.getMainScreenComponent(this).getThreading()
        threading.execute({ readFile(licenseFile) },
                { mBinding.tvLicenseText.text = it })
    }

    @WorkerThread
    private fun readFile(fileName: String): String {

        try {
            BufferedReader(InputStreamReader(assets.open(fileName))).use {
                val builder = StringBuilder()
                it.forEachLine { line ->
                    builder.append(line).append('\n')
                }
                return builder.toString()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't read license file $fileName: ${e.message}", e)
            return ""
        }
    }
}
