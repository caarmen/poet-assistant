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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

/**
 * We use a custom dialog fragment because the app v7 preference dialog fragment
 * doesn't support using Spannables for the preference entry labels.  After rotation,
 * the span information is lost, because ListPreferenceDialogFragmentCompat saves only
 * the toString() representation of the entry labels in onSaveInstanceState().
 */
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
        // We don't persist the entry labels in onSaveInstanceState().  We ask
        // the Preference for them every time we want to display the dialog.
        builder.setSingleChoiceItems(getVoicePreference().getEntries(), mSelectedIndex, (dialog, which) -> {
            mSelectedIndex = which;
            dialog.dismiss();
        });
        builder.setPositiveButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        String value = getVoicePreference().getEntryValues()[mSelectedIndex].toString();
        VoicePreference preference = getVoicePreference();
        if (preference.callChangeListener(value)) {
            preference.setValue(value);
        }
    }

    private VoicePreference getVoicePreference() {
        return (VoicePreference) getPreference();
    }
}
