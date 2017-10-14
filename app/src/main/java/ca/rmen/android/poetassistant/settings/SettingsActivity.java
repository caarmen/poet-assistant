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

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG + SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat
            implements ConfirmDialogFragment.ConfirmDialogListener {

        private static final String DIALOG_TAG = "dialog_tag";
        private static final int ACTION_EXPORT_FAVORITES = 1;
        private static final int ACTION_IMPORT_FAVORITES = 2;
        private static final int ACTION_CLEAR_SEARCH_HISTORY = 3;
        private static final String PREF_CATEGORY_VOICE = "PREF_CATEGORY_VOICE";
        private static final String PREF_CATEGORY_NOTIFICATIONS = "PREF_CATEGORY_NOTIFICATIONS";
        private static final String PREF_CATEGORY_USER_DATA = "PREF_CATEGORY_USER_DATA";
        private static final String PREF_EXPORT_FAVORITES = "PREF_EXPORT_FAVORITES";
        private static final String PREF_IMPORT_FAVORITES = "PREF_IMPORT_FAVORITES";
        private static final String PREF_CLEAR_SEARCH_HISTORY = "PREF_CLEAR_SEARCH_HISTORY";

        @Inject
        Tts mTts;
        private boolean mRestartTtsOnResume = false;

        private SettingsViewModel mViewModel;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            DaggerHelper.getSettingsComponent(getContext()).inject(this);
            mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            loadPreferences();
        }

        private void loadPreferences() {
            addPreferencesFromResource(R.xml.pref_general);
            setOnPreferenceClickListener(PREF_CLEAR_SEARCH_HISTORY, () -> ConfirmDialogFragment.show(
                    ACTION_CLEAR_SEARCH_HISTORY,
                    getString(R.string.confirm_clear_search_history),
                    getString(R.string.action_clear),
                    getChildFragmentManager(),
                    DIALOG_TAG));
            // Hide the voice preference if we can't load any voices
            VoicePreference voicePreference = (VoicePreference) findPreference(Settings.PREF_VOICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                voicePreference.loadVoices(getContext());
            }
            if (voicePreference.getEntries() == null || voicePreference.getEntries().length < 2) {
                removePreference(PREF_CATEGORY_VOICE, voicePreference);
            }
            setOnPreferenceClickListener(Settings.PREF_VOICE_PREVIEW, () -> mViewModel.playTtsPreview());

            // Hide the system tts settings if no system app can handle it
            Preference systemTtsSettings = findPreference(Settings.PREF_SYSTEM_TTS_SETTINGS);
            Intent intent = systemTtsSettings.getIntent();
            if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                removePreference(PREF_CATEGORY_VOICE, systemTtsSettings);
            } else {
                setOnPreferenceClickListener(systemTtsSettings, () -> mRestartTtsOnResume = true);
            }

            // Android O users can change the priority in the system settings.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                removePreferences(PREF_CATEGORY_NOTIFICATIONS, Settings.PREF_WOTD_NOTIFICATION_PRIORITY);
            }

            // Importing/exporting files is only available from KitKat.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                removePreferences(PREF_CATEGORY_USER_DATA, PREF_EXPORT_FAVORITES, PREF_IMPORT_FAVORITES);
            } else {
                setOnPreferenceClickListener(PREF_EXPORT_FAVORITES, () -> startActivityForResult(mViewModel.getExportFavoritesIntent(), ACTION_EXPORT_FAVORITES));
                setOnPreferenceClickListener(PREF_IMPORT_FAVORITES, () -> startActivityForResult(mViewModel.getImportFavoritesIntent(), ACTION_IMPORT_FAVORITES));
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
            mViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
        }

        @Override
        public void onPause() {
            EventBus.getDefault().unregister(this);
            mViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
            mTts.stop();
            super.onPause();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
            Uri uri = data == null ? null : data.getData();
            if (requestCode == ACTION_EXPORT_FAVORITES && resultCode == Activity.RESULT_OK && uri != null) {
                mViewModel.exportFavorites(uri);
            } else if (requestCode == ACTION_IMPORT_FAVORITES && resultCode == Activity.RESULT_OK && uri != null) {
                mViewModel.importFavorites(uri);
            }
        }

        @Override
        public void onOk(int actionId) {
            if (actionId == ACTION_CLEAR_SEARCH_HISTORY) {
                mViewModel.clearSearchHistory();
            }
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

        private void removePreferences(String categoryKey, String... preferenceKeys) {
            for (String preferenceKey : preferenceKeys) {
                removePreference(categoryKey, findPreference(preferenceKey));
            }
        }

        private void removePreference(String categoryKey, Preference preference) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference(categoryKey);
            category.removePreference(preference);
        }

        private void setOnPreferenceClickListener(String preferenceKey, Runnable runnable) {
            setOnPreferenceClickListener(findPreference(preferenceKey), runnable);
        }

        private void setOnPreferenceClickListener(Preference preference, Runnable runnable) {
            preference.setOnPreferenceClickListener(pref -> {
                runnable.run();
                return true;
            });
        }

        private Observable.OnPropertyChangedCallback mSnackbarCallback = new BindingCallbackAdapter(() -> {
            View rootView = getView();
            if (rootView != null) {
                Snackbar.make(rootView, mViewModel.snackbarText.get(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
