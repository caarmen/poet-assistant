/*
 * Copyright (c) 2016 Carmen Alvarez
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
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.Favorites;
import ca.rmen.android.poetassistant.main.dictionaries.Patterns;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;

public class FavoritesLoader extends AsyncTaskLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + FavoritesLoader.class.getSimpleName();

    private final Favorites mFavorites;
    private ResultListData<RTEntry> mResult;

    public FavoritesLoader(Context context) {
        super(context);
        mFavorites = new Favorites(context);
    }

    @Override
    public ResultListData<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground()");

        List<RTEntry> data = new ArrayList<>();

        Set<String> favorites = mFavorites.getFavorites();
        if (favorites.isEmpty()) return emptyResult();

        TreeSet<String> sortedFavorites = new TreeSet<>(favorites);
        int i = 0;
        for (String favorite : sortedFavorites) {
            @ColorRes int color = (i % 2 == 0)? R.color.row_background_color_even : R.color.row_background_color_odd;
            data.add(new RTEntry(
                    RTEntry.Type.WORD,
                    favorite,
                    ContextCompat.getColor(getContext(), color),
                    true));
            i++;
        }
        return new ResultListData<>(getContext().getString(R.string.favorites_list_header), false, data);
    }

    private ResultListData<RTEntry> emptyResult() {
        return new ResultListData<>(getContext().getString(R.string.favorites_list_header), false, new ArrayList<>());
    }

    @Override
    public void deliverResult(ResultListData<RTEntry> data) {
        Log.d(TAG, "deliverResult() called with: data = [" + data + "]");
        mResult = data;
        if (isStarted()) super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading() called");
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
        if (mResult != null) super.deliverResult(mResult);
        else forceLoad();
    }

    @Override
    protected void onReset() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onReset();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFavoritesChanged(Favorites.OnFavoritesChanged event) {
        onContentChanged();
    }

}
