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
import android.os.AsyncTask;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.Tab;

public final class Share {
    private static final String TAG = Constants.TAG + Share.class.getSimpleName();

    private Share() {
        // prevent instantiation
    }

    public static final void share(final Context context, Tab tab, final String word, final String filter) {

        final ResultListSharer sharer = ResultListFactory.createSharer(
                context,
                tab);

        new AsyncTask<Void, Void, ResultListSharer.ShareInfo>() {
            @Override
            protected ResultListSharer.ShareInfo doInBackground(Void... params) {
                return sharer.getHtmlShareInfo(word, filter);
            }

            @Override
            protected void onPostExecute(ResultListSharer.ShareInfo shareInfo) {
                Log.v(TAG, "Will share " + shareInfo);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, shareInfo.title);
                intent.putExtra(Intent.EXTRA_TEXT, shareInfo.content);
                intent.setType("text/html");
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
            }
        }.execute();
    }


}
