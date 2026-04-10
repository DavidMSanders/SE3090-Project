package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for keeping current user session info.
 */
public class KeepUser {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;

    /**
     * Constructor for KeepUser tied to shared preferences
     * @param context
     */
    public KeepUser(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves user session info and marks as loggedIn
     * @param userId
     * @param username
     * @param role
     */
    public void saveUser(int userId, String username, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     *  Gets user session info ID
     *
     * @return UserId, -1 if not found
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserIdString() {
        int userId = getUserId();
        return userId < 0 ? "" : String.valueOf(userId);
    }

    /**
     *  Gets user session info username
     *
     * @return Username, null if not found
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getDisplayUsername() {
        String username = getUsername();
        return username == null || username.trim().isEmpty() ? "Guest" : username.trim();
    }

    /**
     *  Gets user session info role
     *
     * @return Role, null if not found
     */
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getRoleOrDefault() {
        String role = getRole();
        return role == null || role.trim().isEmpty() ? "normal" : role.trim();
    }

    /**
     *  Gets user session info isLoggedIn
     *
     * @return True if logged in, false if not
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     *  Clears user session info
     *
     */
    public void clearUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); //clear all
        editor.apply();
    }
}
