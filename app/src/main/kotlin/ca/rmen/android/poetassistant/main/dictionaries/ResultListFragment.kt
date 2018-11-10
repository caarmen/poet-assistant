/*
 * Copyright (c) 2016-2018 Carmen Alvarez
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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.Favorite
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper
import ca.rmen.android.poetassistant.main.Tab
import ca.rmen.android.poetassistant.settings.SettingsPrefs

class ResultListFragment<out T> : Fragment() {
    companion object {
        private val TAG = Constants.TAG + ResultListFragment::class.java.simpleName
        const val EXTRA_TAB = "tab"
        const val EXTRA_QUERY = "query"
        private const val EXTRA_FILTER = "filter"
    }

    private lateinit var mBinding: FragmentResultListBinding
    private lateinit var mViewModel: ResultListViewModel<T>
    private lateinit var mHeaderViewModel: ResultListHeaderViewModel

    private var mTab: Tab? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mTab = arguments?.getSerializable(EXTRA_TAB) as Tab
        mTab?.let {
            Log.v(TAG, "$mTab onCreateView")
            mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false)
            mBinding.recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            mBinding.recyclerView.setHasFixedSize(true)
            @Suppress("UNCHECKED_CAST")
            mViewModel = ResultListFactory.createViewModel(it, this) as ResultListViewModel<T>
            mBinding.viewModel = mViewModel
            mViewModel.layout.observe(this, mLayoutSettingChanged)
            mViewModel.showHeader.observe(this, mShowHeaderChanged)
            mViewModel.usedQueryWord.observe(this, mUsedQueryWordChanged)
            mViewModel.isDataAvailable.addOnPropertyChangedCallback(mDataAvailableChanged)
            mHeaderViewModel = ViewModelProviders.of(this).get(ResultListHeaderViewModel::class.java)
            mHeaderViewModel.filter.addOnPropertyChangedCallback(mFilterChanged)
            var headerFragment = childFragmentManager.findFragmentById(R.id.result_list_header)
            if (headerFragment == null) {
                headerFragment = ResultListHeaderFragment.newInstance(it)
                childFragmentManager.beginTransaction().replace(R.id.result_list_header, headerFragment).commit()
            }
            mViewModel.favoritesLiveData.observe(this, mFavoritesObserver)
            mViewModel.resultListDataLiveData.observe(this, Observer { data -> mViewModel.setData(data) })
            return mBinding.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$mTab: onActivityCreated: savedInstanceState=$savedInstanceState")
        activity?.let {
            val tab = mTab
            if (tab != null) {
                @Suppress("UNCHECKED_CAST")
                val adapter = ResultListFactory.createAdapter(it, tab) as ResultListAdapter<T>
                mViewModel.setAdapter(adapter)
                mBinding.recyclerView.adapter = adapter
            }
        }
    }

    override fun onStart() {
        Log.v(TAG, "$mTab : onStart")
        super.onStart()
        queryFromArguments()
        Log.v(TAG, "$mTab: onStart: invalidateOptionsMenu")
        activity?.invalidateOptionsMenu()
    }

    override fun onDestroyView() {
        Log.v(TAG, "$mTab onDestroyView")
        mViewModel.isDataAvailable.removeOnPropertyChangedCallback(mDataAvailableChanged)
        mHeaderViewModel.filter.removeOnPropertyChangedCallback(mFilterChanged)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            mHeaderViewModel.query.get()?.let {
                mViewModel.share(it, mHeaderViewModel.filter.get())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_share).isEnabled = mViewModel.isDataAvailable.get()
    }

    private fun queryFromArguments() {
        Log.v(TAG, "$mTab: queryFromArguments: $arguments")
        arguments?.let {
            if (it.containsKey(EXTRA_QUERY)) {
                mViewModel.setQueryParams(ResultListViewModel.QueryParams(it.getString(EXTRA_QUERY), it.getString(EXTRA_FILTER)))
            }
        }
    }

    fun query(query: String) {
        Log.d(TAG, "$mTab : query: $query")
        mHeaderViewModel.filter.set(null)
        if (userVisibleHint) {
            AppBarLayoutHelper.disableAutoHide(activity)
            AppBarLayoutHelper.forceExpandAppBarLayout(activity)
        }
        mViewModel.setQueryParams(ResultListViewModel.QueryParams(query, null))
        Log.v(TAG, "$mTab: query: invalidate options menu")
        activity?.invalidateOptionsMenu()
    }

    /**
     * Enable auto-hiding the toolbar only if not all items in the list are visible.
     */
    fun enableAutoHideIfNeeded() {
        Log.v(TAG, "$mTab: enableAutoHideIfNeeded")
        if (mTab != null && mBinding.recyclerView.adapter != null) {
            val lastVisibleItemPosition = (mBinding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            @Suppress("UNCHECKED_CAST")
            val itemCount = (mBinding.recyclerView.adapter as ResultListAdapter<T>).itemCount
            Log.v(TAG, "$mTab: enableAutoHideIfNeeded: last visibleItem $lastVisibleItemPosition, item count $itemCount")
            if (itemCount > 0 && lastVisibleItemPosition < itemCount - 1) {
                AppBarLayoutHelper.enableAutoHide(activity)
            } else {
                AppBarLayoutHelper.disableAutoHide(activity)
            }
            AppBarLayoutHelper.forceExpandAppBarLayout(activity)
        }
    }

    private val mRecyclerViewLayoutListener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            if (userVisibleHint) {
                enableAutoHideIfNeeded()
            }
            mBinding.recyclerView.removeOnLayoutChangeListener(this)
        }
    }

    private val mDataAvailableChanged = BindingCallbackAdapter(object : BindingCallbackAdapter.Callback {
        override fun onChanged() {
            mBinding.recyclerView.addOnLayoutChangeListener(mRecyclerViewLayoutListener)
            Log.v(TAG, "$mTab: dataAvailableChanged: invalidateOptionsMenu")
            activity?.invalidateOptionsMenu()

            // Hide the keyboard
            mBinding.recyclerView.requestFocus()
            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.
                    hideSoftInputFromWindow(mBinding.recyclerView.windowToken, 0)
        }
    })

    private val mFilterChanged = BindingCallbackAdapter(object : BindingCallbackAdapter.Callback {
        override fun onChanged() {
            reload()
        }
    })

    private val mShowHeaderChanged = Observer<Boolean> { showHeader -> mHeaderViewModel.showHeader.set(showHeader == true) }

    private val mLayoutSettingChanged = Observer<SettingsPrefs.Layout> { _ -> reload() }

    private val mFavoritesObserver = Observer<List<Favorite>> { _ -> reload() }

    private val mUsedQueryWordChanged = Observer<String> { usedQueryWord -> mHeaderViewModel.query.set(usedQueryWord) }

    private fun reload() {
        Log.v(TAG, "$mTab: reload: query=${mHeaderViewModel.query.get()}, filter=${mHeaderViewModel.filter.get()}")
        mViewModel.setQueryParams(ResultListViewModel.QueryParams(mHeaderViewModel.query.get(), mHeaderViewModel.filter.get()))
    }
}
