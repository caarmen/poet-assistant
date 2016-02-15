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

package ca.rmen.android.poetassistant.main.filechooser;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import ca.rmen.android.poetassistant.Constants;

/**
 * Dialog to enter text
 * The calling activity or fragment must implement the {@link InputDialogFragment.InputDialogListener} interface.
 */
public class InputDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + InputDialogFragment.class.getSimpleName();
    private static final String EXTRA_ACTION_ID = "action_id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_DEFAULT_VALUE = "default_value";

    public interface InputDialogListener {
        void onDialogInput(int actionId, String text);
    }

    /**
     * Show a visible dialog fragment to choose a folder or file
     */
    public static void show(InputDialogListener listener, String title, String message, String defaultValue, int actionId) {
        Log.d(TAG, "show() called with: " + "listener = [" + listener + "], title = [" + title + "], message = [" + message + "], defaultValue = [" + defaultValue + "], actionId = [" + actionId + "]");
        Bundle arguments = new Bundle(3);
        arguments.putInt(EXTRA_ACTION_ID, actionId);
        arguments.putString(EXTRA_TITLE, title);
        arguments.putString(EXTRA_MESSAGE, message);
        arguments.putString(EXTRA_DEFAULT_VALUE, defaultValue);
        InputDialogFragment result = new InputDialogFragment();
        result.setArguments(arguments);
        if (listener instanceof FragmentActivity)
            result.show(((FragmentActivity) listener).getSupportFragmentManager(), InputDialogFragment.class.getSimpleName());
        else
            result.show(((Fragment) listener).getChildFragmentManager(), InputDialogFragment.class.getSimpleName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(TAG, "onSavedInstanceState, outState=" + outState);
    }

    /**
     * @return a Dialog to input text
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);

        Bundle arguments = getArguments();
        final int actionId = arguments.getInt(EXTRA_ACTION_ID);
        final String defaultValue = arguments.getString(EXTRA_DEFAULT_VALUE);
        final String title = arguments.getString(EXTRA_TITLE);
        final String message = arguments.getString(EXTRA_MESSAGE);
        final EditText editText = new EditText(getActivity());
        editText.setText(defaultValue);

        final Context context = getActivity();

        // When the user taps the positive button, notify the listener.
        OnClickListener positiveListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputDialogListener listener = getListener();
                if (listener == null)
                    Log.w(TAG, "User clicked on dialog after it was detached from activity. Monkey?");
                else
                    listener.onDialogInput(actionId, editText.getText().toString());
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(editText)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, positiveListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        return dialog;
    }

    private InputDialogListener getListener() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof InputDialogListener)
            return (InputDialogListener) fragment;
        return (InputDialogListener) getActivity();
    }

}
