package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class GthennsTopBarSystemTest {

    private void seedUser(int userId, String username, String role) {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();
        SessionManager.clearSession(context);
        new KeepUser(context).saveUser(userId, username, role);
    }

    @Test
    public void profileScreen_topBarShowsBackButton() {
        seedUser(41, "profile-topbar", "normal");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button backButton = activity.findViewById(R.id.btnTopBack);
                Button homeButton = activity.findViewById(R.id.btnTopHome);

                assertEquals(View.VISIBLE, backButton.getVisibility());
                assertTrue(homeButton.isShown());
            });
        }
    }

    @Test
    public void topBarShowsNotificationAndUserMenuControlsOnMainScreen() {
        Context context = ApplicationProvider.getApplicationContext();
        seedUser(42, "welcome-user", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());
        AppNotificationManager.markNotificationsRead(context);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                ImageButton notificationButton = activity.findViewById(R.id.btnTopNotification);
                ImageButton userMenuButton = activity.findViewById(R.id.btnTopUserMenu);
                View notificationDot = activity.findViewById(R.id.viewTopNotificationDot);

                assertNotNull(notificationButton);
                assertNotNull(userMenuButton);
                assertNotNull(notificationDot);
                assertEquals("Notifications", String.valueOf(notificationButton.getContentDescription()));
                assertEquals("Profile menu", String.valueOf(userMenuButton.getContentDescription()));
            });
        }
    }

    @Test
    public void refreshNotificationIndicator_hidesAndShowsDot() {
        Context context = ApplicationProvider.getApplicationContext();
        seedUser(43, "dot-user", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View notificationDot = activity.findViewById(R.id.viewTopNotificationDot);

                AppNotificationManager.setHasUnreadNotifications(activity, false);
                AppTopBarController.refreshNotificationIndicator(activity);
                assertEquals(View.GONE, notificationDot.getVisibility());

                AppNotificationManager.setHasUnreadNotifications(activity, true);
                AppTopBarController.refreshNotificationIndicator(activity);
                assertEquals(View.VISIBLE, notificationDot.getVisibility());
            });
        }
    }
}