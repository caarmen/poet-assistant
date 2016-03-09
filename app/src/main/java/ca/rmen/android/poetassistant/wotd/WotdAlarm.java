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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;

/**
 * Word of the day task for API levels KitKat and lower.
 * This uses AlarmManager.
 */
final class WotdAlarm {
    private static final String TAG = Constants.TAG + WotdAlarm.class.getSimpleName();

    private static final String ACTION_WOTD = "action_wotd";

    private WotdAlarm() {
        // prevent instantiation
    }

    static void reschedule(Context context) {
        Log.d(TAG, "reschedule() called with: " + "context = [" + context + "]");
        Intent intent = new Intent(ACTION_WOTD);
        PendingIntent existingPendingIntent = PendingIntent.getBroadcast(context, TAG.hashCode(), intent, PendingIntent.FLAG_NO_CREATE);
        Log.v(TAG, "Existing pending intent: " + existingPendingIntent);
        if (existingPendingIntent == null) schedule(context);
    }

    static void schedule(Context context) {
        Log.d(TAG, "schedule() called with: " + "context = [" + context + "]");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                Wotd.NOTIFICATION_FREQUENCY_MS,
                getAlarmPendingIntent(context));
    }

    static void cancel(Context context) {
        Log.d(TAG, "cancel() called with: " + "context = [" + context + "]");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getAlarmPendingIntent(context));
    }

    private static PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(ACTION_WOTD);
        return PendingIntent.getBroadcast(context, TAG.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
