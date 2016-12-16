package ca.rmen.android.poetassistant.main.dictionaries;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CheckBox;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.Tts;
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding;
import ca.rmen.android.poetassistant.main.HelpDialogFragment;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnFavoriteClickListener;

/**
 *
 */
public class ResultListHeader {

    interface HeaderButtonCallback {
        void onFilterCleared();
    }

    static final int ACTION_FILTER = 0;
    static final int ACTION_CLEAR_FAVORITES = 1;
    private static final String DIALOG_TAG = "dialog";
    private final Tab mTab;
    @Inject
    Tts mTts;
    private final Fragment mFragment;

    ResultListHeader(Tab tab, Fragment fragment) {
        mTab = tab;
        mFragment = fragment;
        DaggerHelper.getAppComponent(fragment.getContext()).inject(this);
    }

    public void onPlayButtonClicked(View v) {
        ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
        mTts.speak(binding.tvListHeader.getText().toString());
    }

    public void onFavoriteButtonClicked(View v) {
        ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
        String word = binding.tvListHeader.getText().toString();
        ((OnFavoriteClickListener) mFragment.getActivity()).onFavoriteToggled(word, ((CheckBox)v).isChecked());
    }

    public void onDeleteFavoritesButtonClicked(View v) {
        ConfirmDialogFragment.show(
                ACTION_CLEAR_FAVORITES,
                v.getContext().getString(R.string.action_clear_favorites),
                v.getContext().getString(R.string.action_clear),
                mFragment.getChildFragmentManager(),
                DIALOG_TAG);
    }

    public void onWebSearchButtonClicked(View v) {
        Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
        String word = binding.tvListHeader.getText().toString();
        searchIntent.putExtra(SearchManager.QUERY, word);
        // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
        if (v.getContext().getPackageManager().queryIntentActivities(searchIntent, 0).isEmpty()) {
            searchIntent = new Intent(Intent.ACTION_SEND);
            searchIntent.setType("text/plain");
            searchIntent.putExtra(Intent.EXTRA_TEXT, word);
        }
        v.getContext().startActivity(Intent.createChooser(searchIntent, v.getContext().getString(R.string.action_web_search, word)));
    }

    public void onFilterButtonClicked(View v) {
        ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
        InputDialogFragment fragment =
        ResultListFactory.createFilterDialog(
                v.getContext(),
                mTab,
                ACTION_FILTER,
                binding.tvFilter.getText().toString());
        mFragment.getChildFragmentManager().beginTransaction().add(fragment, DIALOG_TAG).commit();
    }

    public void onHelpButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
        mFragment.getFragmentManager().beginTransaction().add(new HelpDialogFragment(), DIALOG_TAG).commit();
    }

    public void onFilterClearButtonClicked(View v) {
        ResultListHeaderBinding binding = DataBindingUtil.findBinding(v);
        binding.tvFilter.setText(null);
        binding.filter.setVisibility(View.GONE);
        ((HeaderButtonCallback) mFragment).onFilterCleared();
    }
}
