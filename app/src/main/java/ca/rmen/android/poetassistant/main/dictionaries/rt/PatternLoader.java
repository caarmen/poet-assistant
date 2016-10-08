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

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.Favorites;
import ca.rmen.android.poetassistant.main.dictionaries.Patterns;
import ca.rmen.android.poetassistant.main.dictionaries.ResultListData;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;

public class PatternLoader extends AsyncTaskLoader<ResultListData<RTEntry>> {

    private static final String TAG = Constants.TAG + PatternLoader.class.getSimpleName();

    private final String mQuery;
    private final Favorites mFavorites;
    private ResultListData<RTEntry> mResult;

    public PatternLoader(Context context, String query) {
        super(context);
        mQuery = query;
        mFavorites = new Favorites(context);
    }

    @Override
    public ResultListData<RTEntry> loadInBackground() {
        Log.d(TAG, "loadInBackground() called with: query = " + mQuery);

        List<RTEntry> data = new ArrayList<>();
        if (TextUtils.isEmpty(mQuery)) return emptyResult();
        Dictionary dictionary = Dictionary.getInstance(getContext());
        String[] matches = dictionary.findWordsByPattern(Patterns.convertForSqlite(mQuery));

        if (matches.length == 0) {
            return emptyResult();
        }

        Set<String> favorites = mFavorites.getFavorites();

        if (!favorites.isEmpty()) {
            Arrays.sort(matches, new MatchComparator(favorites));
        }

        for (int i=0; i < matches.length; i++) {
            @ColorRes int color = (i % 2 == 0)? R.color.row_background_color_even : R.color.row_background_color_odd;
            data.add(new RTEntry(
                    RTEntry.Type.WORD,
                    matches[i],
                    ContextCompat.getColor(getContext(), color),
                    favorites.contains(matches[i])));
        }
        if (matches.length == Patterns.MAX_RESULTS) {
            data.add(new RTEntry(
                    RTEntry.Type.SUBHEADING,
                    getContext().getString(R.string.max_results, Patterns.MAX_RESULTS))
            );
        }
        return new ResultListData<>(mQuery, favorites.contains(mQuery), data);
    }

    private static class MatchComparator implements Comparator<String> {
        private final Set<String> mFavorites;
        MatchComparator(Set<String> favorites) {
            mFavorites = favorites;
        }

        @Override
        public int compare(String o1, String o2) {
            if (mFavorites.contains(o1) && ! mFavorites.contains(o2)) {
                return -1;
            }
            if (mFavorites.contains(o2) && ! mFavorites.contains(o1)) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

    private ResultListData<RTEntry> emptyResult() {
        return new ResultListData<>(mQuery, false, new ArrayList<>());
    }

    @Override
    public void deliverResult(ResultListData<RTEntry> data) {
        Log.d(TAG, "deliverResult() called with: query = " + mQuery + ", data = [" + data + "]");
        mResult = data;
        if (isStarted()) super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading() called with: query = " + mQuery);
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
