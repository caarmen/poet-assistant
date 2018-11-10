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

package ca.rmen.android.poetassistant.main.dictionaries.search

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import ca.rmen.android.poetassistant.BuildConfig
import ca.rmen.android.poetassistant.dagger.DaggerHelper

class SuggestionsProvider : ContentProvider() {
    companion object {
        val CONTENT_URI : Uri =  Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(SuggestionsProvider.AUTHORITY)
                .build()
        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".SuggestionsProvider"
        private const val URI_MATCH_SUGGEST = 1
    }

    private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        mUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, URI_MATCH_SUGGEST)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, sel: String?, selArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return context?.let {
            val filter = if (!TextUtils.equals(uri.lastPathSegment, SearchManager.SUGGEST_URI_PATH_QUERY)) uri.lastPathSegment else null
            SuggestionsCursor(it, filter)
        }
    }

    override fun getType(uri: Uri): String {
        if (mUriMatcher.match(uri) == URI_MATCH_SUGGEST) {
            return SearchManager.SUGGEST_MIME_TYPE
        }
        throw IllegalArgumentException("Unknown Uri")
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        val suggestion = values.getAsString(SearchManager.QUERY)
        context?.let {
            DaggerHelper.getMainScreenComponent(it).getSuggestions().addSuggestion(suggestion)
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        context?.let {
            DaggerHelper.getMainScreenComponent(it).getSuggestions().clear()
        }
        return 0
    }

    override fun update(p0: Uri?, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }


}
