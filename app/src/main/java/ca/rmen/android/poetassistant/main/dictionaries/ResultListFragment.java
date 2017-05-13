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

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import ca.rmen.android.poetassistant.compat.VectorCompat;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFilterListener;
import ca.rmen.android.poetassistant.settings.Settings;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;


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
    private ResultListViewModel<T> mViewModel;

    private Tab mTab;
    @Inject Tts mTts;
    @Inject SettingsPrefs mPrefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        Log.v(TAG, mTab + " onCreateView");
        ResultListFactory.inject(mTab, this);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setHasFixedSize(true);
        mHeaderFragment = (ResultListHeaderFragment) getChildFragmentManager().findFragmentById(R.id.result_list_header);
        if (mHeaderFragment == null) {
            mHeaderFragment = ResultListHeaderFragment.newInstance(mTab);
            getChildFragmentManager().beginTransaction().replace(R.id.result_list_header, mHeaderFragment).commit();
        }
        //noinspection unchecked
        mViewModel = (ResultListViewModel<T>) ResultListFactory.createViewModel(mTab);
        mBinding.setViewModel(mViewModel);
        mPrefs.registerOnSharedPreferenceChangeListener(mPrefsListener);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, mTab + ": onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        //noinspection unchecked
        ResultListAdapter<T> adapter = (ResultListAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        mViewModel.setAdapter(adapter);
        mBinding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        Log.v(TAG, mTab + " onStart");
        super.onStart();
        getLoaderManager().initLoader(mTab.ordinal(), getArguments(), this);
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, mTab + " onDestroyView");
        mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Share.share(getActivity(), mTab,
                    mHeaderFragment.getHeader(),
                    mHeaderFragment.getFilter(),
                    mViewModel.data.get().data);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(mViewModel.isDataAvailable());
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

        String query = "";
        String filter = "";
        if (args != null && args.containsKey(EXTRA_QUERY)) {
            query = args.getString(EXTRA_QUERY);
            filter = args.getString(EXTRA_FILTER);
            mHeaderFragment.setHeader(query);
            mViewModel.setData(new ResultListData<>(query, Collections.emptyList()));
        } else {
            mViewModel.setData(null);
        }
        mHeaderFragment.show();

        getActivity().supportInvalidateOptionsMenu();

        //noinspection unchecked
        return (Loader<ResultListData<T>>) ResultListFactory.createLoader(mTab, getActivity(), query, filter);
    }

    @Override
    public void onLoadFinished(Loader<ResultListData<T>> loader, ResultListData<T> data) {
        Log.d(TAG, mTab + ": onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mViewModel.setData(data);
        updateUi();

        // Hide the keyboard
        mBinding.recyclerView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBinding.recyclerView.getWindowToken(), 0);
    }

    @Override
    public void onLoaderReset(Loader<ResultListData<T>> loader) {
        Log.d(TAG, mTab + ": onLoaderReset() called with: " + "loader = [" + loader + "]");
        mViewModel.setData(null);
        updateUi();
    }

    @Override
    public void onFilterSubmitted(String filter) {
        filter(filter);
    }


    private void updateUi() {
        Log.d(TAG, mTab + ": updateUi() called with: " + "");
        if (mViewModel.isDataAvailable() || !TextUtils.isEmpty(mHeaderFragment.getHeader())) {
            mHeaderFragment.show();
        } else {
            mHeaderFragment.hide();
        }

        String query = mViewModel.getUsedQueryWord();
        setEmptyText(query);
        mHeaderFragment.setHeader(query);

        getActivity().supportInvalidateOptionsMenu();
    }

    private void setEmptyText(String query) {
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
            mBinding.empty.setText(ResultListFactory.getEmptyListText(getContext(), mTab, query));
        }
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Settings.PREF_LAYOUT.equals(key)) {
                Bundle args = new Bundle(2);
                args.putString(EXTRA_QUERY, mHeaderFragment.getHeader());
                args.putString(EXTRA_FILTER, mHeaderFragment.getFilter());
                getLoaderManager().restartLoader(mTab.ordinal(), args, ResultListFragment.this);
            }
        }
    };


}
