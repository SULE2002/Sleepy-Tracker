package com.example.sleepy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String REMINDER_ENABLED_KEY = "daily_reminder_enabled";
    private static final String REMINDER_HOUR_KEY = "daily_reminder_hour";
    private static final String REMINDER_MINUTE_KEY = "daily_reminder_minute";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(REMINDER_ENABLED_KEY, false);
        if (!enabled) return;

        int hour = prefs.getInt(REMINDER_HOUR_KEY, 22);
        int minute = prefs.getInt(REMINDER_MINUTE_KEY, 0);

        ReminderScheduler.scheduleDailyReminder(context, hour, minute);
    }
}