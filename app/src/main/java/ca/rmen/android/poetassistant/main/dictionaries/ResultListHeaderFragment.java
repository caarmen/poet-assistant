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
import android.widget.CheckBox;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding;
import ca.rmen.android.poetassistant.main.HelpDialogFragment;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFavoriteClickListener;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFilterListener;

public class ResultListHeaderFragment extends Fragment
    implements InputDialogFragment.InputDialogListener,
        ConfirmDialogFragment.ConfirmDialogListener {

    private static final String TAG = Constants.TAG + ResultListHeaderFragment.class.getSimpleName();
    private static final int ACTION_FILTER = 0;
    private static final int ACTION_CLEAR_FAVORITES = 1;
    private static final String DIALOG_TAG = "dialog";
    private static final String EXTRA_TAB = "tab";

    private Tab mTab;
    private ResultListHeaderBinding mBinding;

    @Inject
    Tts mTts;

    public void setTab(Tab tab) {
        mTab = tab;
        mBinding.tvFilterLabel.setText(ResultListFactory.getFilterLabel(getContext(), mTab));
        updateUi();
    }

    public void show() {
        mBinding.getRoot().setVisibility(View.VISIBLE);
    }

    public void hide() {
        mBinding.getRoot().setVisibility(View.GONE);
    }

    public void setHeader(String header) {
        mBinding.tvListHeader.setText(header);
    }

    public void setFilter(String filter) {
        mBinding.tvFilter.setText(filter);
        mBinding.filter.setVisibility(TextUtils.isEmpty(filter) ? View.GONE : View.VISIBLE);
    }

    public String getHeader() {
        return mBinding.tvListHeader.getText().toString();
    }

    public String getFilter() {
        return mBinding.tvFilter.getText().toString();
    }

    public void setFavorite(boolean isFavorite) {
        mBinding.btnStarQuery.setChecked(isFavorite);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView called with savedInstanceState " + savedInstanceState);
        DaggerHelper.getAppComponent(getContext()).inject(this);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.result_list_header, container, false);
        mBinding.setButtonListener(new ButtonListener());
        if (savedInstanceState != null) {
            mTab = (Tab) savedInstanceState.getSerializable(EXTRA_TAB);
        }

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
        Log.v(TAG, "onViewStateRestored, bundle = " + savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
        updateUi();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, mTab + ": onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        outState.putSerializable(EXTRA_TAB, mTab);
    }

    @Override
    public void onInputSubmitted(int actionId, String input) {
        if (actionId == ResultListHeaderFragment.ACTION_FILTER) {
            OnFilterListener listener = (OnFilterListener) getParentFragment();
            String normalizedInput = input == null ? null : input.toLowerCase(Locale.getDefault()).trim();
            setFilter(normalizedInput);
            listener.onFilterSubmitted(normalizedInput);
        }
    }

    @Override
    public void onOk(int actionId) {
        if (actionId == ACTION_CLEAR_FAVORITES) {
            new Favorites(getContext()).clear();
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
        mBinding.filter.setVisibility(TextUtils.isEmpty(mBinding.tvFilter.getText()) ? View.GONE : View.VISIBLE);
    }

    public class ButtonListener {
        public void onPlayButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            mTts.speak(mBinding.tvListHeader.getText().toString());
        }

        public void onFavoriteButtonClicked(View v) {
            ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
            String word = binding.tvListHeader.getText().toString();
            ((OnFavoriteClickListener) getActivity()).onFavoriteToggled(word, ((CheckBox) v).isChecked());
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
            String word = mBinding.tvListHeader.getText().toString();
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
            InputDialogFragment fragment =
                    ResultListFactory.createFilterDialog(
                            getContext(),
                            mTab,
                            ACTION_FILTER,
                            mBinding.tvFilter.getText().toString());
            getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
        }

        public void onHelpButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            getChildFragmentManager().beginTransaction().add(new HelpDialogFragment(), DIALOG_TAG).commit();
        }

        public void onFilterClearButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
            setFilter(null);
            ((OnFilterListener) getParentFragment()).onFilterSubmitted(null);
        }
    }
}
