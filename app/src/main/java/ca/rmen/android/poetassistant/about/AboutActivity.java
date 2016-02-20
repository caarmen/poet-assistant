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

package ca.rmen.android.poetassistant.about;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;


public class AboutActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG + AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.about_title, getString(R.string.app_name)));
        }
        String versionName;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen
            throw new AssertionError(e);
        }
        String appVersionText = getString(R.string.about_app_version, getString(R.string.app_name), versionName);
        ((TextView) findViewById(R.id.txtVersion)).setText(appVersionText);
    }

    public void onClickAppLicense(@SuppressWarnings("UnusedParameters") View view) {
        LicenseActivity.start(this, getString(R.string.app_name), "LICENSE.txt");
    }

    public void onClickRhymerLicense(@SuppressWarnings("UnusedParameters") View view) {
        LicenseActivity.start(this, getString(R.string.about_license_rhyming_dictionary), "LICENSE-rhyming-dictionary.txt");
    }

    public void onClickThesaurusLicense(@SuppressWarnings("UnusedParameters") View view) {
        LicenseActivity.start(this, getString(R.string.about_license_thesaurus), "LICENSE-thesaurus-wordnet.txt");
    }

    public void onClickDictionaryLicense(@SuppressWarnings("UnusedParameters") View view) {
        LicenseActivity.start(this, getString(R.string.about_license_dictionary), "LICENSE-dictionary-wordnet.txt");
    }
}

