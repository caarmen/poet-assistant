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

package ca.rmen.android.poetassistant.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Theme;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.databinding.ActivitySettingsBinding;
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsProvider;
import ca.rmen.android.poetassistant.wotd.Wotd;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity implements ConfirmDialogFragment.ConfirmDialogListener {

    private static final String TAG = Constants.TAG + SettingsActivity.class.getSimpleName();
    private static final int ACTION_CLEAR_SEARCH_HISTORY = 1;
    private static final String PREF_CATEGORY_VOICE = "PREF_CATEGORY_VOICE";
    private static final String PREF_CLEAR_SEARCH_HISTORY = "PREF_CLEAR_SEARCH_HISTORY";

    @Inject Tts mTts;
    @Inject Dictionary mDictionary;
    @Inject SettingsPrefs mSettingsPrefs;
    private ActivitySettingsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHelper.getSettingsComponent(getApplication()).inject(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mListener);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onPause() {
        mTts.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mListener);
        super.onDestroy();
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> {
        Log.v(TAG, "onSharedPreferenceChanged: key = " + key);
        Context context = getApplicationContext();
        if (Settings.PREF_THEME.equals(key)) {
            // When the theme changes, restart the activity
            Theme.setThemeFromSettings(mSettingsPrefs);
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(SettingsActivity.this);
            stackBuilder.addNextIntentWithParentStack(intent);
            stackBuilder.startActivities();
        } else if (Settings.PREF_WOTD_ENABLED.equals(key)) {
            Wotd.setWotdEnabled(context, mDictionary, mSettingsPrefs.getIsWotdEnabled());
        }
    };

    @Override
    public void onOk(int actionId) {
        if (actionId == ACTION_CLEAR_SEARCH_HISTORY) {
            Completable.fromRunnable(() -> getContentResolver().delete(SuggestionsProvider.CONTENT_URI, null, null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Snackbar.make(mBinding.getRoot(), R.string.search_history_cleared, Snackbar.LENGTH_SHORT).show());
        }
    }

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        private static final String DIALOG_TAG = "dialog_tag";

        @Inject Tts mTts;
        private boolean mRestartTtsOnResume = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            DaggerHelper.getSettingsComponent(getContext()).inject(this);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            loadPreferences();
        }

        private void loadPreferences() {
            addPreferencesFromResource(R.xml.pref_general);
            getPreferenceScreen().findPreference(PREF_CLEAR_SEARCH_HISTORY).setOnPreferenceClickListener(preference -> {
                ConfirmDialogFragment.show(
                        ACTION_CLEAR_SEARCH_HISTORY,
                        getString(R.string.confirm_clear_search_history),
                        getString(R.string.action_clear),
                        getFragmentManager(),
                        DIALOG_TAG);
                return true;
            });
            VoicePreference voicePreference = (VoicePreference) findPreference(Settings.PREF_VOICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                voicePreference.loadVoices(getContext());
            }
            if (voicePreference.getEntries() == null || voicePreference.getEntries().length < 2) {
                removePreference(PREF_CATEGORY_VOICE, voicePreference);
            }
            Preference voicePreview = findPreference(Settings.PREF_VOICE_PREVIEW);
            voicePreview.setOnPreferenceClickListener(preference -> {
                if (mTts.isSpeaking()) mTts.stop();
                else mTts.speak(getString(R.string.pref_voice_preview_text));
                return false;
            });
            Preference systemTtsSettings = findPreference(Settings.PREF_SYSTEM_TTS_SETTINGS);
            Intent intent = systemTtsSettings.getIntent();
            if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                removePreference(PREF_CATEGORY_VOICE, systemTtsSettings);
            } else {
                systemTtsSettings.setOnPreferenceClickListener(preference -> {
                    mRestartTtsOnResume = true;
                    return false;
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            EventBus.getDefault().register(this);
            if (mRestartTtsOnResume) {
                mTts.restart();
                mRestartTtsOnResume = false;
            }
        }

        @Override
        public void onPause() {
            EventBus.getDefault().unregister(this);
            super.onPause();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (Settings.PREF_VOICE.equals(preference.getKey())) {
                if (getFragmentManager().findFragmentByTag(DIALOG_TAG) != null) {
                    return;
                }
                VoicePreferenceDialogFragment fragment = VoicePreferenceDialogFragment.newInstance(preference.getKey());
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), DIALOG_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onTtsInitialized(Tts.OnTtsInitialized event) {
            Log.v(TAG, "onTtsInitialized, event = " + event + ", status = " + event.status);
            getPreferenceScreen().removeAll();
            loadPreferences();
        }

        private void removePreference(String categoryKey, Preference preference) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference(categoryKey);
            category.removePreference(preference);
        }

    }

}
