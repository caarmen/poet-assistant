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

package ca.rmen.android.poetassistant.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

public class VoicePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String EXTRA_SELECTED_INDEX = "selected_index";

    private int mSelectedIndex;

    public static VoicePreferenceDialogFragment newInstance(String key) {
        Bundle args = new Bundle(1);
        args.putString("key", key);
        VoicePreferenceDialogFragment fragment = new VoicePreferenceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_INDEX);
        } else {
            ListPreference preference = (ListPreference) getPreference();
            mSelectedIndex = preference.findIndexOfValue(preference.getValue());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_INDEX, mSelectedIndex);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        ListPreference preference = (ListPreference) getPreference();
        builder.setSingleChoiceItems(preference.getEntries(), mSelectedIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSelectedIndex = which;
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        ListPreference preference = (ListPreference) getPreference();
        String value = preference.getEntryValues()[mSelectedIndex].toString();
        if(preference.callChangeListener(value)) {
            preference.setValue(value);
        }
    }
}
