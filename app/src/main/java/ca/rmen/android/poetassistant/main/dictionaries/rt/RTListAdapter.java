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

package ca.rmen.android.poetassistant.main.dictionaries.rt;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.ViewHolder;


public class RTListAdapter extends ArrayAdapter<RTEntry> {
    private static final String TAG = Constants.TAG + RTListAdapter.class.getSimpleName();

    private final Context mContext;
    private final OnWordClickedListener mListener;

    public RTListAdapter(Activity activity) {
        super(activity, 0);
        mContext = activity;
        mListener = (OnWordClickedListener) activity;
    }

    @Override
    public int getItemViewType(int position) {
        RTEntry entry = getItem(position);
        return entry.type.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RTEntry entry = getItem(position);
        if (entry.type == RTEntry.Type.HEADING)
            return getHeadingView(entry, convertView);
        else if (entry.type == RTEntry.Type.SUBHEADING)
            return getSubHeadingView(entry, convertView);
        else
            return getWordView(entry, convertView);
    }

    private View getHeadingView(RTEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_heading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }


    private View getSubHeadingView(RTEntry entry, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_subheading, null);
        }
        TextView text = (TextView) convertView;
        text.setText(entry.text);
        return convertView;
    }

    private View getWordView(RTEntry entry, View convertView) {
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
