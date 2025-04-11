package com.example.dk_habittracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "habit_reminder_channel";
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_LANGUAGE = "My_Lang";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANGUAGE, "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        Context localizedContext = context.createConfigurationContext(config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = localizedContext.getString(R.string.channel_name);
            String description = localizedContext.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = localizedContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_add)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ht_icon))
                .setContentTitle(localizedContext.getString(R.string.reminder_title))
                .setContentText(localizedContext.getString(R.string.reminder_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}
