/*
 * Copyright (c) 2026-present Carmen Alvarez
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

package ca.rmen.android.poetassistant.main.wotd.usecases

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.NotificationChannel
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryEntry
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryRepository
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.WordNotFoundException
import ca.rmen.android.poetassistant.settings.SettingsRepository
import javax.inject.Inject
import androidx.core.net.toUri

/**
 * Posts the word-of-the-day system notification.
 *
 * The notification shows the word picked by [WotdSelection], with the
 * definitions from the dictionary. It deep-links to the dictionary query
 * for the word, and offers a share action.
 *
 * @param dictionaryRepository The dictionary repository for candidate words
 * and definitions. The repository performs its queries on the IO dispatcher.
 * @param settingsRepository The settings repository, for the notification
 * priority below API 26.
 * @param application The application context.
 */
class ShowWotdNotificationUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val settingsRepository: SettingsRepository,
    private val application: Application,
) {
    companion object {
        /**
         * Same notification id as the id used by devices which already have a
         * version of this app installed ("PoetAssistant/" + "Wotd"). Posts
         * with this id replace a previously posted word-of-the-day
         * notification instead of adding a new one.
         */
        private val NOTIFICATION_ID = "PoetAssistant/Wotd".hashCode()
    }

    /**
     * Posts the word-of-the-day notification for today.
     *
     * Does nothing if the dictionary DB is not loaded yet (no candidate
     * words).
     */
    suspend operator fun invoke() {
        val candidates = dictionaryRepository.getWordsInFrequencyRange(
            WotdSelection.MIN_INTERESTING_FREQUENCY,
            WotdSelection.MAX_INTERESTING_FREQUENCY
        )
        // The dictionary DB is not loaded yet: post nothing.
        if (candidates.isEmpty()) return
        val word = WotdSelection.pick(candidates, WotdSelection.todayUtc())

        // Safety net: all candidate words have dictionary entries with the
        // shipped data. If the DB content ever changes and the word has no
        // entry, post the word with no definitions.
        val entry = try {
            dictionaryRepository.lookup(word)
        } catch (_: WordNotFoundException) {
            DictionaryEntry(word, emptyList())
        }

        postNotification(entry)
    }

    private fun postNotification(entry: DictionaryEntry) {
        val title = application.getString(R.string.wotd_notification_title, entry.word)
        val content = buildNotificationContent(entry)
        val uri =
            String.format("poetassistant://%s/%s", Constants.DEEP_LINK_QUERY, entry.word).toUri()
        val intent = Intent(application, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(uri)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(application, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(application, NotificationChannel.createNotificationChannel(application))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentText(content)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_book_vector)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .addAction(
                R.drawable.ic_share_vector,
                application.getString(R.string.share),
                getSharePendingIntent(entry)
            )
        // Below API 26 the notification priority comes from the settings.
        // On API 26 and later the channel importance governs: do nothing.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = settingsRepository.settingsFlow.value.wotdNotificationPriority.priority
        }
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun buildNotificationContent(entry: DictionaryEntry): CharSequence {
        val builder = StringBuilder(entry.word)
        entry.details.forEach {
            builder.append(application.getString(R.string.wotd_notification_definition, it.partOfSpeech, it.definition))
        }
        return HtmlCompat.fromHtml(builder.toString())
    }

    private fun buildShareContent(entry: DictionaryEntry): String {
        val builder = StringBuilder(application.getString(R.string.share_dictionary_title, entry.word))
        entry.details.forEach {
            builder.append(application.getString(R.string.share_dictionary_entry, it.partOfSpeech, it.definition))
        }
        return builder.toString()
    }

    private fun getSharePendingIntent(entry: DictionaryEntry): PendingIntent {
        val intent = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, buildShareContent(entry))
            .setType("text/plain")
        return PendingIntent.getActivity(application, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
