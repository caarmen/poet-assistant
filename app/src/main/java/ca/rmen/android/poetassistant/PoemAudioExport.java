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

package ca.rmen.android.poetassistant;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import ca.rmen.android.poetassistant.dagger.DaggerHelper;
import ca.rmen.android.poetassistant.main.MainActivity;
import ca.rmen.android.poetassistant.main.dictionaries.Share;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class PoemAudioExport {
    private static final String TAG = Constants.TAG + PoemAudioExport.class.getSimpleName();
    private static final int EXPORT_PROGRESS_NOTIFICATION_ID = 1336;
    private static final int EXPORT_FINISH_NOTIFICATION_ID = 1337;

    private static final String EXPORT_FOLDER_PATH = "export";
    private static final String TEMP_AUDIO_FILE = "poem.wav";

    private final Context mContext;

    @Inject
    Tts mTts;

    PoemAudioExport(Context context) {
        mContext = context;
        DaggerHelper.getMainScreenComponent(context).inject(this);
    }

    void speakToFile(TextToSpeech textToSpeech, String text) {
        final File audioFile = getAudioFile();
        if (audioFile == null) {
            notifyPoemAudioFailed();
        } else {
            mTts.getTtsLiveData().observeForever(mTtsObserver);
            notifyPoemAudioInProgress();
            String textToRead = text.substring(0, Math.min(text.length(), TextToSpeech.getMaxSpeechInputLength()));
            Completable.fromRunnable(() -> deleteExistingAudioFile(audioFile))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(()-> speakToFile(textToSpeech, textToRead, audioFile));
        }
    }

    @WorkerThread
    private void deleteExistingAudioFile(File audioFile) {
        if (audioFile.exists()) {
            if (audioFile.delete()) {
                Log.v(TAG, "Deleted existing file " + audioFile + ".");
            } else {
                Log.v(TAG, "Couldn't delete existing file " + audioFile + ". What will happen next?");
            }
        }
    }

    @MainThread
    private void speakToFile(TextToSpeech textToSpeech, String textToRead, File audioFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakToFile21(textToSpeech, textToRead, audioFile);
        } else {
            speakToFile4(textToSpeech, textToRead, audioFile);
        }
    }

    private void speakToFile4(TextToSpeech textToSpeech, String text, File audioFile) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TEMP_AUDIO_FILE);
        //noinspection deprecation
        textToSpeech.synthesizeToFile(text, params, audioFile.getAbsolutePath());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakToFile21(TextToSpeech textToSpeech, String text, File audioFile) {
        Bundle params = new Bundle();
        textToSpeech.synthesizeToFile(text, params, audioFile, TEMP_AUDIO_FILE);
    }


    private void cancelNotifications() {
        Log.v(TAG, "cancelNotifications");
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(EXPORT_PROGRESS_NOTIFICATION_ID);
            notificationManager.cancel(EXPORT_FINISH_NOTIFICATION_ID);
        }
    }

    private void notifyPoemAudioInProgress() {
        Log.v(TAG, "notifyPoemAudioInProgress");
        cancelNotifications();
        Notification notification = new NotificationCompat.Builder(mContext, NotificationChannel.INSTANCE.createNotificationChannel(mContext))
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(getMainActivityIntent())
                .setContentTitle(mContext.getString(R.string.share_poem_audio_progress_notification_title))
                .setContentText(mContext.getString(R.string.share_poem_audio_progress_notification_message))
                .setSmallIcon(Share.getNotificationIcon())
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(EXPORT_PROGRESS_NOTIFICATION_ID, notification);
        }
    }

    private void notifyPoemAudioReady() {
        Log.v(TAG, "notifyPoemAudioReady");
        cancelNotifications();
        PendingIntent shareIntent = getFileShareIntent();
        if (shareIntent != null) {
            Notification notification = new NotificationCompat.Builder(mContext, NotificationChannel.INSTANCE.createNotificationChannel(mContext))
                    .setAutoCancel(true)
                    .setContentIntent(shareIntent)
                    .setContentTitle(mContext.getString(R.string.share_poem_audio_ready_notification_title))
                    .setContentText(mContext.getString(R.string.share_poem_audio_ready_notification_message))
                    .setSmallIcon(Share.getNotificationIcon())
                    .addAction(
                            Share.getShareIconId(),
                            mContext.getString(R.string.share),
                            shareIntent)
                    .build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(EXPORT_FINISH_NOTIFICATION_ID, notification);
            }
        }
    }

    private void notifyPoemAudioFailed() {
        Log.v(TAG, "notifyPoemAudioFailed");
        cancelNotifications();
        Notification notification = new NotificationCompat.Builder(mContext, NotificationChannel.INSTANCE.createNotificationChannel(mContext))
                .setAutoCancel(true)
                .setContentTitle(mContext.getString(R.string.share_poem_audio_error_notification_title))
                .setContentText(mContext.getString(R.string.share_poem_audio_error_notification_message))
                .setContentIntent(getMainActivityIntent())
                .setSmallIcon(Share.getNotificationIcon())
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(EXPORT_FINISH_NOTIFICATION_ID, notification);
        }
    }

    @Nullable
    private PendingIntent getFileShareIntent() {
        File file = getAudioFile();
        if (file != null) {
            // Bring up the chooser to share the file.
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("audio/x-wav");
            return PendingIntent.getActivity(mContext, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            return null;
        }
    }

    private PendingIntent getMainActivityIntent() {
        Intent intent = new Intent(mContext, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    private File getAudioFile() {
        File exportFolder = new File(mContext.getFilesDir(), EXPORT_FOLDER_PATH);
        if (!exportFolder.exists() && !exportFolder.mkdirs()) {
            Log.v(TAG, "Couldn't find or create export folder " + exportFolder);
            return null;
        }
        return new File(exportFolder, TEMP_AUDIO_FILE);
    }

    private final Observer<TtsState> mTtsObserver = new Observer<TtsState>() {
        @Override
        public void onChanged(@Nullable TtsState ttsState) {
            if (ttsState != null
                    && (ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE || ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_ERROR)
                    && TEMP_AUDIO_FILE.equals(ttsState.utteranceId)) {
                mTts.getTtsLiveData().removeObserver(this);
                File audioFile = getAudioFile();
                if (ttsState.currentStatus == TtsState.TtsStatus.UTTERANCE_COMPLETE && audioFile != null && audioFile.exists()) notifyPoemAudioReady();
                else notifyPoemAudioFailed();
            }
        }
    };
}
