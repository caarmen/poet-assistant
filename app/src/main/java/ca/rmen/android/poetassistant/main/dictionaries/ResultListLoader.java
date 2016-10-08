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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ca.rmen.android.poetassistant.Constants;

public abstract class ResultListLoader<T> extends AsyncTaskLoader<T> {

    private static final String TAG = Constants.TAG + ResultListLoader.class.getSimpleName();

    protected final Favorites mFavorites;
    private T mResult;

    protected ResultListLoader(Context context) {
        super(context);
        mFavorites = new Favorites(context);
    }

    @Override
    public void deliverResult(T data) {
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
