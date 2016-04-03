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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.Tab;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;

final class Share {
    private static final String TAG = Constants.TAG + Share.class.getSimpleName();

    private Share() {
        // prevent instantiation
    }

    /**
     * Allows the user to share the contents of the given {@link ResultListFragment} tab as text.
     *
     * @param word    the word the user searched for
     * @param filter  an optional filter the user specified to narrow the search results.
     * @param entries the rhymer, thesaurus, or dictionary entries for the given word
     * @param <T>     the type of data displayed in the list fragment. Must be List of {@link ca.rmen.android.poetassistant.main.dictionaries.rt.RTEntry} or List of {@link DictionaryEntry.DictionaryEntryDetails}
     */
    public static <T> void share(Context context, @NonNull Tab tab, @NonNull String word, @Nullable String filter, @NonNull T entries) {
        // noinspection unchecked
        ResultListExporter<T> exporter = (ResultListExporter<T>) ResultListFactory.createExporter(context, tab);
        String text = exporter.export(word, filter, entries);
        Log.v(TAG, "Will share " + text);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

}
