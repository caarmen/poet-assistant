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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;


/**
 * Shows a dialog with a title, message, and ok button.
 * The activity or fragment which adds this dialog should implement the
 * WarningDialogListener interface.
 */
public class WarningNoSpaceDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + WarningNoSpaceDialogFragment.class.getSimpleName();

    public interface WarningNoSpaceDialogListener {
        void onWarningNoSpaceDialogDismissed();
    }

    /**
     * @return a Dialog with a title, message and ok button.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        Context context = getActivity();

        OnClickListener positiveListener = (dialog, which) -> notifyListener();

        DialogInterface.OnDismissListener dismissListener = dialog -> notifyListener();

        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.warning_no_space_title))
                .setMessage(context.getString(R.string.warning_no_space_message))
                .setPositiveButton(android.R.string.ok, positiveListener)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.setOnDismissListener(dismissListener);
        return dialog;
    }

    private void notifyListener() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof WarningNoSpaceDialogListener) {
            ((WarningNoSpaceDialogListener) parentFragment).onWarningNoSpaceDialogDismissed();
        } else if (getActivity() instanceof WarningNoSpaceDialogListener) {
            ((WarningNoSpaceDialogListener) getActivity()).onWarningNoSpaceDialogDismissed();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        notifyListener();
    }
}
