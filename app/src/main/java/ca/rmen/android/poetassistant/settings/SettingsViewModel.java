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

import android.annotation.TargetApi;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.dictionaries.search.SuggestionsProvider;
import ca.rmen.android.poetassistant.main.reader.PoemFile;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsViewModel extends AndroidViewModel {

    @Inject
    Favorites mFavorites;
    @Inject
    Tts mTts;

    final MutableLiveData<String> snackbarText = new MutableLiveData<>();
    private final SettingsChangeListener mListener;

    public SettingsViewModel(Application application) {
        super(application);
        mListener = new SettingsChangeListener(application);
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mListener);
        DaggerHelper.INSTANCE.getSettingsComponent(application).inject(this);
    }

    void playTtsPreview() {
        if (mTts.isSpeaking()) mTts.stop();
        else mTts.speak(getApplication().getString(R.string.pref_voice_preview_text));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    Intent getExportFavoritesIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, getApplication().getString(R.string.export_favorites_default_filename));
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    Intent getImportFavoritesIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        return intent;
    }

    void exportFavorites(Uri uri) {
        String fileDisplayName = PoemFile.Companion.readDisplayName(getApplication(), uri);
        Completable.fromAction(() -> mFavorites.exportFavorites(getApplication(), uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> snackbarText.setValue(getApplication().getString(R.string.export_favorites_success, fileDisplayName)),
                        throwable -> snackbarText.setValue(getApplication().getString(R.string.export_favorites_error, fileDisplayName)));

    }

    void importFavorites(Uri uri) {
        String fileDisplayName = PoemFile.Companion.readDisplayName(getApplication(), uri);
        Completable.fromAction(() -> mFavorites.importFavorites(getApplication(), uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> snackbarText.setValue(getApplication().getString(R.string.import_favorites_success, fileDisplayName)),
                        throwable -> snackbarText.setValue(getApplication().getString(R.string.import_favorites_error, fileDisplayName)));

    }

    void clearSearchHistory() {
        Completable.fromRunnable(() -> getApplication().getContentResolver().delete(SuggestionsProvider.CONTENT_URI, null, null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> snackbarText.setValue(getApplication().getString(R.string.search_history_cleared)));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        android.preference.PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mListener);
    }
}
