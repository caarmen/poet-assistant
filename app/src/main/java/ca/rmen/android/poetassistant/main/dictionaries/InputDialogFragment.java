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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.InputDialogEditTextBinding;


/**
 * Shows a dialog with a title, message, edit text, and ok/cancel button.
 * The activity or fragment which adds this dialog must implement the
 * InputDialogListener interface.
 */
public class InputDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + InputDialogFragment.class.getSimpleName();
    private static final String EXTRA_ACTION_ID = "action_id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_TEXT = "text";

    public interface InputDialogListener {
        void onInputSubmitted(int actionId, String input);
    }

    public static InputDialogFragment create(int actionId, String title, String message, String text) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle bundle = new Bundle(3);
        bundle.putInt(EXTRA_ACTION_ID, actionId);
        bundle.putString(EXTRA_TITLE, title);
        bundle.putString(EXTRA_MESSAGE, message);
        bundle.putString(EXTRA_TEXT, text);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * @return a Dialog with a title, message, an edit text, and ok/cancel buttons.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        Context context = getActivity();
        final InputDialogEditTextBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivity()),
                R.layout.input_dialog_edit_text,
                null,
                false);
        Bundle arguments = getArguments();
        final int actionId = arguments.getInt(EXTRA_ACTION_ID);
        binding.edit.setText(arguments.getString(EXTRA_TEXT));

        OnClickListener positiveListener = (dialog, which) -> {
            InputDialogListener listener;
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof InputDialogListener)
                listener = (InputDialogListener) parentFragment;
            else listener = (InputDialogListener) getActivity();
            listener.onInputSubmitted(actionId, binding.edit.getText().toString());
        };

        final Dialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setTitle(arguments.getString(EXTRA_TITLE))
                .setMessage(arguments.getString(EXTRA_MESSAGE))
                .setPositiveButton(android.R.string.ok, positiveListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        binding.edit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });

        return dialog;
    }
}
