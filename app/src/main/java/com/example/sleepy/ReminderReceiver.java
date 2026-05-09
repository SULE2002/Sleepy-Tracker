package com.example.sleepy;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String REMINDER_ENABLED_KEY = "daily_reminder_enabled";
    private static final String REMINDER_HOUR_KEY = "daily_reminder_hour";
    private static final String REMINDER_MINUTE_KEY = "daily_reminder_minute";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(REMINDER_ENABLED_KEY, false);
        if (!enabled) return;

        if (Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String channelId = "sleepy_daily_reminder";

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Daily Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily sleep check-up reminders");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sleepy Check-Up")
                .setContentText("Don’t forget to complete your daily sleep check-up.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(1001, builder.build());

        int hour = prefs.getInt(REMINDER_HOUR_KEY, 22);
        int minute = prefs.getInt(REMINDER_MINUTE_KEY, 0);

        ReminderScheduler.scheduleDailyReminder(context, hour, minute);
    }
}