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

import android.app.SearchManager;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.main.Tab;


public class ResultListFragment<T> extends ListFragment
        implements
        LoaderManager.LoaderCallbacks<List<T>> {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private Tab mTab;
    private ArrayAdapter<T> mAdapter;
    private TextView mListHeaderTextView;
    private View mHeaderView;
    private Tts mTts;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_result_list, container, false);
        mListHeaderTextView = (TextView) view.findViewById(R.id.tv_list_header);
        mHeaderView = view.findViewById(R.id.list_header);
        view.findViewById(R.id.btn_play).setOnClickListener(mPlayButtonListener);
        view.findViewById(R.id.btn_web_search).setOnClickListener(mWebSearchButtonListener);
        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(EXTRA_QUERY);
            mListHeaderTextView.setText(query);
        }
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        mTts = Tts.getInstance(getActivity());
        //noinspection unchecked
        mAdapter = (ArrayAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        Bundle arguments = getArguments();
        if (savedInstanceState == null) {
            String initialQuery = arguments.getString(EXTRA_QUERY);
            if (!TextUtils.isEmpty(initialQuery)) query(initialQuery);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        outState.putString(EXTRA_QUERY, (String) mListHeaderTextView.getText());
    }

    public void query(String query) {
        Log.d(TAG, "query() called with: " + "query = [" + query + "]");
        mListHeaderTextView.setText(query);
        Bundle args = new Bundle(1);
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader() called with: " + "id = [" + id + "], args = [" + args + "]");
        String query = "";
        if (args != null) query = args.getString("query");
        //noinspection unchecked
        Loader<List<T>> loader = (Loader<List<T>>) ResultListFactory.createLoader(mTab, getActivity(), query);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        Log.d(TAG, "onLoadFinished() called with: " + "loader = [" + loader + "], data = [" + data + "]");
        mAdapter.clear();
        mAdapter.addAll(data);
        int headerVisible = mAdapter.getCount() > 0 ? View.VISIBLE : View.GONE;
        mHeaderView.setVisibility(headerVisible);
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        Log.d(TAG, "onLoaderReset() called with: " + "loader = [" + loader + "]");
        mAdapter.clear();
    }

    private final View.OnClickListener mPlayButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mTts.speak(mListHeaderTextView.getText().toString());
        }
    };

    private final View.OnClickListener mWebSearchButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            String word = mListHeaderTextView.getText().toString();
            searchIntent.putExtra(SearchManager.QUERY, word);
            // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
            if(getActivity().getPackageManager().queryIntentActivities(searchIntent, 0).isEmpty()) {
                searchIntent = new Intent(Intent.ACTION_SEND);
                searchIntent.setType("text/plain");
                searchIntent.putExtra(Intent.EXTRA_TEXT, word);
            }
            startActivity(Intent.createChooser(searchIntent, getString(R.string.action_web_search, word)));
        }
    };


}
