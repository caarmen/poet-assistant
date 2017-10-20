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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class ResultListLiveData<T> extends LiveData<T> {

    private final Context mContext;
    private boolean mIsLoading = false;

    protected ResultListLiveData(Context context) {
        mContext = context.getApplicationContext();
    }

    protected Context getContext() {
        return mContext;
    }

    protected abstract T loadInBackground();

    @Override
    protected void onActive() {
        if (getValue() == null && !mIsLoading) {
            mIsLoading = true;
            Single.fromCallable(this::loadInBackground)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                        setValue(data);
                        mIsLoading = false;
                    });
        }
    }
}
