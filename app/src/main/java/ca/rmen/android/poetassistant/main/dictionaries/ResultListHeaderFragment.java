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

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.Favorites;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
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

    @Inject Tts mTts;
    @Inject Favorites mFavorites;


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
        DaggerHelper.getMainScreenComponent(getContext()).inject(this);
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.result_list_header, container, false);
        mBinding.tvFilterLabel.setText(ResultListFactory.getFilterLabel(getContext(), mTab));
        mBinding.setButtonListener(new ButtonListener());

        EventBus.getDefault().register(this);
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, mTab + " onViewStateRestored, bundle = " + savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
        ResultListHeaderViewModel resultListHeaderViewModel = ViewModelProviders.of(getParentFragment()).get(ResultListHeaderViewModel.class);
        mBinding.setViewModel(resultListHeaderViewModel);
        updateUi();
    }

    @Override
    public void onFilterSubmitted(String input) {
        String normalizedInput = input == null ? null : input.toLowerCase(Locale.getDefault()).trim();
        mBinding.getViewModel().filter.set(normalizedInput);
    }

    @Override
    public void onOk(int actionId) {
        if (actionId == ACTION_CLEAR_FAVORITES) {
            mFavorites.clear();
            Snackbar.make(mBinding.getRoot(), R.string.favorites_cleared, Snackbar.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onTtsInitialized(Tts.OnTtsInitialized event) {
        Log.d(TAG, mTab + ": onTtsInitialized() called with: " + "event = [" + event + "]");
        updateUi();
    }

    private void updateUi() {
        Log.v(TAG, mTab + " updateUi");
        if (mTab != null) ResultListFactory.updateListHeaderButtonsVisibility(mBinding, mTab, mTts.getStatus());
    }

    public class ButtonListener {
        public void onPlayButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            mTts.speak(mBinding.getViewModel().query.get());
        }

        public void onDeleteFavoritesButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            ConfirmDialogFragment.show(
                    ACTION_CLEAR_FAVORITES,
                    getString(R.string.action_clear_favorites),
                    getString(R.string.action_clear),
                    getChildFragmentManager(),
                    DIALOG_TAG);
        }

        public void onWebSearchButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            String word = mBinding.getViewModel().query.get();
            searchIntent.putExtra(SearchManager.QUERY, word);
            // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
            if (getContext().getPackageManager().queryIntentActivities(searchIntent, 0).isEmpty()) {
                searchIntent = new Intent(Intent.ACTION_SEND);
                searchIntent.setType("text/plain");
                searchIntent.putExtra(Intent.EXTRA_TEXT, word);
            }
            getContext().startActivity(Intent.createChooser(searchIntent, getString(R.string.action_web_search, word)));
        }

        public void onFilterButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            FilterDialogFragment fragment =
                    ResultListFactory.createFilterDialog(
                            getContext(),
                            mTab,
                            mBinding.getViewModel().filter.get());
            getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
        }

        public void onHelpButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            getChildFragmentManager().beginTransaction().add(new HelpDialogFragment(), DIALOG_TAG).commit();
        }

        public void onFilterClearButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            mBinding.getViewModel().filter.set(null);
        }
    }
}
