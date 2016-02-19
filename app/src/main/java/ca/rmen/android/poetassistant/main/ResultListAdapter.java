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

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;


class ResultListAdapter extends BaseAdapter {
    private static final String TAG = Constants.TAG + ResultListAdapter.class.getSimpleName();

    private final Context mContext;
    private final OnWordClickedListener mListener;
    private final List<ResultListEntry> mData = new ArrayList<>();

    public ResultListAdapter(Context context, OnWordClickedListener listener) {
        super();
        mContext = context;
        mListener = listener;
    }

    public void setData(List<ResultListEntry> data) {
        Log.d(TAG, "setData() called with: " + "data = [" + data + "]");
        mData.clear();
        if (data != null) mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        ResultListEntry entry = mData.get(position);
        return entry.type.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResultListEntry entry = mData.get(position);
        if (entry.type == ResultListEntry.Type.HEADING)
            return getHeadingView(entry, convertView);
        else if (entry.type == ResultListEntry.Type.SUBHEADING)
            return getSubHeadingView(entry, convertView);
        else
            return getWordView(entry, convertView);
    }

    private View getHeadingView(ResultListEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_heading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }


    private View getSubHeadingView(ResultListEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_subheading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }

    private View getWordView(ResultListEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_word, null);
        }
        TextView text1 = ViewHolder.get(convertView, android.R.id.text1);
        text1.setText(entry.text);
        ImageView btnRhymer = ViewHolder.get(convertView, R.id.btn_rhymer);
        ImageView btnThesaurus = ViewHolder.get(convertView, R.id.btn_thesaurus);
        ImageView btnDictionary = ViewHolder.get(convertView, R.id.btn_dictionary);
        btnRhymer.setOnClickListener(mOnClickListener);
        btnThesaurus.setOnClickListener(mOnClickListener);
        btnDictionary.setOnClickListener(mOnClickListener);
        return convertView;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentView = (View) v.getParent();
            TextView text1 = ViewHolder.get(parentView, android.R.id.text1);
            String word = (String) text1.getText();

            if (v.getId() == R.id.btn_rhymer) {
                mListener.onWordClicked(word, Tab.RHYMER);
            } else if (v.getId() == R.id.btn_thesaurus) {
                mListener.onWordClicked(word, Tab.THESAURUS);
            } else if (v.getId() == R.id.btn_dictionary) {
                mListener.onWordClicked(word, Tab.DICTIONARY);
            }
        }
    };
}
