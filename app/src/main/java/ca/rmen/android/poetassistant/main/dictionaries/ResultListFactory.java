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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.View;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.PatternLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntry;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RTListAdapter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.RhymerLoader;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusListExporter;
import ca.rmen.android.poetassistant.main.dictionaries.rt.ThesaurusLoader;


public class ResultListFactory {
    private static final String TAG = Constants.TAG + ResultListFactory.class.getSimpleName();

    private ResultListFactory() {
        // prevent instantiation
    }

    public static ResultListFragment createListFragment(Tab tab, @Nullable String initialQuery) {
        Log.d(TAG, "createListFragment() called with: " + "tab= [" + tab + "], initialQuery = [" + initialQuery + "]");
        ResultListFragment<?> fragment;
        switch (tab) {
            case PATTERN:
            case RHYMER:
            case THESAURUS:
                fragment = new ResultListFragment<RTEntry>();
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
            case RHYMER:
            case THESAURUS:
                return new RTListAdapter(activity);
            case DICTIONARY:
            default:
                return new DictionaryListAdapter();
        }
    }

    static AsyncTaskLoader<? extends ResultListData<?>> createLoader(Tab tab, Activity activity, String query, String filter) {
        switch (tab) {
            case PATTERN:
                return new PatternLoader(activity, query);
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
            case RHYMER:
                return new RhymerListExporter(context);
            case THESAURUS:
                return new ThesaurusListExporter(context);
            case DICTIONARY:
            default:
                return new DictionaryListExporter(context);
        }
    }

    static InputDialogFragment createFilterDialog(Context context, Tab tab, @SuppressWarnings("SameParameterValue") int actionId, String text) {
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
        return InputDialogFragment.create(actionId, context.getString(R.string.filter_title), dialogMessage, text);
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
    static void updateListHeaderButtonsVisbility(FragmentResultListBinding fragmentResultListBinding, Tab tab, int textToSpeechStatus) {
        switch (tab) {
            case PATTERN:
                fragmentResultListBinding.btnHelp.setVisibility(View.VISIBLE);
                fragmentResultListBinding.btnPlay.setVisibility(View.GONE);
                fragmentResultListBinding.btnWebSearch.setVisibility(View.GONE);
                fragmentResultListBinding.btnStarQuery.setVisibility(View.GONE);
                break;
            case RHYMER:
            case THESAURUS:
                fragmentResultListBinding.btnFilter.setVisibility(View.VISIBLE);
            case DICTIONARY:
                int playButtonVisibility = textToSpeechStatus == TextToSpeech.SUCCESS ? View.VISIBLE : View.GONE;
                fragmentResultListBinding.btnPlay.setVisibility(playButtonVisibility);
            default:
        }
    }
}
