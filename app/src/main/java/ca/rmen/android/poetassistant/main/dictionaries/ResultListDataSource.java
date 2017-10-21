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

import android.arch.paging.TiledDataSource;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.Tab;

class ResultListDataSource<T> extends TiledDataSource<T> {

    private static final String TAG = Constants.TAG + ResultListDataSource.class.getSimpleName();
    private final ResultListData<T> mData;
    private final Tab mTab;

    ResultListDataSource(Tab tab, ResultListData<T> data) {
        mTab = tab;
        mData = data;
    }

    @Override
    public int countItems() {
        Log.v(TAG, mTab + ": countItems");
        if (mData != null && mData.data != null) {
            return mData.data.size();
        }
        return 0;
    }

    @Override
    public List<T> loadRange(int startPosition, int count) {
        Log.v(TAG, mTab + ": loadRange: start=" + startPosition + ", count=" + count);
        if (mData != null && mData.data != null && !mData.data.isEmpty() && startPosition <= mData.data.size() - 1) {
            List<T> result = mData.data.subList(startPosition, Math.min(startPosition + count, mData.data.size()));
            Log.v(TAG, mTab + ": returning " + result);
            return result;
        }
        return new ArrayList<>();
    }
}
