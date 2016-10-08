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

package ca.rmen.android.poetassistant.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.HtmlCompat;
import ca.rmen.android.poetassistant.R;


/**
 * Shows a dialog with a title, message, and ok button.
 */
public class HelpDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + HelpDialogFragment.class.getSimpleName();

    /**
     * @return a Dialog with a title, message and ok button.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        Context context = getActivity();

        // For now, we only show help about pattern searching
        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.pattern_help_title))
                .setMessage(HtmlCompat.fromHtml(context.getString(R.string.pattern_help_message)))
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

}
