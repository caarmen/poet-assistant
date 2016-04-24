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

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ActivityLicenseBinding;


public class LicenseActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG + LicenseActivity.class.getSimpleName();
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_LICENSE_TEXT_ASSET_FILE = "license_text_asset_file";

    private ActivityLicenseBinding mBinding;

    public static void start(Context context, String title, String licenseText) {
        Intent intent = new Intent(context, LicenseActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LICENSE_TEXT_ASSET_FILE, licenseText);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_license);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.license_title);
        }

        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String licenseFile = intent.getStringExtra(EXTRA_LICENSE_TEXT_ASSET_FILE);
        mBinding.tvTitle.setText(title);
        loadLicenseFile(licenseFile);
    }

    private void loadLicenseFile(String fileName) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                BufferedReader reader = null;
                try {
                    InputStream is = getAssets().open(params[0]);
                    reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder builder = new StringBuilder();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        builder.append(line).append('\n');
                    }
                    return builder.toString();
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't read license file " + params[0] + ": " + e.getMessage(), e);
                    return "";
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.wtf(TAG, e.getMessage(), e);
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(String licenseText) {
                mBinding.tvLicenseText.setText(licenseText);
            }
        }.execute(fileName);

    }
}


