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
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Build;
import android.print.PrintJob;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.PreferenceManager;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.TtsState;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.databinding.LiveDataMapping;
import ca.rmen.android.poetassistant.main.dictionaries.Share;

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
    final MutableLiveData<SnackbarText> snackbarText = new MutableLiveData<>();
    final MutableLiveData<Boolean> ttsError = new MutableLiveData<>();
    final MutableLiveData<PoemFile> poemFile = new MutableLiveData<>();
    final MediatorLiveData<PlayButtonState> playButtonStateLiveData;
    @Inject
    Tts mTts;
    private final PoemPrefs mPoemPrefs;
    private final SharedPreferences mSharedPreferences;

    public ReaderViewModel(Application application) {
        super(application);
        DaggerHelper.getMainScreenComponent(application).inject(this);
        mPoemPrefs = new PoemPrefs(application);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPrefsListener);
        poemFile.setValue(mPoemPrefs.getSavedPoem());
        playButtonStateLiveData = new MediatorLiveData<>();
        playButtonStateLiveData.addSource(mTts.getTtsLiveData(),
                ttsState -> playButtonStateLiveData.setValue(toPlayButtonState(ttsState, poem.get())));
        playButtonStateLiveData.addSource(LiveDataMapping.fromObservableField(poem),
                poemText -> playButtonStateLiveData.setValue(toPlayButtonState(mTts.getTtsState(), poemText)));
    }

    // begin TTS

    /**
     * The button shall be disabled if TTS isn't initialized, or if there is no text to play.
     * The button should display a "Play" icon if TTS isn't running but can be started.
     * The button should display a "Stop" icon if TTS is currently running.
     */
    private static PlayButtonState toPlayButtonState(TtsState ttsState, String poemText) {
        Log.v(TAG, "toPlayButtonState: ttsState = " + ttsState + ", poemText = " + poemText);
        if (ttsState != null) {
            if (ttsState.currentStatus == TtsState.TtsStatus.INITIALIZED) {
                if (TextUtils.isEmpty(poemText)) {
                    return new PlayButtonState(false, R.drawable.ic_play_disabled);
                } else {
                    return new PlayButtonState(true, R.drawable.ic_play_enabled);
                }
            } else if (ttsState.currentStatus == TtsState.TtsStatus.SPEAKING) {
                return new PlayButtonState(true, R.drawable.ic_stop);
            } else {
                return new PlayButtonState(false, R.drawable.ic_play_disabled);
            }
        } else {
            return new PlayButtonState(false, R.drawable.ic_play_disabled);
        }
    }

    static class PlayButtonState {
        final boolean isEnabled;
        @DrawableRes
        final int iconId;
        PlayButtonState(boolean isEnabled, int iconId) {
            this.isEnabled = isEnabled;
            this.iconId = iconId;
        }

        @Override
        public String toString() {
            return "PlayButtonState{" +
                    "isEnabled=" + isEnabled +
                    ", iconId=" + iconId +
                    '}';
        }
    }

    public void onPlayButtonClicked() {
        Log.v(TAG, "Play button clicked");
        if (mTts.isSpeaking()) {
            mTts.stop();
        } else if (mTts.getTtsState() != null && mTts.getTtsState().currentStatus == TtsState.TtsStatus.INITIALIZED) {
            speak();
        } else {
            ttsError.setValue(true);
            ttsError.setValue(false);
        }
    }

    /**
     * Read the text in our text view.
     */
    private void speak() {
        CharSequence text = poemSelection.get();
        if (TextUtils.isEmpty(text)) {
            mTts.speak(poem.get());
        } else {
            int startPosition = Selection.getSelectionStart(text);
            int endPosition = Selection.getSelectionEnd(text);
            Log.d(TAG, "selection " + startPosition + " - " + endPosition);
            if (startPosition == -1) startPosition = 0;
            if (endPosition == -1) endPosition = 0;
            if (startPosition == text.length()) startPosition = 0;
            if (startPosition == endPosition) endPosition = text.length();
            Log.d(TAG, "now selection " + startPosition + " - " + endPosition);
            mTts.speak(text.toString().substring(startPosition, endPosition));
        }
    }

    void speakToFile() {
        mTts.speakToFile(poem.get());
        snackbarText.setValue(new SnackbarText(R.string.share_poem_audio_snackbar));
    }

    // end TTS

    // Begin saving/Opening files
    void updatePoemText() {
        Log.d(TAG, "Update poem text");
        mPoemPrefs.updatePoemText(poem.get());
    }

    void setSavedPoem(PoemFile savedPoem) {
        Log.v(TAG, "setSavedPoem " + savedPoem);
        mPoemPrefs.setSavedPoem(savedPoem);
        poem.set(savedPoem.text);
    }

    private String getSaveAsFilename() {
        PoemFile poemFile = mPoemPrefs.getSavedPoem();
        if (poemFile != null) {
            return poemFile.name;
        } else {
            return PoemFile.Companion.generateFileName(poem.get());
        }
    }

    void clearPoem() {
        mPoemPrefs.clear();
        poem.set("");
    }

    void save(Context context) {
        PoemFile savedPoem = mPoemPrefs.getSavedPoem();
        if (savedPoem != null) {
            PoemFile.Companion.save(context, savedPoem.uri, poem.get(), mPoemFileCallback);
        }
    }

    void saveAs(Context context, Uri uri) {
        PoemFile.Companion.save(context, uri, poem.get(), mPoemFileCallback);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    Intent getOpenFileIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (poemFile.getValue() != null) {
            intent.setData(poemFile.getValue().uri);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    Intent getSaveAsFileIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        String fileName = getSaveAsFilename();
        if (!TextUtils.isEmpty(fileName)) intent.putExtra(Intent.EXTRA_TITLE, fileName);
        return intent;
    }

    void open(Context context, Uri uri) {
        PoemFile.Companion.open(context, uri, mPoemFileCallback);
    }

    void loadPoem() {
        // Load the poem we previously saved
        if (mPoemPrefs.hasSavedPoem()) {
            PoemFile savedPoem = mPoemPrefs.getSavedPoem();
            if (savedPoem != null) {
                poem.set(savedPoem.text);
            }
        } else if (mPoemPrefs.hasTempPoem()) {
            String tempPoemText = mPoemPrefs.getTempPoem();
            poem.set(tempPoemText);
        }
    }

    void sharePoem() {
        Share.share(getApplication(), poem.get());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void print(Context context) {
        if (poemFile.getValue() == null) {
            PoemFile.Companion.print(context, new PoemFile(null, PoemFile.Companion.generateFileName(poem.get()), poem.get()), mPoemFileCallback);
        } else {
            PoemFile.Companion.print(context, poemFile.getValue(), mPoemFileCallback);
        }
    }

    private final PoemFileCallback mPoemFileCallback = new PoemFileCallback() {
        @Override
        public void onPoemLoaded(@SuppressWarnings("SameParameterValue") PoemFile loadedPoem) {
            Log.d(TAG, "onPoemLoaded() called with: " + "poemFile = [" + loadedPoem + "]");
            if (loadedPoem == null) {
                clearPoem();
                snackbarText.setValue(new SnackbarText(R.string.file_opened_error));
            } else {
                setSavedPoem(loadedPoem);
                snackbarText.setValue(new SnackbarText(R.string.file_opened, loadedPoem.name));
            }
        }

        @Override
        public void onPoemSaved(PoemFile savedPoem) {
            if (savedPoem == null) {
                snackbarText.setValue(new SnackbarText(R.string.file_saved_error));
            } else {
                Log.d(TAG, "onPoemSaved() called with: " + "poemFile = [" + savedPoem + "]");
                setSavedPoem(savedPoem);
                snackbarText.setValue(new SnackbarText(R.string.file_saved, savedPoem.name));
            }
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onPrintJobCreated(@NonNull PoemFile poemFile, @Nullable PrintJob printJob) {
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
                    Log.v(TAG, "onSharedPreferenceChanged: key = " + key);

                    // Prevent the search EditText from disappearing while the user is typing,
                    // by only notifying actual changes in the poem text.
                    // When starting a new instrumentation test after completing another instrumentation
                    // test, we get a shared prefs change with a null value before and after for the shared poem.
                    // This resulted in an invalidation of the options menu, causing problems when entering
                    // search text.
                    PoemFile oldPoemText = poemFile.getValue();
                    PoemFile newPoemText = mPoemPrefs.getSavedPoem();
                    Log.v(TAG, "old: " + oldPoemText + ", new: " + newPoemText);
                    if ((oldPoemText == null && newPoemText == null)
                            || (oldPoemText != null && oldPoemText.equals(newPoemText))
                            || (newPoemText != null && newPoemText.equals(oldPoemText))) {
                        Log.v(TAG, "Ignoring uninteresting poem file change");
                    } else {
                        poemFile.setValue(mPoemPrefs.getSavedPoem());
                    }
                }
            };
    // end saving/opening files


    @Override
    protected void onCleared() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
        super.onCleared();
    }
}
