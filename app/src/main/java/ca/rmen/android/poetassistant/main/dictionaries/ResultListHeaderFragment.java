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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding;
import ca.rmen.android.poetassistant.main.Tab;

public class ResultListHeaderFragment extends Fragment
    implements FilterDialogFragment.FilterDialogListener,
        ConfirmDialogFragment.ConfirmDialogListener {

    private static final String TAG = Constants.TAG + ResultListHeaderFragment.class.getSimpleName();
    private static final int ACTION_CLEAR_FAVORITES = 1;
    private static final String DIALOG_TAG = "dialog";
    private static final String EXTRA_TAB = "tab";

    private Tab mTab;
    private ResultListHeaderBinding mBinding;
    private ResultListHeaderViewModel mViewModel;

    public static ResultListHeaderFragment newInstance(Tab tab) {
        Bundle arguments = new Bundle(1);
        arguments.putSerializable(EXTRA_TAB, tab);
        ResultListHeaderFragment fragment = new ResultListHeaderFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView called with savedInstanceState " + savedInstanceState);
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.result_list_header, container, false);
        mBinding.tvFilterLabel.setText(ResultListFactory.getFilterLabel(getContext(), mTab));
        mBinding.setButtonListener(new ButtonListener());
        mViewModel = ViewModelProviders.of(getParentFragment()).get(ResultListHeaderViewModel.class);
        mBinding.setViewModel(mViewModel);
        mViewModel.snackbarText.observe(this, mSnackbarTextChanged);
        mViewModel.isFavoriteLiveData.observe(this, mFavoriteObserver);
        mViewModel.ttsStateLiveData.observe(this, mTtsObserver);

        return mBinding.getRoot();
    }

    @Override
    public void onFilterSubmitted(String input) {
        String normalizedInput = input == null ? null : input.toLowerCase(Locale.getDefault()).trim();
        mViewModel.filter.set(normalizedInput);
    }

    @Override
    public void onOk(int actionId) {
        if (actionId == ACTION_CLEAR_FAVORITES) {
            mViewModel.clearFavorites();
        }
    }

    private final Observer<String> mSnackbarTextChanged = text -> {
        if (!TextUtils.isEmpty(text)) {
            Snackbar.make(mBinding.getRoot(), text, Snackbar.LENGTH_SHORT).show();
        }
    };

    private final Observer<Boolean> mFavoriteObserver = isFavorite -> mBinding.btnStarQuery.setChecked(isFavorite == Boolean.TRUE);
    private final Observer<Tts.TtsState> mTtsObserver = ttsState -> {
        Log.d(TAG, mTab + ": ttsState = " + ttsState);
        if (mTab != null && ttsState != null) ResultListFactory.updateListHeaderButtonsVisibility(mBinding, mTab, ttsState.currentStatus);
    };

    public class ButtonListener {

        public void onDeleteFavoritesButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            ConfirmDialogFragment.show(
                    ACTION_CLEAR_FAVORITES,
                    getString(R.string.action_clear_favorites),
                    getString(R.string.action_clear),
                    getChildFragmentManager(),
                    DIALOG_TAG);
        }

        public void onFilterButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            FilterDialogFragment fragment =
                    ResultListFactory.createFilterDialog(
                            getContext(),
                            mTab,
                            mViewModel.filter.get());
            getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
        }

        public void onHelpButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            getChildFragmentManager().beginTransaction().add(new HelpDialogFragment(), DIALOG_TAG).commit();
        }
    }
}
