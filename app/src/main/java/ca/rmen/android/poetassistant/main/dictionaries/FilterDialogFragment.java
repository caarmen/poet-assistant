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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.InputDialogEditTextBinding;
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper;


/**
 * Shows a dialog prompting the user enter a word to filter search results.
 * The activity or fragment which adds this dialog must implement the
 * FilterDialogListener interface.
 */
public class FilterDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + FilterDialogFragment.class.getSimpleName();
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_TEXT = "text";

    interface FilterDialogListener {
        void onFilterSubmitted(String input);
    }

    public static FilterDialogFragment newInstance(String message, String text) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle bundle = new Bundle(2);
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
        if (context != null) {
            LayoutInflater themedLayoutInflater = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.AppAlertDialog));
            final InputDialogEditTextBinding binding = DataBindingUtil.inflate(themedLayoutInflater,
                    R.layout.input_dialog_edit_text,
                    null,
                    false);
            Bundle arguments = getArguments();
            if (arguments != null) {
                binding.edit.setText(arguments.getString(EXTRA_TEXT));

                OnClickListener positiveListener = (dialog, which) -> {
                    FilterDialogListener listener;
                    Fragment parentFragment = getParentFragment();
                    if (parentFragment instanceof FilterDialogListener)
                        listener = (FilterDialogListener) parentFragment;
                    else listener = (FilterDialogListener) getActivity();
                    listener.onFilterSubmitted(binding.edit.getText().toString());
                };

                final Dialog dialog = new AlertDialog.Builder(context)
                        .setView(binding.getRoot())
                        .setTitle(R.string.filter_title)
                        .setMessage(arguments.getString(EXTRA_MESSAGE))
                        .setPositiveButton(android.R.string.ok, positiveListener)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                binding.edit.setOnFocusChangeListener((v, hasFocus) -> {
                    Window window = dialog.getWindow();
                    if (window != null) {
                        if (hasFocus) {
                            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        } else {
                            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        }
                    }
                });

                return dialog;
            }
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        AppBarLayoutHelper.forceExpandAppBarLayout(getActivity());
    }
}
