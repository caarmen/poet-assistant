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

package ca.rmen.android.poetassistant

import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import ca.rmen.android.poetassistant.main.MainActivity
import ca.rmen.android.poetassistant.main.dictionaries.Share
import java.io.File

@TargetApi(Build.VERSION_CODES.KITKAT)
class PoemAudioExport(val context: Context, private val threading: Threading, private val mTts: Tts) {
    companion object {
        private val TAG = Constants.TAG + PoemAudioExport::class.java.simpleName
        private const val EXPORT_PROGRESS_NOTIFICATION_ID = 1336
        private const val EXPORT_FINISH_NOTIFICATION_ID = 1337
        private const val EXPORT_FOLDER_PATH = "export"
        private const val TEMP_AUDIO_FILE = "poem.wav"
    }

    fun speakToFile(textToSpeech: TextToSpeech, text: String) {
        val audioFile = getAudioFile()
        if (audioFile == null) {
            notifyPoemAudioFailed()
        } else {
            mTts.getTtsLiveData().observeForever(mTtsObserver)
            notifyPoemAudioInProgress()
            val textToRead = text.substring(0, Math.min(text.length, TextToSpeech.getMaxSpeechInputLength()))
            threading.execute({ deleteExistingAudioFile(audioFile) },
                    { speakToFile(textToSpeech, textToRead, audioFile) })
        }
    }

    @WorkerThread
    private fun deleteExistingAudioFile(audioFile: File) {
        if (audioFile.exists()) {
            if (audioFile.delete()) {
                Log.v(TAG, "Deleted existing file $audioFile.")
            } else {
                Log.v(TAG, "Couldn't delete existing file $audioFile. What will happen next?")
            }
        }
    }

    @MainThread
    private fun speakToFile(textToSpeech: TextToSpeech, textToRead: String, audioFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakToFile21(textToSpeech, textToRead, audioFile)
        } else {
            speakToFile4(textToSpeech, textToRead, audioFile)
        }
    }

    private fun speakToFile4(textToSpeech: TextToSpeech, text: String, audioFile: File) {
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = TEMP_AUDIO_FILE
        @Suppress("DEPRECATION")
        textToSpeech.synthesizeToFile(text, params, audioFile.absolutePath)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speakToFile21(textToSpeech: TextToSpeech, text: String, audioFile: File) {
        textToSpeech.synthesizeToFile(text, Bundle(), audioFile, TEMP_AUDIO_FILE)
    }

    private fun cancelNotifications() {
        Log.v(TAG, "cancelNotifications")
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.let {
            it.cancel(EXPORT_PROGRESS_NOTIFICATION_ID)
            it.cancel(EXPORT_FINISH_NOTIFICATION_ID)
        }
    }

    private fun notifyPoemAudioInProgress() {
        Log.v(TAG, "notifyPoemAudioInProgress")
        cancelNotifications()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.
                notify(EXPORT_PROGRESS_NOTIFICATION_ID, NotificationCompat.Builder(context, NotificationChannel.createNotificationChannel(context))
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentIntent(getMainActivityIntent())
                        .setContentTitle(context.getString(R.string.share_poem_audio_progress_notification_title))
                        .setContentText(context.getString(R.string.share_poem_audio_progress_notification_message))
                        .setSmallIcon(Share.getNotificationIcon())
                        .build())
    }

    private fun notifyPoemAudioReady() {
        Log.v(TAG, "notifyPoemAudioReady")
        cancelNotifications()
        val shareIntent = getFileShareIntent()
        if (shareIntent != null) {
            if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } else {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.let {
                    val pendingIntent =
                            PendingIntent.getActivity(context, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                    it.notify(EXPORT_FINISH_NOTIFICATION_ID, NotificationCompat.Builder(context, NotificationChannel.createNotificationChannel(context))
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setContentTitle(context.getString(R.string.share_poem_audio_ready_notification_title))
                            .setContentText(context.getString(R.string.share_poem_audio_ready_notification_message))
                            .setSmallIcon(Share.getNotificationIcon())
                            .addAction(
                                    Share.getShareIconId(),
                                    context.getString(R.string.share),
                                    pendingIntent)
                            .build())
                }
            }
        }
    }

    private fun notifyPoemAudioFailed() {
        Log.v(TAG, "notifyPoemAudioFailed")
        cancelNotifications()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.
                notify(EXPORT_FINISH_NOTIFICATION_ID, NotificationCompat.Builder(context, NotificationChannel.createNotificationChannel(context))
                        .setAutoCancel(true)
                        .setContentTitle(context.getString(R.string.share_poem_audio_error_notification_title))
                        .setContentText(context.getString(R.string.share_poem_audio_error_notification_message))
                        .setSmallIcon(Share.getNotificationIcon())
                        .build())
    }

    private fun getFileShareIntent(): Intent? {
        val file = getAudioFile()
        return if (file != null) {
            // Bring up the chooser to share the file.
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file)
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
            sendIntent.type = "audio/x-wav"
            Intent.createChooser(sendIntent, context.getString(R.string.share_poem_audio))
        } else {
            null
        }
    }

    private fun getMainActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private fun getAudioFile(): File? {
        val exportFolder = File(context.filesDir, EXPORT_FOLDER_PATH)
        return if (!exportFolder.exists() && !exportFolder.mkdirs()) {
            Log.v(TAG, "Couldn't find or create export folder $exportFolder")
            null
        } else {
            File(exportFolder, TEMP_AUDIO_FILE)
        }
    }

    private val mTtsObserver = object : Observer<TtsState?> {
        override fun onChanged(ttsState: TtsState?) {
            if (ttsState != null
                    && (ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE || ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_ERROR)
                    && TEMP_AUDIO_FILE == ttsState.utteranceId) {
                mTts.getTtsLiveData().removeObserver(this)
                val audioFile = getAudioFile()
                if (ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE && audioFile != null && audioFile.exists()) notifyPoemAudioReady()
                else notifyPoemAudioFailed()
            }
        }
    }
}
