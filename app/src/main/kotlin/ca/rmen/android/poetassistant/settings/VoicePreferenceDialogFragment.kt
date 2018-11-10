/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.settings

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat

/**
 * We use a custom dialog fragment because the app v7 preference dialog fragment
 * doesn't support using Spannables for the preference entry labels.  After rotation,
 * the span information is lost, because ListPreferenceDialogFragmentCompat saves only
 * the toString() representation of the entry labels in onSaveInstanceState().
 */
class VoicePreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    companion object {
        private const val EXTRA_SELECTED_INDEX = "selected_index"
        fun newInstance(key: String): VoicePreferenceDialogFragment {
            val args = Bundle(1)
            args.putString("key", key)
            val fragment = VoicePreferenceDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mSelectedIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSelectedIndex = if (savedInstanceState != null) {
            savedInstanceState.getInt(EXTRA_SELECTED_INDEX)
        } else {
            val listPreference = preference as ListPreference
            listPreference.findIndexOfValue(listPreference.value)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_SELECTED_INDEX, mSelectedIndex)
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        // We don't persist the entry labels in onSaveInstanceState().  We ask
        // the Preference for them every time we want to display the dialog.
        builder.setSingleChoiceItems(getVoicePreference().entries, mSelectedIndex) { dialog, which ->
            mSelectedIndex = which
            dialog.dismiss()
        }
        builder.setPositiveButton(null, null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val preference = getVoicePreference()
        val value = preference.entryValues[mSelectedIndex].toString()
        if (preference.callChangeListener(value)) {
            preference.value = value
        }
    }

    private fun getVoicePreference(): VoicePreference = preference as VoicePreference
}
