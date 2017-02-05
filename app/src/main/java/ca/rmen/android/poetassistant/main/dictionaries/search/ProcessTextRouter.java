/*
 * Copyright (c) 2017 Carmen Alvarez
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
package ca.rmen.android.poetassistant.main.dictionaries.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.main.Tab;

final class ProcessTextRouter {
    private static final String TAG = Constants.TAG + ProcessTextRouter.class.getSimpleName();

    private ProcessTextRouter() {
        // prevent instantiation
    }

    static void handleIntent(Context context, Intent intent, Tab tab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
                if (!TextUtils.isEmpty(text)) {
                    String query = text.toString().trim().toLowerCase(Locale.US);
                    Uri uri = Uri.withAppendedPath(
                            Uri.parse("poetassistant://" + tab.name().toLowerCase(Locale.US)),
                            query);
                    Intent mainActivityIntent = new Intent(Intent.ACTION_VIEW);
                    mainActivityIntent.setData(uri);
                    Log.v(TAG, "Launching intent " + mainActivityIntent);
                    context.startActivity(mainActivityIntent);
                }
            }
        }

    }
}
