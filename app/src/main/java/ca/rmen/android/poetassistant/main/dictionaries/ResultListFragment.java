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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.Collections;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.VectorCompat;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFilterListener;


public class ResultListFragment<T> extends Fragment
        implements
        LoaderManager.LoaderCallbacks<ResultListData<T>>,
        OnFilterListener {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private FragmentResultListBinding mBinding;
    private ResultListHeaderFragment mHeaderFragment;

    private Tab mTab;
    private ResultListAdapter<T> mAdapter;
    private ResultListData<T> mData;
    @Inject Tts mTts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        ResultListFactory.inject(mTab, this);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setHasFixedSize(true);
        FragmentManager fragmentManager = getChildFragmentManager();
        mHeaderFragment = (ResultListHeaderFragment) fragmentManager.findFragmentById(R.id.result_list_header);
        mHeaderFragment.setTab(mTab);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, mTab + ": onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        //noinspection unchecked
        mAdapter = (ResultListAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        mBinding.recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(mTab.ordinal(), getArguments(), this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Share.share(getActivity(), mTab,
                    mHeaderFragment.getHeader(),
                    mHeaderFragment.getFilter(),
                    mData.data);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(mAdapter.getItemCount() > 0);
    }

    public void query(String query) {
        Log.d(TAG, mTab + ": query() called with: " + "query = [" + query + "]");
        mHeaderFragment.setHeader(query);
        mHeaderFragment.setFilter(null);
        Bundle args = new Bundle(1);
        args.putString(EXTRA_QUERY, query);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    private void filter(String filter) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mHeaderFragment.getHeader());
        args.putString(EXTRA_FILTER, filter);
        getLoaderManager().restartLoader(mTab.ordinal(), args, this);
    }

    @Override
    public Loader<ResultListData<T>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, mTab + ": onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        mAdapter.clear();
        mData = null;

        String query = "";
        String filter = "";
        if (args != null) {
            query = args.getString(EXTRA_QUERY);
            filter = args.getString(EXTRA_FILTER);
            mHeaderFragment.setHeader(query);
            mData = new ResultListData<>(query, false, Collections.emptyList());
        }
        mBinding.empty.setVisibility(View.GONE);
        mHeaderFragment.show();
        mBinding.recyclerView.scrollToPosition(0); // why do I have to do this?

        getActivity().supportInvalidateOptionsMenu();

        //noinspection unchecked
        return (Loader<ResultListData<T>>) ResultListFactory.createLoader(mTab, getActivity(), query, filter);
    }

    @Override
    public void onLoadFinished(Loader<ResultListData<T>> loader, ResultListData<T> data) {
        Log.d(TAG, mTab + ": onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mAdapter.clear();
        //noinspection unchecked
        mAdapter.addAll(data.data);
        mData = data;
        updateUi();

        // Hide the keyboard
        mBinding.recyclerView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBinding.recyclerView.getWindowToken(), 0);
    }

    @Override
    public void onLoaderReset(Loader<ResultListData<T>> loader) {
        Log.d(TAG, mTab + ": onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.clear();
        mData = null;
        updateUi();
    }

    @Override
    public void onFilterSubmitted(String filter) {
        filter(filter);
    }


    private void updateUi() {
        Log.d(TAG, mTab + ": updateUi() called with: " + "");
        if (mAdapter.getItemCount() > 0 || !TextUtils.isEmpty(mHeaderFragment.getHeader())) {
            mHeaderFragment.show();
        } else {
            mHeaderFragment.hide();
        }

        String query = mData == null ? null : mData.matchedWord;
        mHeaderFragment.setHeader(query);
        // If we have an empty list because the user didn't enter any search term,
        // we'll show a text to tell them to search.
        if (TextUtils.isEmpty(query)) {
            String emptySearch = getString(R.string.empty_list_without_query);
            ImageSpan imageSpan = VectorCompat.createVectorImageSpan(getActivity(), R.drawable.ic_action_search_dark);
            SpannableStringBuilder ssb = new SpannableStringBuilder(emptySearch);
            int iconIndex = emptySearch.indexOf("%s");
            ssb.setSpan(imageSpan, iconIndex, iconIndex + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mBinding.empty.setText(ssb, TextView.BufferType.SPANNABLE);
        }
        // If the user entered a query and there are no matches, show the normal "no results" text.
        else {
            String noResults = ResultListFactory.getEmptyListText(getContext(), mTab, query);
            mBinding.empty.setText(noResults);
        }
        if (mData == null || mData.data.isEmpty()) {
            mBinding.empty.setVisibility(View.VISIBLE);
            mBinding.recyclerView.setVisibility(View.GONE);
        } else {
            mBinding.empty.setVisibility(View.GONE);
            mBinding.recyclerView.setVisibility(View.VISIBLE);
        }

        if (mData != null) mHeaderFragment.setFavorite(mData.isFavorite);

        getActivity().supportInvalidateOptionsMenu();
    }

}
