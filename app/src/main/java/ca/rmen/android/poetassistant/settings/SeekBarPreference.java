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

package ca.rmen.android.poetassistant.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.Locale;

import ca.rmen.android.poetassistant.R;

public class SeekBarPreference extends DialogPreference {

    private float mDefaultValue;
    private final float mMin;
    private final float mMax;
    private final String mMinLabel;
    private final String mMaxLabel;
    private final String mProgressLabel;
    private final String mSummaryFormat;

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context) {
        this(context, null);
    }

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);

        mMinLabel = a.getString(R.styleable.SeekBarPreference_minLabel);
        mMaxLabel = a.getString(R.styleable.SeekBarPreference_maxLabel);
        mProgressLabel = a.getString(R.styleable.SeekBarPreference_progressLabel);
        mMin = a.getFloat(R.styleable.SeekBarPreference_min, 0f);
        mMax = a.getFloat(R.styleable.SeekBarPreference_max, 100f);
        a.recycle();
        a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        mSummaryFormat = TypedArrayUtils.getString(a, R.styleable.Preference_summary, R.styleable.Preference_android_summary);
        a.recycle();
        setPersistent(true);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue = a.getFloat(index, 0f);
        return mDefaultValue;
    }

    @Override
    public CharSequence getSummary() {
        if (TextUtils.isEmpty(mSummaryFormat)) return "";
        return String.format(Locale.getDefault(), mSummaryFormat, getValue());
    }

    float getMin() {
        return mMin;
    }

    float getMax() {
        return mMax;
    }

    String getMinLabel() {
        return mMinLabel;
    }

    String getMaxLabel() {
        return mMaxLabel;
    }

    String getProgressLabel() {
        return mProgressLabel;
    }

    float getValue() {
        return getPersistedFloat(mDefaultValue);
    }

    void save(float value) {
        persistFloat(value);
        notifyChanged();
    }
}
