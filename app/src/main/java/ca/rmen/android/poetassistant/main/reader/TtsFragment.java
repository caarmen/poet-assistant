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

package ca.rmen.android.poetassistant.main.reader;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.filechooser.FileChooserDialogFragment;


public class TtsFragment extends Fragment implements
        FileChooserDialogFragment.FileChooserDialogListener,
        PoemFile.PoemFileCallback {
    private static final String TAG = Constants.TAG + TtsFragment.class.getSimpleName();
    private static final String EXTRA_INITIAL_TEXT = "initial_text";
    private static final int ACTION_FILE_OPEN = 0;
    private static final int ACTION_FILE_SAVE_AS = 1;

    private ImageView mPlayButton;
    private EditText mTextView;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
    private Handler mHandler;
    private PoemFile mPoemFile;
    private PoemPrefs mPoemPrefs;

    public static TtsFragment newInstance(String initialText) {
        Log.d(TAG, "newInstance() called with: " + "initialText = [" + initialText + "]");
        TtsFragment fragment = new TtsFragment();
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
        mTextToSpeech = new TextToSpeech(getActivity().getApplicationContext(), mOnInitListener);
        mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
        mPoemPrefs = new PoemPrefs(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        View view = inflater.inflate(R.layout.fragment_tts, container, false);
        mPlayButton = (ImageView) view.findViewById(R.id.btn_play);
        mTextView = (EditText) view.findViewById(R.id.tv_text);
        mPlayButton.setOnClickListener(mOnClickListener);
        mTextView.addTextChangedListener(mTextWatcher);
        mHandler = new Handler();
        mPoemFile = new PoemFile(this);
        mTextView.setText(mPoemPrefs.getSavedPoemText());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu() called with: " + "menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.menu_tts, menu);
        MenuItem saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setEnabled(mPoemPrefs.hasSavedPoem());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_open) {
            FileChooserDialogFragment.show(this, mPoemPrefs.getSavedPoemFolder(), false, false, ACTION_FILE_OPEN);
        } else if (item.getItemId() == R.id.action_save) {
            mPoemFile.save(mPoemPrefs.getSavedPoemFile(), mTextView.getText().toString());
        } else if (item.getItemId() == R.id.action_save_as) {
            FileChooserDialogFragment.show(this, mPoemPrefs.getSavedPoemFile(), false, true, ACTION_FILE_SAVE_AS);
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called with: " + "");
        mPoemPrefs.setSavedPoemText(mTextView.getText().toString());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called with: " + "");
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
        super.onDestroy();
    }

    public void speak(String text) {
        Log.d(TAG, "speak() called with: " + "text = [" + text + "]");
        mTextView.setText(text);
        mPlayButton.callOnClick();
    }

    @Override
    public void onFileSelected(int actionId, File file) {
        Log.d(TAG, "onFileSelected() called with: " + "actionId = [" + actionId + "], file = [" + file + "]");
        if (actionId == ACTION_FILE_OPEN) {
            mPoemFile.open(file);
        } else if (actionId == ACTION_FILE_SAVE_AS) {
            mPoemFile.save(file, mTextView.getText().toString());
        }
    }

    @Override
    public void onDismiss(int actionId) {
        Log.d(TAG, "onDismiss() called with: " + "actionId = [" + actionId + "]");
    }

    @Override
    public void onPoemLoaded(File file, String text) {
        mTextView.setText(text);
        mPoemPrefs.setSavedPoemFile(file, text);
        getActivity().supportInvalidateOptionsMenu();
        Snackbar.make(mTextView, getString(R.string.file_opened, file.getAbsolutePath()), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPoemSaved(File file, String text) {
        mPoemPrefs.setSavedPoemFile(file, text);
        Snackbar.make(mTextView, getString(R.string.file_saved, file.getAbsolutePath()), Snackbar.LENGTH_LONG).show();
    }

    /**
     * The button shall be disabled if TTS isn't initialized, or if there is no text to play.
     * The button should display a "Play" icon if TTS isn't running but can be started.
     * The button should display a "Stop" icon if TTS is currently running.
     * This is called from a background thread by TTS.
     */
    private void updatePlayButton() {
        Log.d(TAG, "updatePlayButton");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean enabled = !TextUtils.isEmpty(mTextView.getText())
                        && mTtsStatus == TextToSpeech.SUCCESS;
                mPlayButton.setEnabled(enabled);
                if (mTextToSpeech.isSpeaking()) {
                    mPlayButton.setImageResource(R.drawable.ic_stop);
                } else {
                    mPlayButton.setImageResource(R.drawable.ic_play);
                }
            }
        });
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "Play button clicked");
            if (mTextToSpeech.isSpeaking()) mTextToSpeech.stop();
            else speak();

            updatePlayButton();
        }
    };

    /**
     * Read the text in our text view.
     */
    private void speak() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            speak21();
        else
            speak4();
    }

    @SuppressWarnings("deprecation")
    private void speak4() {
        mTextToSpeech.speak(mTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speak21() {
        mTextToSpeech.speak(mTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, mTextView.getText().toString());
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updatePlayButton();
        }
    };

    private final TextToSpeech.OnInitListener mOnInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            mTtsStatus = status;
            updatePlayButton();
            if (mTtsStatus == TextToSpeech.SUCCESS) {
                Bundle arguments = getArguments();
                if (arguments != null) {
                    String initialText = arguments.getString(EXTRA_INITIAL_TEXT);
                    if (!TextUtils.isEmpty(initialText)) {
                        mTextView.setText(initialText);
                        mPoemPrefs.setSavedPoem(initialText);
                        speak();
                        getActivity().supportInvalidateOptionsMenu();
                    } else if (mPoemPrefs.hasSavedPoem()) {
                        mPoemFile.open(mPoemPrefs.getSavedPoemFile());
                    }
                }
            }
        }
    };

    private final UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            updatePlayButton();
        }

        @Override
        public void onDone(String utteranceId) {
            updatePlayButton();
        }

        @Override
        public void onError(String utteranceId) {
            updatePlayButton();
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            super.onError(utteranceId, errorCode);
            updatePlayButton();
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            super.onStop(utteranceId, interrupted);
            updatePlayButton();
        }
    };


}
