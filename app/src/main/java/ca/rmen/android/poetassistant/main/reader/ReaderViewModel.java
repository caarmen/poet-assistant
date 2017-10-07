/*
 * Copyright (c) 2017 Carmen Alvarez
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
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.print.PrintJob;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.PreferenceManager;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;

public class ReaderViewModel extends AndroidViewModel {
    private static final String TAG = Constants.TAG + ReaderViewModel.class.getSimpleName();

    class SnackbarText {
        final @StringRes int stringResId;
        final Object[] params;

        SnackbarText(int stringResId, Object... params) {
            this.stringResId = stringResId;
            this.params = params;
        }
    }

    public final ObservableField<CharSequence> poemSelection = new ObservableField<>("");
    public final ObservableField<String> poem = new ObservableField<>("");
    public final ObservableInt playButtonDrawable = new ObservableInt();
    public final ObservableBoolean playButtonEnabled = new ObservableBoolean();
    final ObservableField<SnackbarText> snackbarText = new ObservableField<>();
    final ObservableBoolean ttsError = new ObservableBoolean();
    final ObservableField<PoemFile> poemFile = new ObservableField<>();
    @Inject
    Tts mTts;
    private final Handler mHandler;
    private final PoemPrefs mPoemPrefs;
    private final SharedPreferences mSharedPreferences;

    public ReaderViewModel(Application application) {
        super(application);
        DaggerHelper.getMainScreenComponent(application).inject(this);
        mHandler = new Handler();
        poem.addOnPropertyChangedCallback(new BindingCallbackAdapter(this::updatePlayButton));
        EventBus.getDefault().register(this);
        mPoemPrefs = new PoemPrefs(application);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPrefsListener);
        poemFile.set(mPoemPrefs.getSavedPoem());
    }

    // begin TTS

    /**
     * The button shall be disabled if TTS isn't initialized, or if there is no text to play.
     * The button should display a "Play" icon if TTS isn't running but can be started.
     * The button should display a "Stop" icon if TTS is currently running.
     * This is called from a background thread by TTS.
     */
    private void updatePlayButton() {
        Log.d(TAG, "updatePlayButton: tts status = " + mTts.getStatus() + ", tts is speaking = " + mTts.isSpeaking());
        mHandler.post(() -> {
            boolean enabled = !TextUtils.isEmpty(poem.get());
            playButtonEnabled.set(enabled);
            if (mTts.isSpeaking()) {
                playButtonDrawable.set(R.drawable.ic_stop);
            } else if (!enabled) {
                playButtonDrawable.set(R.drawable.ic_play_disabled);
            } else {
                playButtonDrawable.set(R.drawable.ic_play_enabled);
            }
        });
    }

    public void onPlayButtonClicked() {
        Log.v(TAG, "Play button clicked");
        if (mTts.isSpeaking()) {
            mTts.stop();
        } else if (mTts.getStatus() == TextToSpeech.SUCCESS) {
            speak();
        } else {
            ttsError.set(true);
            ttsError.set(false);
        }
        updatePlayButton();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true)
    public void onTtsInitialized(Tts.OnTtsInitialized event) {
        Log.d(TAG, "onTtsInitialized() called with: " + "event = [" + event + "]");

        updatePlayButton();
        // Sometimes when the tts engine is initialized, the "isSpeaking()" method returns true
        // if you call it immediately.  If we call updatePlayButton only once at this point, we
        // will show a "stop" button instead of a "play" button.  We workaround this by updating
        // the button again after a brief moment, hoping that isSpeaking() will correctly
        // return false, allowing us to display a "play" button.
        mHandler.postDelayed(this::updatePlayButton, 5000);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsUtteranceCompleted(Tts.OnUtteranceCompleted event) {
        Log.d(TAG, "onTtsUtteranceCompleted() called with: " + "event = [" + event + "]");
        updatePlayButton();
        mHandler.postDelayed(this::updatePlayButton, 1000);
    }

    /**
     * Read the text in our text view.
     */
    private void speak() {
        CharSequence text = poemSelection.get();
        int startPosition = Selection.getSelectionStart(text);
        if (startPosition == text.length() || startPosition == -1) startPosition = 0;
        int endPosition = Selection.getSelectionEnd(text);
        if (startPosition == endPosition || endPosition == -1) endPosition = text.length();
        Log.d(TAG, "selection " + startPosition + " - " + endPosition);
        mTts.speak(text.toString().substring(startPosition, endPosition));
    }

    void speakToFile() {
        mTts.speakToFile(poem.get());
        snackbarText.set(new SnackbarText(R.string.share_poem_audio_snackbar));
    }

    // end TTS

    // Begin saving/Opening files
    void updatePoemText() {
        Log.d(TAG, "Update poem text");
        mPoemPrefs.updatePoemText(poem.get());
    }

    void setSavedPoem(PoemFile savedPoem) {
        mPoemPrefs.setSavedPoem(savedPoem);
        poem.set(savedPoem.text);
    }

    String getSaveAsFilename() {
        PoemFile poemFile = mPoemPrefs.getSavedPoem();
        if (poemFile != null) {
            return poemFile.name;
        } else {
            return PoemFile.generateFileName(poem.get());
        }
    }

    void clearPoem() {
        mPoemPrefs.clear();
        poem.set("");
    }

    void save(Context context) {
        PoemFile savedPoem = mPoemPrefs.getSavedPoem();
        PoemFile.save(context, savedPoem.uri, poem.get(), mPoemFileCallback);
    }

    void saveAs(Context context, Uri uri) {
        PoemFile.save(context, uri, poem.get(), mPoemFileCallback);
    }

    void open(Context context, Uri uri) {
        PoemFile.open(context, uri, mPoemFileCallback);
    }

    void loadPoem() {
        // Load the poem we previously saved
        if (mPoemPrefs.hasSavedPoem()) {
            PoemFile savedPoem = mPoemPrefs.getSavedPoem();
            poem.set(savedPoem.text);
        } else if (mPoemPrefs.hasTempPoem()) {
            String tempPoemText = mPoemPrefs.getTempPoem();
            poem.set(tempPoemText);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void print(Context context) {
        if (poemFile.get() == null) {
            PoemFile.print(context, new PoemFile(null, PoemFile.generateFileName(poem.get()), poem.get()), mPoemFileCallback);
        } else {
            PoemFile.print(context, poemFile.get(), mPoemFileCallback);
        }
    }

    private final PoemFile.PoemFileCallback mPoemFileCallback = new PoemFile.PoemFileCallback() {
        @Override
        public void onPoemLoaded(@SuppressWarnings("SameParameterValue") PoemFile loadedPoem) {
            Log.d(TAG, "onPoemLoaded() called with: " + "poemFile = [" + loadedPoem + "]");
            if (loadedPoem == null) {
                clearPoem();
                snackbarText.set(new SnackbarText(R.string.file_opened_error));
            } else {
                setSavedPoem(loadedPoem);
                snackbarText.set(new SnackbarText(R.string.file_opened, loadedPoem.name));
            }
        }

        @Override
        public void onPoemSaved(PoemFile savedPoem) {
            if (savedPoem == null) {
                snackbarText.set(new SnackbarText(R.string.file_saved_error));
            } else {
                Log.d(TAG, "onPoemSaved() called with: " + "poemFile = [" + savedPoem + "]");
                setSavedPoem(savedPoem);
                snackbarText.set(new SnackbarText(R.string.file_saved, savedPoem.name));
            }
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onPrintJobCreated(PoemFile poemFile, @Nullable PrintJob printJob) {
            Log.d(TAG, "onPrintJobCreated() called with: poemFile = [" + poemFile + "], printJob = [" + printJob + "]");
            if (printJob != null) {
                Log.d(TAG, "Print job id = " + printJob.getId() + ", info = " +  printJob.getInfo());
            }
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    poemFile.set(mPoemPrefs.getSavedPoem());
                }
            };
    // end saving/opening files


    void destroy() {
        EventBus.getDefault().unregister(this);
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }


}
