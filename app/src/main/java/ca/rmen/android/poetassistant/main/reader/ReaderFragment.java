/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.reader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.compat.HtmlCompat;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;
import ca.rmen.android.poetassistant.databinding.FragmentReaderBinding;
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper;
import ca.rmen.android.poetassistant.main.TextPopupMenu;
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener;
import ca.rmen.android.poetassistant.settings.SettingsActivity;
import ca.rmen.android.poetassistant.widget.DebounceTextWatcher;

public class ReaderFragment extends Fragment implements
        ConfirmDialogFragment.ConfirmDialogListener {
    private static final String TAG = Constants.TAG + ReaderFragment.class.getSimpleName();
    private static final String EXTRA_INITIAL_TEXT = "initial_text";
    private static final String DIALOG_TAG = "dialog";
    private static final int ACTION_FILE_OPEN = 0;
    private static final int ACTION_FILE_SAVE_AS = 1;
    private static final int ACTION_FILE_NEW = 2;

    private ReaderViewModel mViewModel;

    public static ReaderFragment newInstance(String initialText) {
        Log.d(TAG, "newInstance() called with: " + "initialText = [" + initialText + "]");
        ReaderFragment fragment = new ReaderFragment();
        fragment.setRetainInstance(true);
        Bundle bundle = new Bundle(1);
        bundle.putString(EXTRA_INITIAL_TEXT, initialText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        loadPoem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        FragmentReaderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reader, container, false);
        mViewModel = new ReaderViewModel(getContext());
        binding.setViewModel(mViewModel);
        mViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
        mViewModel.ttsError.addOnPropertyChangedCallback(mTtsErrorCallback);
        mViewModel.poemFile.addOnPropertyChangedCallback(mPoemFileCallback);
        binding.tvText.setImeListener(() -> AppBarLayoutHelper.forceExpandAppBarLayout(getActivity()));
        DebounceTextWatcher.observe(binding.tvText).subscribe(text -> mViewModel.updatePoemText());
        TextPopupMenu.addSelectionPopupMenu(binding.tvText, (OnWordClickListener) getActivity());
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu() called with: " + "menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.menu_tts, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) menu.findItem(R.id.action_share).setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        prepareMenuItemsRequiringEnteredText(menu, R.id.action_new, R.id.action_save_as,
                R.id.action_share, R.id.action_share_poem_text, R.id.action_share_poem_audio);
        MenuItem menuItem = menu.findItem(R.id.action_save);
        if (menuItem == null) {
            Log.d(TAG, "Unexpected: save menu item missing from reader fragment. Monkey?");
        } else {
            menuItem.setEnabled(mViewModel.poemFile.get() != null);
        }
    }

    private void prepareMenuItemsRequiringEnteredText(Menu menu, @IdRes int ...menuIds) {
        boolean hasEnteredText = !TextUtils.isEmpty(mViewModel.poem.get());
        for (@IdRes int menuId : menuIds) {
            MenuItem menuItem = menu.findItem(menuId);
            if (menuItem != null) {
                menuItem.setEnabled(hasEnteredText);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            ConfirmDialogFragment.show(ACTION_FILE_NEW,
                    getString(R.string.file_new_confirm_title),
                    getString(R.string.action_clear),
                    getChildFragmentManager(),
                    DIALOG_TAG);
        } else if (item.getItemId() == R.id.action_open) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) open();
        } else if (item.getItemId() == R.id.action_save) {
            mViewModel.save(getActivity());
        } else if (item.getItemId() == R.id.action_save_as) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) saveAs();
        } else if (item.getItemId() == R.id.action_share_poem_text || item.getItemId() == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, mViewModel.poem.get());
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } else if (item.getItemId() == R.id.action_share_poem_audio) {
            mViewModel.speakToFile();
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void open() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        PoemFile poemFile = mViewModel.poemFile.get();
        if (poemFile != null) intent.setData(poemFile.uri);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, ACTION_FILE_OPEN);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void saveAs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        String fileName = mViewModel.getSaveAsFilename();
        if (!TextUtils.isEmpty(fileName)) intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, ACTION_FILE_SAVE_AS);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called with: " + "");
        mViewModel.updatePoemText();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        mViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        mViewModel.ttsError.removeOnPropertyChangedCallback(mTtsErrorCallback);
        mViewModel.poemFile.removeOnPropertyChangedCallback(mPoemFileCallback);
        mViewModel.destroy();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called with: " + "");
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == ACTION_FILE_OPEN && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mViewModel.open(getActivity(), data.getData());
            }
        } else if (requestCode == ACTION_FILE_SAVE_AS && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mViewModel.saveAs(getActivity(), data.getData());
            }
        }
    }

    public void setText(String text) {
        Log.d(TAG, "speak() called with: " + "text = [" + text + "]");
        PoemFile poemFile = new PoemFile(null, null, text);
        mViewModel.setSavedPoem(poemFile);
    }

    @Override
    public void onOk(int actionId) {
        if (actionId == ACTION_FILE_NEW) {
            mViewModel.clearPoem();
            // Hack for https://github.com/caarmen/poet-assistant/issues/72
            // On some devices, clearing the poem text auto-hides the app bar layout.
            // Let's expand it again.
            AppBarLayoutHelper.forceExpandAppBarLayout(getActivity());
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    private void loadPoem() {
        Log.d(TAG, "loadPoem() called with: " + "");
        // First see if we have poem in the arguments
        // (the user chose to share some text with our app)
        Bundle arguments = getArguments();
        if (arguments != null) {
            String initialText = arguments.getString(EXTRA_INITIAL_TEXT);
            if (!TextUtils.isEmpty(initialText)) {
                PoemFile poemFile = new PoemFile(null, null, initialText);
                mViewModel.setSavedPoem(poemFile);
                getActivity().supportInvalidateOptionsMenu();
                return;
            }
        }
        // Load the poem we previously saved
        mViewModel.loadPoem();
    }

    private final Observable.OnPropertyChangedCallback mSnackbarCallback =
            new BindingCallbackAdapter(
                    () -> {
                        View root = getView();
                        if (root != null) {
                            ReaderViewModel.SnackbarText text = mViewModel.snackbarText.get();
                            String message = getString(text.stringResId, text.params);
                            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
                        }
                    });

    private final Observable.OnPropertyChangedCallback mTtsErrorCallback =
            new BindingCallbackAdapter(
                    () -> {
                        if (mViewModel.ttsError.get()) {
                            View root = getView();
                            if (root != null) {
                                Snackbar snackBar = Snackbar.make(root, HtmlCompat.fromHtml(getString(R.string.tts_error)), Snackbar.LENGTH_LONG);
                                final Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    snackBar.setAction(R.string.tts_error_open_system_settings, view -> startActivity(intent));
                                } else {
                                    snackBar.setAction(R.string.tts_error_open_app_settings, view -> startActivity(new Intent(getContext(), SettingsActivity.class)));
                                }
                                snackBar.show();
                            }
                        }
                    }
            );

    private final Observable.OnPropertyChangedCallback mPoemFileCallback =
            new BindingCallbackAdapter(() -> getActivity().supportInvalidateOptionsMenu());

}

