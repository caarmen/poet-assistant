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

package ca.rmen.android.poetassistant.main.dictionaries.dictionary;

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

import java.util.Arrays;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.SearchableListFragment;


public class DictionaryListFragment extends ListFragment
        implements SearchableListFragment,
        LoaderManager.LoaderCallbacks<Dictionary.DictionaryEntry[]> {
    private static final String TAG = Constants.TAG + DictionaryListFragment.class.getSimpleName();
    private static final String EXTRA_QUERY = "query";
    private DictionaryListAdapter mAdapter;
    private TextView mListHeaderView;
    private View mDivider;

    public static DictionaryListFragment newInstance(@Nullable String initialQuery) {
        Log.d(TAG, "newInstance() called with: initialQuery = [" + initialQuery + "]");
        DictionaryListFragment fragment = new DictionaryListFragment();
        Bundle bundle = new Bundle(1);
        if (initialQuery != null) {
            bundle.putString(EXTRA_QUERY, initialQuery);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mAdapter = new DictionaryListAdapter(getActivity());
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

    @Override
    public void query(String query) {
        Log.d(TAG, "query() called with: " + "query = [" + query + "]");
        mListHeaderView.setText(query);
        Bundle args = new Bundle(1);
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public Loader<Dictionary.DictionaryEntry[]> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        String query = "";
        if (args != null) query = args.getString("query");
        DictionaryLoader loader = new DictionaryLoader(getActivity(), query);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Dictionary.DictionaryEntry[]> loader, Dictionary.DictionaryEntry[] data) {
        Log.d(TAG, "onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + Arrays.toString(data) + "]");
        mAdapter.setData(data);
        int headerVisible = mAdapter.getCount() > 0 ? View.VISIBLE : View.GONE;
        mListHeaderView.setVisibility(headerVisible);
        mDivider.setVisibility(headerVisible);
    }

    @Override
    public void onLoaderReset(Loader<Dictionary.DictionaryEntry[]> loader) {
        Log.d(TAG, "onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.setData(null);
    }


}
