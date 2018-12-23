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

package ca.rmen.android.poetassistant.main.dictionaries

import androidx.lifecycle.LiveData
import android.content.Context
import ca.rmen.android.poetassistant.dagger.DaggerHelper

abstract class ResultListLiveData<T> protected constructor(protected val context: Context) : LiveData<T>() {
    private var mIsLoading = false

    protected abstract fun loadInBackground(): T
    override fun onActive() {
        if (value == null && !mIsLoading) {
            mIsLoading = true
            val threading = DaggerHelper.getMainScreenComponent(context).getThreading()
            threading.execute(
                    { loadInBackground() },
                    {
                        value = it
                        mIsLoading = false
                    }
            )
        }
    }
}
