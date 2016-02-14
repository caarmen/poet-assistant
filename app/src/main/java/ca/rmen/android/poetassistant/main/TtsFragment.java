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

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;


public class TtsFragment extends Fragment {
    private static final String TAG = Constants.TAG + TtsFragment.class.getSimpleName();
    private static final String PREF_POEM_TEXT = "poem_text";

    private ImageView mPlayButton;
    private EditText mTextView;
    private TextToSpeech mTextToSpeech;
    private int mTtsStatus = TextToSpeech.ERROR;
    private SharedPreferences mSharedPreferences;
    private Handler mHandler;

    public static TtsFragment newInstance() {
        Log.d(TAG, "newInstance() called with: " + "");
        TtsFragment fragment = new TtsFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        mTextToSpeech = new TextToSpeech(getActivity().getApplicationContext(), mOnInitListener);
        mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
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
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String poemText = mSharedPreferences.getString(PREF_POEM_TEXT, null);
        mTextView.setText(poemText);
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called with: " + "");
        mSharedPreferences.edit().putString(PREF_POEM_TEXT, mTextView.getText().toString()).apply();
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
            if (mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    speak21();
                else
                    speak4();
            }

            updatePlayButton();
        }
    };

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
