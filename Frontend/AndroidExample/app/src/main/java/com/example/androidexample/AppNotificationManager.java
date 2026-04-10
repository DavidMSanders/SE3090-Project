package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

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
    private static final String LEGACY_KEY_ITEMS = "items";
    private static final int MAX_ITEMS = 30;

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
        if (items == null) {
            saveNotifications(context, new ArrayList<>());
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
    }

    private static void saveNotifications(Context context, List<String> items) {
        JSONArray array = new JSONArray();
        for (String item : items) {
            array.put(item);
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(resolveItemsKey(context), array.toString()).apply();
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
}
