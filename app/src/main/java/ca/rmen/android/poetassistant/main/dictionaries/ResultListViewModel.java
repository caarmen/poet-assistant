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
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.Favorite;
import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.compat.VectorCompat;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class ResultListViewModel<T> extends AndroidViewModel {
    private static final String TAG = Constants.TAG + ResultListViewModel.class.getSimpleName();

    public final ObservableBoolean isDataAvailable = new ObservableBoolean();
    public final ObservableField<CharSequence> emptyText = new ObservableField<>();
    final MutableLiveData<Settings.Layout> layout = new MutableLiveData<>();
    final MutableLiveData<Boolean> showHeader = new MutableLiveData<>();
    final MutableLiveData<String> usedQueryWord = new MutableLiveData<>();
    private ResultListAdapter<T> mAdapter;
    private final Tab mTab;
    @Inject
    Favorites mFavorites;

    static class QueryParams {
        final String word;
        final String filter;

        QueryParams(String word, String filter) {
            this.word = word;
            this.filter = filter;
        }

        @Override
        public String toString() {
            return "QueryParams{" +
                    "word='" + word + '\'' +
                    ", filter='" + filter + '\'' +
                    '}';
        }
    }
    private final MutableLiveData<QueryParams> mQueryParams = new MutableLiveData<>();
    final LiveData<ResultListData<T>> resultListDataLiveData;
    final LiveData<List<Favorite>> favoritesLiveData;

    ResultListViewModel(Application application, Tab tab) {
        super(application);
        mTab = tab;
        ResultListFactory.INSTANCE.inject(application, tab, this);
        emptyText.set(getNoQueryEmptyText());
        PreferenceManager.getDefaultSharedPreferences(application).registerOnSharedPreferenceChangeListener(mPrefsListener);
        favoritesLiveData = mFavorites.getFavoritesLiveData();
        //noinspection unchecked
        resultListDataLiveData = Transformations.switchMap(mQueryParams, queryParams -> (LiveData<ResultListData<T>>) ResultListFactory.INSTANCE.createLiveData(mTab, getApplication(), queryParams.word, queryParams.filter));
    }

    void setQueryParams(QueryParams queryParams) {
        Log.v(TAG, mTab + ": setQueryParams " + queryParams);
        if (!TextUtils.isEmpty(queryParams.word) || ResultListFactory.INSTANCE.isLoadWithoutQuerySupported(mTab)) {
            mQueryParams.setValue(queryParams);
        }
    }
    void setAdapter(ResultListAdapter<T> adapter) {
        mAdapter = adapter;
    }

    void share(Tab tab, String query, String filter) {
        Share.INSTANCE.share(getApplication(), tab, query, filter, mAdapter.getAll());
    }

    private void updateDataAvailable() {
        isDataAvailable.set(mAdapter != null && mAdapter.getItemCount() > 0);
        isDataAvailable.notifyChange();
    }

    void setData(ResultListData<T> loadedData) {
        Log.v(TAG, mTab + ": setData " + loadedData);
        mAdapter.clear();
        if (loadedData != null) mAdapter.addAll(loadedData.data);
        boolean hasQuery = loadedData != null && !TextUtils.isEmpty(loadedData.matchedWord);
        if (!hasQuery) {
            emptyText.set(getNoQueryEmptyText());
        } else if (loadedData.data != null) {
            emptyText.set(getNoResultsEmptyText(loadedData.matchedWord));
        } else {
            emptyText.set(null);
        }
        showHeader.setValue(hasQuery);
        if (loadedData != null) {
            usedQueryWord.setValue(loadedData.matchedWord);
        }
        updateDataAvailable();
    }

    // If we have an empty list because the user didn't enter any search term,
    // we'll show a text to tell them to search.
    private CharSequence getNoQueryEmptyText() {
        String emptySearch = getApplication().getString(R.string.empty_list_without_query);
        ImageSpan imageSpan = VectorCompat.INSTANCE.createVectorImageSpan(getApplication(), R.drawable.ic_action_search_dark);
        SpannableStringBuilder ssb = new SpannableStringBuilder(emptySearch);
        int iconIndex = emptySearch.indexOf("%s");
        ssb.setSpan(imageSpan, iconIndex, iconIndex + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return ssb;
    }

    // If the user entered a query and there are no matches, show the normal "no results" text.
    private CharSequence getNoResultsEmptyText(String query) {
        return ResultListFactory.INSTANCE.getEmptyListText(getApplication(), mTab, query);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        PreferenceManager.getDefaultSharedPreferences(getApplication()).unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = (sharedPreferences, key) -> {
        if (Settings.PREF_LAYOUT.equals(key)) {
            layout.setValue(Settings.Companion.getLayout(SettingsPrefs.get(getApplication())));
        }
    };
}
