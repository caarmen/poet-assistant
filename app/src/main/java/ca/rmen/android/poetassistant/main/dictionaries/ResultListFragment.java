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

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;


public class ResultListFragment<T> extends Fragment
        implements
        LoaderManager.LoaderCallbacks<ResultListData<T>> {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private FragmentResultListBinding mBinding;
    private ResultListViewModel<T> mViewModel;
    private ResultListHeaderViewModel mHeaderViewModel;

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
        //noinspection unchecked
        mViewModel = (ResultListViewModel<T>) ResultListFactory.createViewModel(mTab, this);
        mBinding.setViewModel(mViewModel);
        mViewModel.layout.addOnPropertyChangedCallback(mLayoutSettingChanged);
        mViewModel.showHeader.addOnPropertyChangedCallback(mShowHeaderChanged);
        mHeaderViewModel = ViewModelProviders.of(this).get(ResultListHeaderViewModel.class);
        mHeaderViewModel.filter.addOnPropertyChangedCallback(mFilterChanged);
        Fragment headerFragment = getChildFragmentManager().findFragmentById(R.id.result_list_header);
        if (headerFragment == null) {
            headerFragment = ResultListHeaderFragment.newInstance(mTab);
            getChildFragmentManager().beginTransaction().replace(R.id.result_list_header, headerFragment).commit();
        }
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
        mViewModel.layout.removeOnPropertyChangedCallback(mLayoutSettingChanged);
        mViewModel.layout.removeOnPropertyChangedCallback(mShowHeaderChanged);
        mHeaderViewModel.filter.removeOnPropertyChangedCallback(mFilterChanged);
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            mViewModel.share(mTab, mHeaderViewModel.query.get(), mHeaderViewModel.filter.get());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(mViewModel.isDataAvailable.get());
    }

    public void query(String query) {
        Log.d(TAG, mTab + ": query() called with: " + "query = [" + query + "]");
        mHeaderViewModel.query.set(query);
        mHeaderViewModel.filter.set(null);
        Bundle args = new Bundle(1);
        args.putString(EXTRA_QUERY, query);
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
            mHeaderViewModel.query.set(query);
            mViewModel.setData(new ResultListData<>(query, null));
        } else {
            mViewModel.setData(null);
        }

        getActivity().invalidateOptionsMenu();

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

    private void updateUi() {
        Log.d(TAG, mTab + ": updateUi() called with: " + "");
        String query = mViewModel.getUsedQueryWord();
        mHeaderViewModel.query.set(query);
        getActivity().invalidateOptionsMenu();
    }

    private final BindingCallbackAdapter mLayoutSettingChanged = new BindingCallbackAdapter(() -> {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_QUERY, mHeaderViewModel.query.get());
        args.putString(EXTRA_FILTER, mHeaderViewModel.filter.get());
        getLoaderManager().restartLoader(mTab.ordinal(), args, ResultListFragment.this);
    });

    private final BindingCallbackAdapter mShowHeaderChanged =
            new BindingCallbackAdapter(() -> mHeaderViewModel.showHeader.set(mViewModel.showHeader.get()));

    private final BindingCallbackAdapter mFilterChanged =
            new BindingCallbackAdapter(() -> {
                Bundle args = new Bundle(2);
                args.putString(EXTRA_QUERY, mHeaderViewModel.query.get());
                args.putString(EXTRA_FILTER, mHeaderViewModel.filter.get());
                getLoaderManager().restartLoader(mTab.ordinal(), args, this);
            });
}
