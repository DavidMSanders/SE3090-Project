package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    private static final String PREFS_NAME = "app_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SessionManager() {
    }

    public static void saveLoggedInUser(Context context, String username, String userId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_USERNAME, username == null ? "" : username)
                .putString(KEY_USER_ID, userId == null ? "" : userId)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public static String getUsername(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = preferences.getString(KEY_USERNAME, "");
        return username == null || username.trim().isEmpty() ? "Guest" : username.trim();
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = preferences.getString(KEY_USER_ID, "");
        return userId == null ? "" : userId.trim();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void clearSession(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }
}
