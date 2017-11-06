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

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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

import java.util.concurrent.TimeUnit;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.compat.HtmlCompat;
import ca.rmen.android.poetassistant.databinding.FragmentReaderBinding;
import ca.rmen.android.poetassistant.databinding.LiveDataMapping;
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper;
import ca.rmen.android.poetassistant.main.TextPopupMenu;
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment;
import ca.rmen.android.poetassistant.main.dictionaries.HelpDialogFragment;
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
    private Handler mHandler;
    private FragmentReaderBinding mBinding;

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
        mHandler = new Handler();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        loadPoem();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reader, container, false);
        mBinding.setButtonListener(new ButtonListener());
        mViewModel = ViewModelProviders.of(this).get(ReaderViewModel.class);
        mBinding.setViewModel(mViewModel);
        mViewModel.snackbarText.observe(this, mSnackbarCallback);
        mViewModel.ttsError.observe(this, mTtsErrorCallback);
        mViewModel.poemFile.observe(this, mPoemFileCallback);
        mBinding.tvText.setImeListener(() -> AppBarLayoutHelper.forceExpandAppBarLayout(getActivity()));
        DebounceTextWatcher.observe(mBinding.tvText).subscribe(text -> mViewModel.updatePoemText());
        TextPopupMenu.addSelectionPopupMenu(mBinding.tvText, (OnWordClickListener) getActivity());
        LiveDataMapping.debounceObserve(mViewModel.playButtonStateLiveData, this, mPlayButtonStateObserver, 500, TimeUnit.MILLISECONDS);
        return mBinding.getRoot();
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
                R.id.action_share, R.id.action_share_poem_text, R.id.action_share_poem_audio, R.id.action_print);
        MenuItem menuItem = menu.findItem(R.id.action_save);
        if (menuItem == null) {
            Log.d(TAG, "Unexpected: save menu item missing from reader fragment. Monkey?");
        } else {
            menuItem.setEnabled(mViewModel.poemFile.getValue() != null);
        }
    }

    private void prepareMenuItemsRequiringEnteredText(Menu menu, @IdRes int... menuIds) {
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
        } else if (item.getItemId() == R.id.action_share_poem_text || item.getItemId() == R.id.action_share) {
            mViewModel.sharePoem();
        } else if (item.getItemId() == R.id.action_share_poem_audio) {
            mViewModel.speakToFile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (item.getItemId() == R.id.action_open) {
                startActivityForResult(mViewModel.getOpenFileIntent(), ACTION_FILE_OPEN);
            } else if (item.getItemId() == R.id.action_save) {
                mViewModel.save(getActivity());
            } else if (item.getItemId() == R.id.action_save_as) {
                startActivityForResult(mViewModel.getSaveAsFileIntent(), ACTION_FILE_SAVE_AS);
            } else if (item.getItemId() == R.id.action_print) {
                mViewModel.print(getActivity());
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called with: " + "");
        mViewModel.updatePoemText();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == ACTION_FILE_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            mViewModel.open(getActivity(), data.getData());
        } else if (requestCode == ACTION_FILE_SAVE_AS && resultCode == Activity.RESULT_OK && data != null) {
            mViewModel.saveAs(getActivity(), data.getData());
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
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
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
                Log.v(TAG, "loadPoem: invalidateOptionsMenu");
                Activity activity = getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
                return;
            }
        }
        // Load the poem we previously saved
        mViewModel.loadPoem();
    }

    private void updatePlayButton() {
        ReaderViewModel.PlayButtonState playButtonState = mViewModel.playButtonStateLiveData.getValue();
        Log.v(TAG, "updatePlayButton: playButtonState = " + playButtonState);
        if (playButtonState != null) {
            mBinding.btnPlay.setEnabled(playButtonState.isEnabled);
            mBinding.btnPlay.setImageResource(playButtonState.iconId);
        }
    }

    private final Observer<ReaderViewModel.SnackbarText> mSnackbarCallback = text -> {
        View root = getView();
        if (root != null && text != null) {
            String message = getString(text.stringResId, text.params);
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
        }
    };

    private final Observer<Boolean> mTtsErrorCallback = hasTtsError -> {
        if (hasTtsError == Boolean.TRUE) {
            View root = getView();
            if (root != null) {
                Snackbar snackBar = Snackbar.make(root, HtmlCompat.fromHtml(getString(R.string.tts_error)), Snackbar.LENGTH_LONG);
                final Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
                if (intent.resolveActivity(root.getContext().getPackageManager()) != null) {
                    snackBar.setAction(R.string.tts_error_open_system_settings, view -> startActivity(intent));
                } else {
                    snackBar.setAction(R.string.tts_error_open_app_settings, view -> startActivity(new Intent(getContext(), SettingsActivity.class)));
                }
                snackBar.show();
            }
        }
    };

    private final Observer<PoemFile> mPoemFileCallback = poemFile -> {
        Activity activity = getActivity();
        if (activity != null) {
            Log.v(TAG, "poemFileCallback: invalidateOptionsMenu");
            activity.invalidateOptionsMenu();
        }
    };

    private final Observer<ReaderViewModel.PlayButtonState> mPlayButtonStateObserver = playButtonState -> {
        Log.v(TAG, "playButtonStateLiveData " + playButtonState);
        updatePlayButton();
        // Sometimes when the tts engine is initialized, the "isSpeaking()" method returns true
        // if you call it immediately.  If we call updatePlayButton only once at this point, we
        // will show a "stop" button instead of a "play" button.  We workaround this by updating
        // the button again after a brief moment, hoping that isSpeaking() will correctly
        // return false, allowing us to display a "play" button.
        mHandler.postDelayed(this::updatePlayButton, 5000);
    };

    public class ButtonListener {

        public void onPlayButtonClicked() {
            mViewModel.play(mBinding.tvText.getText());
        }

        public void onWordCountClicked() {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(HelpDialogFragment.create(R.string.word_count_help_title, R.string.word_count_help_message), DIALOG_TAG)
                    .commit();
        }
    }

}

