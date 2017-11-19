/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.dictionaries

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import ca.rmen.android.poetassistant.R

object WebSearch {
    /**
     * Allows the user to lookup the given text in a web search.
     */
    fun search(context : Context, text: String) {
        var searchIntent = Intent(Intent.ACTION_WEB_SEARCH)
        searchIntent.putExtra(SearchManager.QUERY, text)
        // No apps can handle ACTION_WEB_SEARCH.  We'll try a more generic intent instead
        if (context.packageManager.queryIntentActivities(searchIntent, 0).isEmpty()) {
            searchIntent = Intent(Intent.ACTION_SEND)
            searchIntent.type = "text/plain"
            searchIntent.putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooserIntent = Intent.createChooser(searchIntent, context.getString(R.string.action_web_search, text))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}