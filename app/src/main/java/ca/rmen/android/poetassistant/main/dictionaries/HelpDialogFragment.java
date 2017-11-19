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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.compat.HtmlCompat;


/**
 * Shows a dialog with a title, message, and ok button.
 */
public class HelpDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + HelpDialogFragment.class.getSimpleName();
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_MESSAGE = "extra_message";


    public static HelpDialogFragment create(@StringRes int titleId, @StringRes int messageId) {
        HelpDialogFragment fragment = new HelpDialogFragment();
        Bundle arguments = new Bundle(2);
        arguments.putInt(EXTRA_TITLE, titleId);
        arguments.putInt(EXTRA_MESSAGE, messageId);
        fragment.setArguments(arguments);
        return fragment;
    }
    /**
     * @return a Dialog with a title, message and ok button.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        Context context = getContext();
        Bundle arguments = getArguments();
        if (context != null && arguments != null) {
            @StringRes int titleId = arguments.getInt(EXTRA_TITLE);
            @StringRes int messageId = arguments.getInt(EXTRA_MESSAGE);
            return new AlertDialog.Builder(context)
                    .setTitle(context.getString(titleId))
                    .setMessage(HtmlCompat.INSTANCE.fromHtml(context.getString(messageId)))
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
