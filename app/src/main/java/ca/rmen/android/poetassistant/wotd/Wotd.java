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

package ca.rmen.android.poetassistant.wotd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.compat.HtmlCompat;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.Share;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary;
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry;
import ca.rmen.android.poetassistant.settings.SettingsPrefs;

/**
 * Word of the day system notification.
 */
public final class Wotd {
    private static final String TAG = Constants.TAG + Wotd.class.getSimpleName();

    static final long NOTIFICATION_FREQUENCY_MS = 24 * 60 * 60 * 1000;
    //private static final long NOTIFICATION_FREQUENCY_MS = 60 * 1000;

    private Wotd() {
        // Prevent instantiation
    }

    public static void setWotdEnabled(Context context, Dictionary dictionary, boolean enabled) {
        Log.v(TAG, "setWotdEnabled: enabled = " + enabled);
        if (enabled) enableWotd(context, dictionary);
        else disableWotd(context);
    }

    /**
     * If we have the wotd setting enabled, but no task has been scheduled
     * to do the wotd, we'll schedule the task.
     */
    static void reschedule(Context context, SettingsPrefs settingsPrefs) {
        Log.d(TAG, "reschedule() called with: " + "context = [" + context + "]");
        if (settingsPrefs.getIsWotdEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WotdJob.reschedule(context);
            } else {
                WotdAlarm.reschedule(context);
            }
        }
    }

    private static void enableWotd(final Context context, final Dictionary dictionary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WotdJob.schedule(context);
        } else {
            WotdAlarm.schedule(context);
        }
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                notifyWotd(context, dictionary);
                return null;
            }
        }.execute();
    }

    private static void disableWotd(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WotdJob.cancel(context);
        } else {
            WotdAlarm.cancel(context);
        }
    }

    static Calendar getTodayUTC() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone("UTC"));
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return now;
    }

    static void notifyWotd(Context context, Dictionary dictionary) {
        Log.v(TAG, "notifyWotd");
        DictionaryEntry entry = dictionary.getRandomEntry(getTodayUTC().getTimeInMillis());
        if (entry == null) return;
        String title = context.getString(R.string.wotd_notification_title, entry.word);
        CharSequence content = buildWotdNotificationContent(context, entry);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle().bigText(content);
        Uri uri = Uri.parse(String.format("poetassistant://%s/%s", Constants.DEEP_LINK_QUERY, entry.word));
        Intent intent = new Intent(context, MainActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .setData(uri)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setContentTitle(title)
                .setSmallIcon(Share.getNotificationIcon())
                .setStyle(bigTextStyle)
                .addAction(
                        Share.getShareIconId(),
                        context.getString(R.string.share),
                        getShareIntent(context, entry))
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(TAG.hashCode(), notification);
    }

    private static CharSequence buildWotdNotificationContent(Context context, DictionaryEntry entry) {
        StringBuilder builder = new StringBuilder(entry.word);
        for (DictionaryEntry.DictionaryEntryDetails details : entry.details) {
            builder.append(context.getString(R.string.wotd_notification_definition, details.partOfSpeech, details.definition));
        }
        String content = builder.toString();
        return HtmlCompat.fromHtml(content);
    }

    private static CharSequence buildWotdShareContent(Context context, DictionaryEntry entry) {
        StringBuilder builder = new StringBuilder(context.getString(R.string.share_dictionary_title, entry.word));
        for (DictionaryEntry.DictionaryEntryDetails details : entry.details) {
            builder.append(context.getString(R.string.share_dictionary_entry, details.partOfSpeech, details.definition));
        }
        return builder.toString();
    }

    private static PendingIntent getShareIntent(Context context, DictionaryEntry entry) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, buildWotdShareContent(context, entry));
        intent.setType("text/plain");
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
