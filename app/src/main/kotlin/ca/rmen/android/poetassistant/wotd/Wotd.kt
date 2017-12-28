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

package ca.rmen.android.poetassistant.wotd

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.NotificationChannel
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.dictionaries.Share
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.Dictionary
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.settings.Settings
import ca.rmen.android.poetassistant.settings.SettingsPrefs
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Word of the day system notification.
 */
object Wotd {
    private val TAG = Constants.TAG + Wotd::class.java.simpleName

    const val NOTIFICATION_FREQUENCY_MS = (24 * 60 * 60 * 1000).toLong()

    fun setWotdEnabled(context : Context, dictionary : Dictionary, enabled : Boolean) {
        Log.v(TAG, "setWotdEnabled $enabled")
        if (enabled) enableWotd(context, dictionary)
        else disableWotd(context)
    }

    /**
     * If we have the wotd setting enabled, but no task has been scheduled
     * to do the wotd, we'll schedule the task.
     */

    fun reschedule(context : Context, settingsPrefs : SettingsPrefs) {
        Log.d(TAG, "reschedule")
        if (settingsPrefs.isWotdEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WotdJob.reschedule(context)
            } else {
                WotdAlarm.reschedule(context)
            }
        }
    }

    private fun enableWotd(context : Context, dictionary : Dictionary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WotdJob.schedule(context)
        } else {
            WotdAlarm.schedule(context)
        }
        val threading = DaggerHelper.getWotdComponent(context).getThreading()
        threading.execute({ notifyWotd(context, dictionary) })
    }

    private fun disableWotd(context : Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WotdJob.cancel(context)
        } else {
            WotdAlarm.cancel(context)
        }
    }

    fun getTodayUTC() : Calendar {
        val now = Calendar.getInstance()
        now.timeZone = TimeZone.getTimeZone("UTC")
        now[Calendar.HOUR_OF_DAY] = 0
        now[Calendar.MINUTE] = 0
        now[Calendar.SECOND] = 0
        now[Calendar.MILLISECOND] = 0
        return now
    }

    fun notifyWotd(context : Context, dictionary : Dictionary) {
        Log.v(TAG, "notifyWotd")
        val entry = dictionary.getRandomEntry(getTodayUTC().timeInMillis) ?: return
        val title = context.getString(R.string.wotd_notification_title, entry.word)
        val content = buildWotdNotificationContent(context, entry)
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(content)
        val uri = Uri.parse(String.format("poetassistant://%s/%s", Constants.DEEP_LINK_QUERY, entry.word))
        val intent = Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(uri)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(context, NotificationChannel.createNotificationChannel(context))
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = Settings.NotificationPriority.valueOf(SettingsPrefs.get(context).wotdNotificationPriority.toUpperCase(Locale.US)).priority
        }
        val notification = builder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(TAG.hashCode(), notification)
    }

    private fun buildWotdNotificationContent(context : Context, entry : DictionaryEntry) : CharSequence {
        val builder = StringBuilder(entry.word)
        entry.details.forEach { builder.append(context.getString(R.string.wotd_notification_definition, it.partOfSpeech, it.definition)) }
        return HtmlCompat.fromHtml(builder.toString())
    }

    private fun buildWotdShareContent(context : Context, entry : DictionaryEntry) : CharSequence {
        val builder = StringBuilder(context.getString(R.string.share_dictionary_title, entry.word))
        entry.details.forEach { builder.append(context.getString(R.string.share_dictionary_entry, it.partOfSpeech, it.definition)) }
        return builder.toString()
    }

    private fun getShareIntent(context : Context, entry : DictionaryEntry) : PendingIntent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, buildWotdShareContent(context, entry))
        intent.type = "text/plain"
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
