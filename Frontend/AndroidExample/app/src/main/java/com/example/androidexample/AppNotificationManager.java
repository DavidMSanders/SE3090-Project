package com.example.androidexample;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AppNotificationManager {

    private static final String PREFS_NAME = "app_notifications";
    private static final String KEY_ITEMS_PREFIX = "items_";
    private static final String KEY_HAS_UNREAD_PREFIX = "has_unread_";
    private static final String LEGACY_KEY_ITEMS = "items";
    private static final String KEY_NEXT_SYSTEM_NOTIFICATION_ID = "next_system_notification_id";
    private static final int MAX_ITEMS = 30;
    private static final String SYSTEM_CHANNEL_ID = "general_notifications";
    private static final String SYSTEM_CHANNEL_NAME = "General notifications";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2201;

    private AppNotificationManager() {
    }

    public static void addNotification(Context context, String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        // Preserve style marker – trim() strips control characters (< '\u0020')
        char marker = (!message.isEmpty() && message.charAt(0) < '\u0020') ? message.charAt(0) : 0;
        String body = message.trim();

        List<String> items = getNotifications(context);
        String timestamp = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(new Date());
        String entry = timestamp + " - " + body;
        if (marker != 0) entry = marker + entry;
        items.add(0, entry);

        if (items.size() > MAX_ITEMS) {
            items = items.subList(0, MAX_ITEMS);
        }

        saveNotifications(context, items);
        setHasUnreadNotifications(context, true);
        showPhoneNotification(context, body);
    }

    public static void requestNotificationPermissionIfNeeded(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST_CODE
        );
    }

    public static String getLatestNotification(Context context) {
        List<String> items = getNotifications(context);
        return items.isEmpty() ? "No notifications yet" : items.get(0);
    }

    public static List<String> getNotifications(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = preferences.getString(resolveItemsKey(context), null);
        if (raw == null) {
            raw = preferences.getString(LEGACY_KEY_ITEMS, "[]");
        }

        List<String> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "");
                // trim() strips control chars — preserve leading style marker
                char m = (!value.isEmpty() && value.charAt(0) < '\u0020') ? value.charAt(0) : 0;
                value = value.trim();
                if (m != 0 && !value.isEmpty()) value = m + value;
                if (!value.isEmpty()) {
                    items.add(value);
                }
            }
        } catch (JSONException ignored) {
            // Ignore malformed data and return an empty list.
        }

        return items;
    }

    public static List<String> getNewestNotifications(Context context, int limit) {
        List<String> items = getNotifications(context);
        if (limit <= 0 || items.size() <= limit) {
            return items;
        }
        return new ArrayList<>(items.subList(0, limit));
    }

    public static void replaceNotifications(Context context, List<String> items) {
        replaceNotifications(context, items, hasUnreadNotifications(context));
    }

    public static void replaceNotifications(Context context, List<String> items, boolean hasUnread) {
        if (items == null) {
            saveNotifications(context, new ArrayList<>());
            setHasUnreadNotifications(context, false);
            return;
        }

        List<String> cleaned = new ArrayList<>();
        for (String item : items) {
            if (item != null) {
                // preserve leading style marker before trim()
                char m = (!item.isEmpty() && item.charAt(0) < '\u0020') ? item.charAt(0) : 0;
                String value = item.trim();
                if (m != 0 && !value.isEmpty()) value = m + value;
                if (!value.isEmpty()) {
                    cleaned.add(value);
                }
            }
            if (cleaned.size() >= MAX_ITEMS) {
                break;
            }
        }

        saveNotifications(context, cleaned);
        setHasUnreadNotifications(context, hasUnread && !cleaned.isEmpty());
    }

    public static boolean hasUnreadNotifications(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(resolveUnreadKey(context), false);
    }

    public static void setHasUnreadNotifications(Context context, boolean hasUnread) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(resolveUnreadKey(context), hasUnread).apply();
    }

    public static void markNotificationsRead(Context context) {
        setHasUnreadNotifications(context, false);
    }

    private static void saveNotifications(Context context, List<String> items) {
        JSONArray array = new JSONArray();
        for (String item : items) {
            array.put(item);
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(resolveItemsKey(context), array.toString()).apply();
    }

    private static void showPhoneNotification(Context context, String rawMessage) {
        String message = removeStyleMarker(rawMessage);
        if (message.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannelIfNeeded(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SYSTEM_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("AndroidExample")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(nextSystemNotificationId(context), builder.build());
        } catch (SecurityException ignored) {
            // Permission may be revoked while posting.
        }
    }

    private static String removeStyleMarker(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.charAt(0) < '\u0020') {
            return value.substring(1).trim();
        }
        return value.trim();
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel existing = manager.getNotificationChannel(SYSTEM_CHANNEL_ID);
        if (existing != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                SYSTEM_CHANNEL_ID,
                SYSTEM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alerts and updates from AndroidExample");
        manager.createNotificationChannel(channel);
    }

    private static int nextSystemNotificationId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = preferences.getInt(KEY_NEXT_SYSTEM_NOTIFICATION_ID, 1);
        int next = current == Integer.MAX_VALUE ? 1 : current + 1;
        preferences.edit().putInt(KEY_NEXT_SYSTEM_NOTIFICATION_ID, next).apply();
        return current;
    }

    private static String resolveItemsKey(Context context) {
        KeepUser keepUser = new KeepUser(context);
        String userId = keepUser.getUserIdString();
        if (!userId.isEmpty()) {
            return KEY_ITEMS_PREFIX + userId;
        }

        String legacyUserId = SessionManager.getUserId(context);
        if (!legacyUserId.isEmpty()) {
            return KEY_ITEMS_PREFIX + legacyUserId;
        }

        return KEY_ITEMS_PREFIX + "guest";
    }

    private static String resolveUnreadKey(Context context) {
        KeepUser keepUser = new KeepUser(context);
        String userId = keepUser.getUserIdString();
        if (!userId.isEmpty()) {
            return KEY_HAS_UNREAD_PREFIX + userId;
        }

        String legacyUserId = SessionManager.getUserId(context);
        if (!legacyUserId.isEmpty()) {
            return KEY_HAS_UNREAD_PREFIX + legacyUserId;
        }

        return KEY_HAS_UNREAD_PREFIX + "guest";
    }
}
