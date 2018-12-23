/*
 * Copyright (c) 2018 Carmen Alvarez
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

package ca.rmen.android.poetassistant

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import ca.rmen.android.poetassistant.main.dictionaries.ResultListAdapter

object ResultListIdlingHelper {
    private val TAG = Constants.TAG + ResultListIdlingHelper::class.java.simpleName

    fun <T> setupIdlingResource(adapter: ResultListAdapter<T>,
                                oldList: MutableList<T>?, newList: MutableList<T>?,
                                countingIdlingResource: CountingIdlingResource) {
        Log.v(TAG, "submitList: oldList = $oldList, newList = $newList, countingIdlingResource = ${countingIdlingResource.name}")
        // Don't register an observer if it won't be called.
        // It won't be called if the list is the same
        if (oldList != null && newList != null && oldList == newList) {
            return
        }
        // It won't be called if we're just changing from one empty list to another empty list.
        if ((oldList == null || oldList.isEmpty()) && (newList == null || newList.isEmpty())) {
            return
        }
        // We expect one of the onXYZ() callbacks if the list data has changed.
        countingIdlingResource.increment()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            private fun unregister() {
                adapter.unregisterAdapterDataObserver(this)
                Handler(Looper.getMainLooper()).postDelayed({ countingIdlingResource.decrement() }, 200)
            }

            override fun onChanged() {
                Log.v(TAG, "${countingIdlingResource.name} onChanged")
                unregister()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                Log.v(TAG, "${countingIdlingResource.name} onItemRangeRemoved")
                unregister()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                Log.v(TAG, "${countingIdlingResource.name} onItemRangeMoved")
                unregister()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.v(TAG, "${countingIdlingResource.name} onItemRangeInserted")
                unregister()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                Log.v(TAG, "${countingIdlingResource.name} onItemRangeChanged")
                unregister()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                Log.v(TAG, "${countingIdlingResource.name} onItemRangeChanged2")
            }
        })
    }
}
