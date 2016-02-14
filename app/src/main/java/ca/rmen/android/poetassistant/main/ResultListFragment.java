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

package ca.rmen.android.poetassistant.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;


public class ResultListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ResultListEntry>> {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    private static final String EXTRA_TAB = "tab";
    private static final String EXTRA_QUERY = "query";
    private ResultListAdapter mAdapter;
    private Tab mTab;
    private TextView mListHeaderView;
    private View mDivider;

    public static ResultListFragment newInstance(Tab tab, @Nullable String initialQuery) {
        Log.d(TAG, "newInstance() called with: " + "tab= [" + tab + "], initialQuery = [" + initialQuery + "]");
        ResultListFragment fragment = new ResultListFragment();
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(EXTRA_TAB, tab);
        if (initialQuery != null) {
            bundle.putString(EXTRA_QUERY, initialQuery);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        Log.v(TAG, "onCreateView: tab = " + mTab);
        View view = inflater.inflate(R.layout.fragment_result_list, container, false);
        mListHeaderView = (TextView) view.findViewById(R.id.tv_list_header);
        mDivider = view.findViewById(R.id.divider);
        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            mListHeaderView.setText(query);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mAdapter = new ResultListAdapter(getActivity(), (OnWordClickedListener) getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        Bundle arguments = getArguments();
        if (arguments != null && savedInstanceState == null) {
            String initialQuery = arguments.getString(EXTRA_QUERY);
            if (!TextUtils.isEmpty(initialQuery)) query(initialQuery);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        outState.putString(EXTRA_QUERY, (String) mListHeaderView.getText());
    }

    public void query(String query) {
        Log.d(TAG, "query() called with: " + "query = [" + query + "]");
        mListHeaderView.setText(query);
        Bundle args = new Bundle(1);
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public Loader<List<ResultListEntry>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        String query = "";
        if (args != null) query = args.getString("query");
        ResultListLoader loader = ResultListLoader.getLoader(mTab, getActivity(), query);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<ResultListEntry>> loader, List<ResultListEntry> data) {
        Log.d(TAG, "onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mAdapter.setData(data);
        int headerVisible = mAdapter.getCount() > 0 ? View.VISIBLE : View.GONE;
        mListHeaderView.setVisibility(headerVisible);
        mDivider.setVisibility(headerVisible);
    }

    @Override
    public void onLoaderReset(Loader<List<ResultListEntry>> loader) {
        Log.d(TAG, "onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.setData(null);
    }


}
