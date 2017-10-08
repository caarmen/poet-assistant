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

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.FavoritesLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntryViewModel;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLoader;
import ca.rmen.android.poetassistant.wotd.WotdAdapter;
import ca.rmen.android.poetassistant.wotd.WotdEntryViewModel;
import ca.rmen.android.poetassistant.wotd.WotdListExporter;
import ca.rmen.android.poetassistant.wotd.WotdLoader;


public final class ResultListFactory {
    private static final String TAG = Constants.TAG + ResultListFactory.class.getSimpleName();

    private ResultListFactory() {
        // prevent instantiation
    }

    public static ResultListFragment createListFragment(Tab tab, @Nullable String initialQuery) {
        Log.d(TAG, "createListFragment() called with: " + "tab= [" + tab + "], initialQuery = [" + initialQuery + "]");
        ResultListFragment<?> fragment;
        switch (tab) {
            case PATTERN:
            case FAVORITES:
            case RHYMER:
            case THESAURUS:
                fragment = new ResultListFragment<RTEntryViewModel>();
                break;
            case WOTD:
                fragment = new ResultListFragment<WotdEntryViewModel>();
                break;
            case DICTIONARY:
            default:
                fragment = new ResultListFragment<DictionaryEntry.DictionaryEntryDetails>();
        }
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(ResultListFragment.EXTRA_TAB, tab);
        fragment.setArguments(bundle);
        if (initialQuery != null) {
            bundle.putString(ResultListFragment.EXTRA_QUERY, initialQuery);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    static ResultListAdapter<?> createAdapter(Activity activity, Tab tab) {
        switch (tab) {
            case PATTERN:
            case FAVORITES:
            case RHYMER:
            case THESAURUS:
                return new RTListAdapter(activity);
            case WOTD:
                return new WotdAdapter(activity);
            case DICTIONARY:
            default:
                return new DictionaryListAdapter((OnWordClickListener) activity);
        }
    }

    static ResultListViewModel<?> createViewModel(Tab tab, Fragment fragment) {
        ViewModelProvider.Factory factory = createViewModelFactory(tab, (Application) fragment.getContext().getApplicationContext());
        return ViewModelProviders.of(fragment,factory).get(ResultListViewModel.class);
    }

    private static ViewModelProvider.Factory createViewModelFactory(Tab tab, Application application) {
        return new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> aClass) {
                switch(tab) {
                    case PATTERN:
                    case FAVORITES:
                    case RHYMER:
                    case THESAURUS:
                        //noinspection unchecked
                        return (T) new ResultListViewModel<RTEntryViewModel>(application, tab);
                    case WOTD:
                        //noinspection unchecked
                        return (T) new ResultListViewModel<WotdEntryViewModel>(application, tab);
                    case DICTIONARY:
                    default:
                        //noinspection unchecked
                        return (T) new ResultListViewModel<DictionaryEntry.DictionaryEntryDetails>(application, tab);
                }
            }
        };
    }

    static ResultListLoader<? extends ResultListData<?>> createLoader(Tab tab, Activity activity, String query, String filter) {
        switch (tab) {
            case PATTERN:
                return new PatternLoader(activity, query);
            case FAVORITES:
                return new FavoritesLoader(activity);
            case WOTD:
                return new WotdLoader(activity);
            case RHYMER:
                return new RhymerLoader(activity, query, filter);
            case THESAURUS:
                return new ThesaurusLoader(activity, query, filter);
            case DICTIONARY:
            default:
                return new DictionaryLoader(activity, query);

        }
    }

    static ResultListExporter<?> createExporter(Context context, Tab tab) {
        switch (tab) {
            case PATTERN:
                return new PatternListExporter(context);
            case FAVORITES:
                return new FavoritesListExporter(context);
            case WOTD:
                return new WotdListExporter(context);
            case RHYMER:
                return new RhymerListExporter(context);
            case THESAURUS:
                return new ThesaurusListExporter(context);
            case DICTIONARY:
            default:
                return new DictionaryListExporter(context);
        }
    }

    static FilterDialogFragment createFilterDialog(Context context, Tab tab, String text) {
        String dialogMessage;
        switch (tab) {
            case RHYMER:
                dialogMessage = context.getString(R.string.filter_rhymer_message);
                break;
            case THESAURUS:
            default:
                dialogMessage = context.getString(R.string.filter_thesaurus_message);
                break;
        }
        return FilterDialogFragment.newInstance(dialogMessage, text);
    }

    static void inject(Tab tab, ResultListFragment<?> fragment) {
        switch (tab) {
            case RHYMER:
            case THESAURUS:
            case PATTERN:
            case FAVORITES:
                //noinspection unchecked
                DaggerHelper.getMainScreenComponent(fragment.getContext())
                        .inject((ResultListFragment<RTEntryViewModel>) fragment);
                break;
            case WOTD:
                //noinspection unchecked
                DaggerHelper.getMainScreenComponent(fragment.getContext())
                        .injectWotd((ResultListFragment<WotdEntryViewModel>) fragment);
            case DICTIONARY:
                //noinspection unchecked
                DaggerHelper.getMainScreenComponent(fragment.getContext())
                        .injectDict((ResultListFragment<DictionaryEntry>) fragment);
                break;
            default:
        }
    }
    static String getFilterLabel(Context context, Tab tab) {
        switch (tab) {
            case RHYMER:
                return context.getString(R.string.filter_rhymer_label);
            case THESAURUS:
            default:
                return context.getString(R.string.filter_thesaurus_label);
        }
    }

    static String getEmptyListText(Context context, Tab tab, String query) {
        switch (tab) {
            case FAVORITES:
                return context.getString(R.string.empty_favorites_list);
            case PATTERN:
                return context.getString(R.string.empty_pattern_list_with_query, query);
            case RHYMER:
                return context.getString(R.string.empty_rhymer_list_with_query, query);
            case THESAURUS:
                return context.getString(R.string.empty_thesaurus_list_with_query, query);
            case DICTIONARY:
            default:
                return context.getString(R.string.empty_dictionary_list_with_query, query);
        }
    }

    /**
     * Set the various buttons which appear in the result list header (ex: tts play,
     * web search, filter, help) to visible or gone, depending on the tab.
     */
    static void updateListHeaderButtonsVisibility(ResultListHeaderBinding binding, Tab tab, int textToSpeechStatus) {
        switch (tab) {
            case FAVORITES:
                binding.btnPlay.setVisibility(View.GONE);
                binding.btnWebSearch.setVisibility(View.GONE);
                binding.btnStarQuery.setVisibility(View.GONE);
                binding.btnDelete.setVisibility(View.VISIBLE);
                break;
            case WOTD:
                binding.btnPlay.setVisibility(View.GONE);
                binding.btnWebSearch.setVisibility(View.GONE);
                binding.btnStarQuery.setVisibility(View.GONE);
                binding.btnDelete.setVisibility(View.GONE);
                break;
            case PATTERN:
                binding.btnHelp.setVisibility(View.VISIBLE);
                binding.btnPlay.setVisibility(View.GONE);
                binding.btnWebSearch.setVisibility(View.GONE);
                binding.btnStarQuery.setVisibility(View.GONE);
                break;
            case RHYMER:
            case THESAURUS:
                binding.btnFilter.setVisibility(View.VISIBLE);
            case DICTIONARY:
                int playButtonVisibility = textToSpeechStatus == TextToSpeech.SUCCESS ? View.VISIBLE : View.GONE;
                binding.btnPlay.setVisibility(playButtonVisibility);
            default:
        }
    }

    public static String getTabName(Context context, Tab tab) {
        if (tab == Tab.PATTERN)
            return context.getString(R.string.tab_pattern).toUpperCase(Locale.getDefault());
        else if (tab == Tab.FAVORITES)
            return context.getString(R.string.tab_favorites).toUpperCase(Locale.getDefault());
        else if (tab == Tab.WOTD)
            return context.getString(R.string.tab_wotd).toUpperCase(Locale.getDefault());
        else if (tab == Tab.RHYMER)
            return context.getString(R.string.tab_rhymer).toUpperCase(Locale.getDefault());
        else if (tab == Tab.THESAURUS)
            return context.getString(R.string.tab_thesaurus).toUpperCase(Locale.getDefault());
        else if (tab == Tab.DICTIONARY)
            return context.getString(R.string.tab_dictionary).toUpperCase(Locale.getDefault());
        else
            return context.getString(R.string.tab_reader).toUpperCase(Locale.getDefault());
    }
}
