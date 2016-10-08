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
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;


/**
 * Shows a dialog with a title, message, and ok/cancel button.
 * The activity or fragment which adds this dialog must implement the
 * ConfirmDialogListener interface.
 */
public class ConfirmDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + ConfirmDialogFragment.class.getSimpleName();
    private static final String EXTRA_ACTION_ID = "action_id";
    private static final String EXTRA_TITLE = "title";

    public interface ConfirmDialogListener {
        void onOk(int actionId);
    }

    public static ConfirmDialogFragment create(int actionId, String title) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle bundle = new Bundle(3);
        bundle.putInt(EXTRA_ACTION_ID, actionId);
        bundle.putString(EXTRA_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * @return a Dialog with a title, message, and ok/cancel buttons.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        Context context = getActivity();
        Bundle arguments = getArguments();
        final int actionId = arguments.getInt(EXTRA_ACTION_ID);

        OnClickListener positiveListener = (dialog, which) -> {
            Fragment parentFragment = getParentFragment();
            ConfirmDialogListener listener;
            if (parentFragment instanceof ConfirmDialogListener)
                listener = (ConfirmDialogListener) parentFragment;
            else listener = (ConfirmDialogListener) getActivity();
            listener.onOk(actionId);
        };

        return new AlertDialog.Builder(context)
                .setTitle(arguments.getString(EXTRA_TITLE))
                .setPositiveButton(android.R.string.ok, positiveListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
