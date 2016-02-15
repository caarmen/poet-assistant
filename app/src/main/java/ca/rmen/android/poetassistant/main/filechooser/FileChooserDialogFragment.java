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
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;

/**
 * Dialog to pick a file (or folder)
 * The calling activity or fragment must implement the {@link FileChooserDialogFragment.FileChooserDialogListener} interface.
 */
public class FileChooserDialogFragment extends DialogFragment {

    private static final String TAG = Constants.TAG + FileChooserDialogFragment.class.getSimpleName();
    private static final String EXTRA_ACTION_ID = "action_id";

    /**
     * Optional.  Must be a folder. If provided, the file browser will open at this folder. If
     * the value is not a folder, the parent folder will be used.
     */
    private static final String EXTRA_FILE_CHOOSER_INITIAL_FOLDER = "initial_folder";

    /**
     * Optional. If true, only folders will appear in the chooser.
     */
    private static final String EXTRA_FILE_CHOOSER_FOLDERS_ONLY = "folders_only";

    private File mSelectedFile = null;

    public interface FileChooserDialogListener {
        void onFileSelected(int actionId, File file);

        void onDismiss(int actionId);
    }

    /**
     * Show a visible dialog fragment to choose a folder or file
     */
    public static void show(FragmentActivity activity, File initialFolder, boolean foldersOnly, int actionId) {
        Log.d(TAG, "show() called with: " + "activity = [" + activity + "], initialFolder = [" + initialFolder + "], foldersOnly = [" + foldersOnly + "], actionId = [" + actionId + "]");
        FileChooserDialogFragment result = create(initialFolder, foldersOnly, actionId);
        result.show(activity.getSupportFragmentManager(), FileChooserDialogFragment.class.getSimpleName());
    }

    /**
     * Show a visible dialog fragment to choose a folder or file
     */
    public static void show(Fragment fragment, File initialFolder, @SuppressWarnings("SameParameterValue") boolean foldersOnly, int actionId) {
        Log.d(TAG, "show() called with: " + "fragment = [" + fragment + "], initialFolder = [" + initialFolder + "], foldersOnly = [" + foldersOnly + "], actionId = [" + actionId + "]");
        FileChooserDialogFragment result = create(initialFolder, foldersOnly, actionId);
        result.show(fragment.getChildFragmentManager(), FileChooserDialogFragment.class.getSimpleName());
    }

    private static FileChooserDialogFragment create(File initialFolder, boolean foldersOnly, int actionId) {
        Bundle arguments = new Bundle(3);
        arguments.putInt(EXTRA_ACTION_ID, actionId);
        if (initialFolder != null)
            arguments.putSerializable(FileChooserDialogFragment.EXTRA_FILE_CHOOSER_INITIAL_FOLDER, initialFolder);
        arguments.putBoolean(FileChooserDialogFragment.EXTRA_FILE_CHOOSER_FOLDERS_ONLY, foldersOnly);
        FileChooserDialogFragment result = new FileChooserDialogFragment();
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_FILE_CHOOSER_INITIAL_FOLDER, mSelectedFile);
        super.onSaveInstanceState(outState);
        Log.v(TAG, "onSavedInstanceState, outState=" + outState);
    }

    /**
     * Returns the initial folder to open in the file chooser dialog.
     * First we look in the savedInstanceState, if any, to see if the user selected a folder before
     * rotating the screen.
     * Then we look in the arguments given when creating this dialog.
     * Then we fall back to the SD card folder.
     */
    private File getInitialFolder(Bundle savedInstanceState) {
        File initialFolder = null;

        if (savedInstanceState != null) {
            initialFolder = (File) savedInstanceState.getSerializable(EXTRA_FILE_CHOOSER_INITIAL_FOLDER);
        }

        if (initialFolder == null) {
            initialFolder = (File) getArguments().getSerializable(EXTRA_FILE_CHOOSER_INITIAL_FOLDER);
        }

        if (initialFolder == null) {
            initialFolder = Environment.getExternalStorageDirectory();
        }

        // We need a folder to start with.
        if (!initialFolder.isDirectory()) {
            initialFolder = initialFolder.getParentFile();
        }
        return initialFolder;
    }

    /**
     * @return a Dialog to browse files and folders
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);

        Bundle arguments = getArguments();
        final int actionId = arguments.getInt(EXTRA_ACTION_ID);
        boolean foldersOnly = arguments.getBoolean(FileChooserDialogFragment.EXTRA_FILE_CHOOSER_FOLDERS_ONLY);

        mSelectedFile = getInitialFolder(savedInstanceState);

        final Context context = getActivity();
        final FileAdapter adapter = new FileAdapter(context, mSelectedFile, foldersOnly);

        // Save the file the user selected. Reload the dialog with the new folder
        // contents.
        OnClickListener fileSelectionListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectedFile = adapter.getItem(i);
                if (mSelectedFile.isDirectory()) {
                    adapter.load(mSelectedFile);
                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    dialog.setTitle(FileChooser.getFullDisplayName(context, mSelectedFile));
                    dialog.getListView().clearChoices();
                    dialog.getListView().setSelectionAfterHeaderView();
                }
            }
        };

        // When the user taps the positive button, notify the listener.
        OnClickListener positiveListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileChooserDialogListener listener = getListener();
                if (listener == null)
                    Log.w(TAG, "User clicked on dialog after it was detached from activity. Monkey?");
                else
                    listener.onFileSelected(actionId, mSelectedFile);
            }
        };

        // Dismiss/cancel callbacks: Are all of these needed?
        OnClickListener negativeListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getListener().onDismiss(actionId);
            }
        };
        OnCancelListener cancelListener = new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getListener().onDismiss(actionId);
            }
        };
        OnDismissListener dismissListener = new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                getListener().onDismiss(actionId);
            }
        };
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(FileChooser.getFullDisplayName(context, mSelectedFile))
                .setSingleChoiceItems(adapter, -1, fileSelectionListener)
                .setPositiveButton(R.string.file_chooser_choose, positiveListener)
                .setNegativeButton(android.R.string.cancel, negativeListener)
                .setOnCancelListener(cancelListener)
                .create();
        dialog.setOnDismissListener(dismissListener);
        return dialog;
    }

    private FileChooserDialogListener getListener() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof FileChooserDialogListener)
            return (FileChooserDialogListener) fragment;
        return (FileChooserDialogListener) getActivity();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.v(TAG, "onDismiss");
        super.onDismiss(dialog);
        Bundle arguments = getArguments();
        int actionId = arguments.getInt(EXTRA_ACTION_ID);
        FileChooserDialogListener listener = getListener();
        if (listener != null) listener.onDismiss(actionId);
    }
}
