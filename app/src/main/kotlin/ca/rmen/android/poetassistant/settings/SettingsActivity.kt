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

package ca.rmen.android.poetassistant.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.databinding.ActivitySettingsBinding
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {
    companion object {
        private val TAG = Constants.TAG + SettingsActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat(), ConfirmDialogFragment.ConfirmDialogListener {
        companion object {
            private const val DIALOG_TAG = "dialog_tag"
            private const val ACTION_EXPORT_FAVORITES = 1
            private const val ACTION_IMPORT_FAVORITES = 2
            private const val ACTION_CLEAR_SEARCH_HISTORY = 3
            @VisibleForTesting
            const val PREF_CATEGORY_VOICE = "PREF_CATEGORY_VOICE"
            private const val PREF_CATEGORY_NOTIFICATIONS = "PREF_CATEGORY_NOTIFICATIONS"
            private const val PREF_EXPORT_FAVORITES = "PREF_EXPORT_FAVORITES"
            private const val PREF_IMPORT_FAVORITES = "PREF_IMPORT_FAVORITES"
            private const val PREF_CLEAR_SEARCH_HISTORY = "PREF_CLEAR_SEARCH_HISTORY"
        }

        @Inject
        lateinit var mTts: Tts

        private var mRestartTtsOnResume = false
        @Inject lateinit var mPrefs: SettingsPrefs
        private lateinit var mViewModel: SettingsViewModel

        private val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                mPrefs.isWotdEnabled = isGranted
                if (!isGranted) {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        settingsIntent.data = Uri.fromParts("package", requireContext().packageName, null)
                        startActivity(settingsIntent)
                    }
                }
                findPreference<SwitchPreferenceCompat>(SettingsPrefs.PREF_WOTD_ENABLED)?.isChecked = isGranted
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            context?.let {
                DaggerHelper.getSettingsComponent(it).inject(this)
                mViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                mTts.getTtsLiveData().observe(this, mTtsObserver)
                mViewModel.snackbarText.observe(this, mSnackbarCallback)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            loadPreferences()
        }

        private fun loadPreferences() {
            context?.let {
                addPreferencesFromResource(R.xml.pref_general)
                setOnPreferenceClickListener(PREF_CLEAR_SEARCH_HISTORY, Runnable {
                    ConfirmDialogFragment.show(
                            ACTION_CLEAR_SEARCH_HISTORY,
                            getString(R.string.confirm_clear_search_history),
                            getString(R.string.action_clear),
                            childFragmentManager,
                            DIALOG_TAG)

                })
                // Hide the voice preference if we can't load any voices
                val voicePreference = findPreference<Preference>(SettingsPrefs.PREF_VOICE) as VoicePreference
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    voicePreference.loadVoices()
                }
                if (voicePreference.entries == null || voicePreference.entries.size < 2) {
                    removePreference(PREF_CATEGORY_VOICE, voicePreference)
                }
                setOnPreferenceClickListener(SettingsPrefs.PREF_VOICE_PREVIEW, Runnable { mViewModel.playTtsPreview() })

                // Hide the system tts settings if no system app can handle it
                val systemTtsSettings = findPreference<Preference>(SettingsPrefs.PREF_SYSTEM_TTS_SETTINGS)!!
                val intent = systemTtsSettings.intent
                if (intent?.resolveActivity(it.packageManager) == null) {
                    removePreference(PREF_CATEGORY_VOICE, systemTtsSettings)
                } else {
                    setOnPreferenceClickListener(systemTtsSettings, Runnable { mRestartTtsOnResume = true })
                }

                // Android O users can change the priority in the system settings.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    removePreferences(PREF_CATEGORY_NOTIFICATIONS, SettingsPrefs.PREF_WOTD_NOTIFICATION_PRIORITY)
                }

                findPreference<SwitchPreferenceCompat>(SettingsPrefs.PREF_WOTD_ENABLED)?.setOnPreferenceClickListener { preference ->
                    val wotdPref = preference as SwitchPreferenceCompat
                    if (wotdPref.isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            when {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    mPrefs.isWotdEnabled = true
                                }
                                else -> {
                                    preference.isChecked = false
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        } else {
                            mPrefs.isWotdEnabled = true
                        }
                    } else {
                        mPrefs.isWotdEnabled = false
                    }

                    true
                }

                setOnPreferenceClickListener(PREF_EXPORT_FAVORITES, Runnable { startActivityForResult(mViewModel.getExportFavoritesIntent(), ACTION_EXPORT_FAVORITES) })
                setOnPreferenceClickListener(PREF_IMPORT_FAVORITES, Runnable { startActivityForResult(mViewModel.getImportFavoritesIntent(), ACTION_IMPORT_FAVORITES) })
            }
        }

        override fun onResume() {
            super.onResume()
            if (mRestartTtsOnResume) {
                mTts.restart()
                mRestartTtsOnResume = false
            }
        }

        override fun onPause() {
            mTts.stop()
            super.onPause()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
            val uri = data?.data
            if (requestCode == ACTION_EXPORT_FAVORITES && resultCode == Activity.RESULT_OK && uri != null) {
                mViewModel.exportFavorites(uri)
            } else if (requestCode == ACTION_IMPORT_FAVORITES && resultCode == Activity.RESULT_OK && uri != null) {
                mViewModel.importFavorites(uri)
            }
        }

        override fun onOk(actionId: Int) {
            if (actionId == ACTION_CLEAR_SEARCH_HISTORY) {
                mViewModel.clearSearchHistory()
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (SettingsPrefs.PREF_VOICE == preference.key) {
                if (parentFragmentManager.findFragmentByTag(DIALOG_TAG) != null) {
                    return
                }
                val fragment = VoicePreferenceDialogFragment.newInstance(preference.key)
                fragment.setTargetFragment(this, 0)
                fragment.show(parentFragmentManager, DIALOG_TAG)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun removePreferences(categoryKey: String, vararg preferenceKeys: String) {
            preferenceKeys.forEach { removePreference(categoryKey, findPreference(it)!!) }
        }

        private fun removePreference(categoryKey: String, preference: Preference) {
            val category = preferenceScreen.findPreference<Preference>(categoryKey)!! as PreferenceCategory
            category.removePreference(preference)
        }

        private fun setOnPreferenceClickListener(preferenceKey: String, runnable: Runnable) {
            setOnPreferenceClickListener(findPreference<Preference>(preferenceKey)!!, runnable)
        }

        private fun setOnPreferenceClickListener(preference: Preference, runnable: Runnable) {
            preference.setOnPreferenceClickListener {
                runnable.run()
                false
            }
        }

        private val mSnackbarCallback = Observer<String> { snackbarText ->
            view?.let {
                if (!TextUtils.isEmpty(snackbarText)) {
                    Snackbar.make(it, snackbarText!!, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        private val mTtsObserver = Observer<TtsState?> { ttsState ->
            Log.v(TAG, "ttsState = $ttsState")
            if (ttsState != null
                    && ttsState.previousStatus == TtsState.TtsStatus.UNINITIALIZED
                    && ttsState.currentStatus == TtsState.TtsStatus.INITIALIZED) {
                preferenceScreen.removeAll()
                loadPreferences()
            }
        }
    }
}
