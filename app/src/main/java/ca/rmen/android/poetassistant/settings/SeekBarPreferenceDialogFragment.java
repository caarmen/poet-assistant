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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import java.util.Locale;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.PreferenceSeekBarBinding;

public class SeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String EXTRA_SELECTED_VALUE = "selected_value";

    private static final int SEEK_BAR_MAX = 100;

    private PreferenceSeekBarBinding mBinding;
    private float mValue;

    public static SeekBarPreferenceDialogFragment newInstance(String key) {
        Bundle args = new Bundle(1);
        args.putString("key", key);
        SeekBarPreferenceDialogFragment fragment = new SeekBarPreferenceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mValue = savedInstanceState.getInt(EXTRA_SELECTED_VALUE);
        } else {
            SeekBarPreference preference = (SeekBarPreference) getPreference();
            mValue = preference.getValue();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(EXTRA_SELECTED_VALUE, mValue);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        SeekBarPreference seekBarPreference = getSeekBarPreference();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.preference_seek_bar, null, false);
        mBinding.seekBar.setMax(SEEK_BAR_MAX);
        mBinding.seekBarLeftLabel.setText(seekBarPreference.getMinLabel());
        mBinding.seekBarRightLabel.setText(seekBarPreference.getMaxLabel());
        updateSeekBarProgress(mValue);
        mBinding.seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        return mBinding.getRoot();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mBinding = DataBindingUtil.findBinding(view);
        updateSeekBarProgress(mValue);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SeekBarPreference seekBarPreference = getSeekBarPreference();
            float value = getSeekBarProgress();
            if (seekBarPreference.callChangeListener(value)) {
                seekBarPreference.save(value);
            }
        }
    }

    private String getDisplayProgress(float progress) {
        return String.format(Locale.getDefault(), getSeekBarPreference().getProgressLabel(), progress);
    }

    private void updateSeekBarProgress(float value) {
        SeekBarPreference seekBarPreference = getSeekBarPreference();
        PreferenceSeekBarBinding binding = DataBindingUtil.findBinding(mBinding.seekBar);
        mBinding.seekBar.setProgress((int) (((value - seekBarPreference.getMin()) / seekBarPreference.getMax()) * SEEK_BAR_MAX));
        binding.seekBarProgress.setText(getDisplayProgress(value));
    }

    private float getSeekBarProgress() {
        SeekBarPreference seekBarPreference = getSeekBarPreference();
        return ((float) mBinding.seekBar.getProgress() / SEEK_BAR_MAX) * (seekBarPreference.getMax() - seekBarPreference.getMin()) + seekBarPreference.getMin();
    }

    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mBinding.seekBarProgress.setText(getDisplayProgress(getSeekBarProgress()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private SeekBarPreference getSeekBarPreference() {
        return (SeekBarPreference) getPreference();
    }
}
