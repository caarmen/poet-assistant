/*
 * Copyright (c) 2016 - present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.common.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import ca.rmen.android.poetassistant.R
import javax.inject.Inject

/**
 * Use case for opening an external browser for web search.
 * Constructs a URL and launches an Intent for the given word.
 * Called by Composable (not injected) as it requires Context.
 */
class OpenExternalSearchUseCase @Inject constructor() {

    /**
     * Opens an external browser to search for the given word.
     *
     * @param word The word to search for.
     * @param context The Android context used to start the activity.
     */
    operator fun invoke(word: String, context: Context) {
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(android.app.SearchManager.QUERY, word)
        }
        // No apps can handle ACTION_WEB_SEARCH. We'll try a more generic intent instead
        val intentActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(searchIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(searchIntent, 0)
        }
        val finalIntent = if (intentActivities.isEmpty()) {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, word)
            }
        } else {
            searchIntent
        }
        val chooserIntent = Intent.createChooser(finalIntent, context.getString(R.string.action_web_search, word)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}
