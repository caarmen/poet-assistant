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

import android.annotation.TargetApi
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.preference.PreferenceManager
import ca.rmen.android.poetassistant.Favorites
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.Threading
import ca.rmen.android.poetassistant.Tts
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsProvider
import ca.rmen.android.poetassistant.main.reader.PoemFile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val mFavorites: Favorites,
    private val mTts: Tts,
    private val mThreading: Threading,
    private val dictionary: Dictionary,
    private val settingsPrefs: SettingsPrefs,
) : AndroidViewModel(application) {

    val snackbarText = MutableLiveData<String>()
    private val mListener: SettingsChangeListener = SettingsChangeListener(application, dictionary, settingsPrefs)

    init {
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mListener)
    }

    fun playTtsPreview() {
        if (mTts.isSpeaking()) mTts.stop()
        else mTts.speak(getApplication<Application>().getString(R.string.pref_voice_preview_text))
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getExportFavoritesIntent(): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
            .putExtra(
                Intent.EXTRA_TITLE,
                getApplication<Application>().getString(R.string.export_favorites_default_filename)
            )

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getImportFavoritesIntent(): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")

    fun exportFavorites(uri: Uri) {
        val fileDisplayName = PoemFile.readDisplayName(getApplication(), uri)
        mThreading.execute({ mFavorites.exportFavorites(getApplication(), uri) },
            { snackbarText.value = getApplication<Application>().getString(R.string.export_favorites_success, fileDisplayName) },
            { snackbarText.value = getApplication<Application>().getString(R.string.export_favorites_error, fileDisplayName) })
    }

    fun importFavorites(uri: Uri) {
        val fileDisplayName = PoemFile.readDisplayName(getApplication(), uri)
        mThreading.execute({ mFavorites.importFavorites(getApplication(), uri) },
            { snackbarText.value = getApplication<Application>().getString(R.string.import_favorites_success, fileDisplayName) },
            { snackbarText.value = getApplication<Application>().getString(R.string.import_favorites_error, fileDisplayName) })
    }

    fun clearSearchHistory() {
        mThreading.execute( { getApplication<Application>().contentResolver.delete(SuggestionsProvider.CONTENT_URI, null, null) },
            { snackbarText.value = getApplication<Application>().getString(R.string.search_history_cleared) })
    }

    override fun onCleared() {
        super.onCleared()
        PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mListener)
    }
}
