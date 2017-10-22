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
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;
import ca.rmen.android.poetassistant.databinding.LiveDataMapping;

public class ResultListHeaderViewModel extends AndroidViewModel {

    public final ObservableField<String> query = new ObservableField<>();
    public final ObservableField<String> filter = new ObservableField<>();
    public final ObservableBoolean isFavorite = new ObservableBoolean();
    public final ObservableBoolean showHeader = new ObservableBoolean();

    final MutableLiveData<String> snackbarText = new MutableLiveData<>();
    final LiveData<Boolean> isFavoriteLiveData;
    final LiveData<Tts.TtsState> ttsStateLiveData;

    @Inject Favorites mFavorites;
    @Inject Tts mTts;

    public ResultListHeaderViewModel(Application application) {
        super(application);
        DaggerHelper.getMainScreenComponent(application).inject(this);
        ttsStateLiveData = mTts.getTtsLiveData();
        // Expose a LiveData to the fragment, so it can update the star icon when the favorite
        // value changes in the DB. This is relevant when the favorite value changes because the star
        // was clicked in *another* fragment. If we only had one screen where the user could change
        // the favorites, a simple databinding between the star checkbox and this ViewModel would
        // suffice to sync the db and the UI.
        isFavoriteLiveData = Transformations.switchMap(LiveDataMapping.fromObservableField(query),
                query -> mFavorites.getIsFavoriteLiveData(query));
        // When the user taps on the star icon, update the favorite in the DB
        isFavorite.addOnPropertyChangedCallback(
                new BindingCallbackAdapter(() -> mFavorites.saveFavorite(query.get(), isFavorite.get())));
    }

    public void speak() {
        mTts.speak(query.get());
    }

    public void clearFilter() {
        filter.set(null);
    }

    public void webSearch() {
        WebSearch.search(getApplication(), query.get());
    }

    void clearFavorites () {
        mFavorites.clear();
        snackbarText.setValue(getApplication().getString(R.string.favorites_cleared));
    }

    @Override
    public String toString() {
        return "RTEntry{" +
                "query ='" + query.get() + '\'' +
                ", filter ='" + filter.get() + '\'' +
                ", isFavorite =" + isFavorite.get() +
                '}';
    }
}
