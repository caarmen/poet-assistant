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
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.VectorCompat
import ca.rmen.android.poetassistant.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.about_title, getString(R.string.app_name))
        }
        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            // Should never happen
            ""
        }
        val appVersionText = getString(R.string.about_app_version, getString(R.string.app_name), versionName)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        mBinding.txtVersion.text = appVersionText
        hackSetIcons()
    }

    @SuppressWarnings("unused")
    fun onClickAppLicense(@Suppress("UNUSED_PARAMETER") v: View) {
        LicenseActivity.start(this, getString(R.string.app_name), "LICENSE.txt")
    }

    @SuppressWarnings("unused")
    fun onClickRhymerLicense(@Suppress("UNUSED_PARAMETER") v: View) {
        LicenseActivity.start(this, getString(R.string.about_license_rhyming_dictionary), "LICENSE-rhyming-dictionary.txt")
    }

    @SuppressWarnings("unused")
    fun onClickThesaurusLicense(@Suppress("UNUSED_PARAMETER") v: View) {
        LicenseActivity.start(this, getString(R.string.about_license_thesaurus), "LICENSE-thesaurus-wordnet.txt")
    }

    @SuppressWarnings("unused")
    fun onClickDictionaryLicense(@Suppress("UNUSED_PARAMETER") v: View) {
        LicenseActivity.start(this, getString(R.string.about_license_dictionary), "LICENSE-dictionary-wordnet.txt")
    }

    @SuppressWarnings("unused")
    fun onClickGoogleNgramDatasetLicense(@Suppress("UNUSED_PARAMETER") v: View) {
        LicenseActivity.start(this, getString(R.string.about_license_google_ngram_dataset), "LICENSE-google-ngram-dataset.txt")
    }

    /**
     * The support library 23.3.0 dropped support for using vector drawables in the
     * "drawableLeft" attribute of TextViews.  We set them programmatically here.
     */
    private fun hackSetIcons() {
        hackSetIcon(mBinding.tvSourceCode, R.drawable.ic_source_code)
        hackSetIcon(mBinding.tvBugReport, R.drawable.ic_bug_report_24dp)
        hackSetIcon(mBinding.tvRate, R.drawable.ic_rate)
        hackSetIcon(mBinding.tvLegal, R.drawable.ic_legal)
        hackSetIcon(mBinding.tvPrivacyPolicy, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvPoetAssistantLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvRhymerLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvThesaurusLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvDictionaryLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvGoogleNgramDatasetLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvSupportLibLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvDaggerLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvKotlinLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvPrefsLicense, R.drawable.ic_bullet)
        hackSetIcon(mBinding.tvStemmerLicense, R.drawable.ic_bullet)
    }

    private fun hackSetIcon(textView: TextView, @DrawableRes iconRes: Int) {
        VectorCompat.setCompoundVectorDrawables(this, textView, iconRes, 0, 0, 0)
    }

}
