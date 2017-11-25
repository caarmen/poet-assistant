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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListLiveData;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

public class FavoritesLiveData extends ResultListLiveData<ResultListData<RTEntryViewModel>> {

    private static final String TAG = Constants.TAG + FavoritesLiveData.class.getSimpleName();

    @Inject SettingsPrefs mPrefs;
    @Inject Favorites mFavorites;
    public FavoritesLiveData(Context context) {
        super(context);
        DaggerHelper.INSTANCE.getMainScreenComponent(context).inject(this);
    }

    @Override
    public ResultListData<RTEntryViewModel> loadInBackground() {
        Log.d(TAG, "loadInBackground()");

        List<RTEntryViewModel> data = new ArrayList<>();

        Set<String> favorites = mFavorites.getFavorites();
        if (favorites.isEmpty()) return emptyResult();

        TreeSet<String> sortedFavorites = new TreeSet<>(favorites);
        Settings.Layout layout = Settings.getLayout(mPrefs);
        int i = 0;
        for (String favorite : sortedFavorites) {
            @ColorRes int color = (i % 2 == 0)? R.color.row_background_color_even : R.color.row_background_color_odd;
            data.add(new RTEntryViewModel(
                    getContext(),
                    RTEntryViewModel.Type.WORD,
                    favorite,
                    ContextCompat.getColor(getContext(), color),
                    true,
                    layout == Settings.Layout.EFFICIENT));
            i++;
        }
        return new ResultListData<>(getContext().getString(R.string.favorites_list_header), data);
    }

    private ResultListData<RTEntryViewModel> emptyResult() {
        return new ResultListData<>(getContext().getString(R.string.favorites_list_header), new ArrayList<>());
    }

}
