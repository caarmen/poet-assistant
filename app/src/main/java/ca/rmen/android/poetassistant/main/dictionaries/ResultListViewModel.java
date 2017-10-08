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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.SharedPreferences;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.preference.PreferenceManager;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class ResultListViewModel<T> extends AndroidViewModel {
    private static final String TAG = Constants.TAG + ResultListViewModel.class.getSimpleName();

    final ObservableField<ResultListData<T>> data = new ObservableField<>();
    public final ObservableBoolean isDataAvailable = new ObservableBoolean();
    public final ObservableField<Settings.Layout> layout = new ObservableField<>();
    private ResultListAdapter<T> mAdapter;

    ResultListViewModel(Application application) {
        super(application);
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mPrefsListener);
    }

    void setAdapter(ResultListAdapter<T> adapter) {
        mAdapter = adapter;
    }

    private void updateDataAvailable() {
        isDataAvailable.set(mAdapter != null && mAdapter.getItemCount() > 0);
        isDataAvailable.notifyChange();
    }

    void setData(ResultListData<T> loadedData) {
        Log.v(TAG, "setData " + loadedData);
        mAdapter.clear();
        if (loadedData != null) mAdapter.addAll(loadedData.data);
        data.set(loadedData);
        updateDataAvailable();
    }

    String getUsedQueryWord() {
        return data.get() == null ? null : data.get().matchedWord;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = (sharedPreferences, key) -> {
        if (Settings.PREF_LAYOUT.equals(key)) {
            layout.set(Settings.getLayout(SettingsPrefs.get(getApplication())));
        }
    };

}
