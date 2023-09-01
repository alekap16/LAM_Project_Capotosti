package com.example.lam_project.managers;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lam_project.R;

public class NotificationsManager {
    private static final String PREF_NAME = "notifications_preferences";
    private static final String NOTIFICATION_SWITCH = "Notification";
    private static final String NOTIFICATION_SWITCH_10M = "Notify10m";
    private static final String NOTIFICATION_SWITCH_100M = "Notify100m";
    private static final String NOTIFICATION_SWITCH_1KM = "Notify1km";
    private static final String NOTIFICATION_SWITCH_EXPIRY = "NotifyExpires";
    private static final String CHANNEL_ID = "Notifications";

    private static final int NOTIFICATION_ID_10M = 1;
    private static final int NOTIFICATION_ID_100M = 2;
    private static final int NOTIFICATION_ID_1KM = 3;
    private static final int NOTIFICATION_ID_EXPIRED = 4;
    private static final long COOLDOWN_RANGES_DURATION = 10 * 60 * 1000;
    private static final long COOLDOWN_EXPIRY_DURATION = 5 * 60 * 1000;
    private static long lastNotification10M = 0;
    private static long lastNotification100M = 0;
    private static long lastNotification1KM = 0;
    private static long lastNotificationExpiry = 0;
    private SharedPreferences preferences;

    public NotificationsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean areNotificationsEnabled(){
        return preferences.getBoolean(NOTIFICATION_SWITCH, false);
    }
    public boolean is10MNotificationsEnabled() {
        return preferences.getBoolean(NOTIFICATION_SWITCH_10M, false);
    }
    public boolean is100MNotificationsEnabled() {
        return preferences.getBoolean(NOTIFICATION_SWITCH_100M, false);
    }
    public boolean is1KMNotificationsEnabled() {
        return preferences.getBoolean(NOTIFICATION_SWITCH_1KM, false);
    }
    public boolean isExpiryNotificationsEnabled() {
        return preferences.getBoolean(NOTIFICATION_SWITCH_EXPIRY, false);
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void show10MNotification(Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotification10M >= COOLDOWN_RANGES_DURATION) {
            NotificationCompat.Builder builder = createNotificationBuilder(context, "10M", "10 meters measurements not taken in this area!");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_10M, builder.build());

            lastNotification10M = currentTime;
        }
    }

    public static void show100MNotification(Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotification100M >= COOLDOWN_RANGES_DURATION) {
            NotificationCompat.Builder builder = createNotificationBuilder(context, "100M", "100 meters measurements not taken in this area!");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_100M, builder.build());

            lastNotification100M = currentTime;
        }
    }

    public static void show1KMNotification(Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotification1KM >= COOLDOWN_RANGES_DURATION) {
            NotificationCompat.Builder builder = createNotificationBuilder(context, "1KM", "1 km measurements not taken in this area!");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_1KM, builder.build());

            lastNotification1KM = currentTime;
        }
    }

    public static void showExpiredNotification(Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotificationExpiry >= COOLDOWN_EXPIRY_DURATION) {
            NotificationCompat.Builder builder = createNotificationBuilder(context, "Expired", "Your measurements expired");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_EXPIRED, builder.build());

            lastNotificationExpiry = currentTime;
        }
    }

    private static NotificationCompat.Builder createNotificationBuilder(Context context, String iconText, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_manual_scan)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_manual_scan))
                .setContentTitle(iconText)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        return builder;
    }
}

