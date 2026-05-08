package com.example.androidexample;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GthennsNotificationsSystemTest {

    @Test
    public void getLatestNotification_whenEmpty_returnsFallbackMessage() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(81, "notify-empty", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        String latest = AppNotificationManager.getLatestNotification(context);
        assertEquals("No notifications yet", latest);
    }

    @Test
    public void addNotification_blankMessage_isIgnored() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(82, "notify-ignore", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        AppNotificationManager.addNotification(context, "   ");

        List<String> items = AppNotificationManager.getNotifications(context);
        assertTrue(items.isEmpty());
    }

    @Test
    public void addNotification_storesLatestMessageForCurrentUser() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(17, "notify-user", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        AppNotificationManager.addNotification(context, "Hydrate before workout");

        String latest = AppNotificationManager.getLatestNotification(context);
        assertTrue(latest.contains("Hydrate before workout"));
    }

    @Test
    public void notifications_areIsolatedByUserId() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);

        keepUser.clearUser();
        keepUser.saveUser(51, "user-51", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());
        AppNotificationManager.addNotification(context, "Message for user 51");

        keepUser.clearUser();
        keepUser.saveUser(52, "user-52", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());
        AppNotificationManager.addNotification(context, "Message for user 52");

        keepUser.clearUser();
        keepUser.saveUser(51, "user-51", "normal");
        List<String> user51 = AppNotificationManager.getNotifications(context);

        assertFalse(user51.isEmpty());
        assertTrue(user51.get(0).contains("Message for user 51"));
    }

    @Test
    public void getNewestNotifications_respectsRequestedLimitAndOrder() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(61, "notify-limit", "normal");

        ArrayList<String> seeded = new ArrayList<>();
        seeded.add("Newest");
        seeded.add("Middle");
        seeded.add("Oldest");
        AppNotificationManager.replaceNotifications(context, seeded);

        List<String> newestTwo = AppNotificationManager.getNewestNotifications(context, 2);
        assertEquals(2, newestTwo.size());
        assertEquals("Newest", newestTwo.get(0));
        assertEquals("Middle", newestTwo.get(1));
    }

    @Test
    public void addNotification_capsStoredListAtThirtyItems() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(71, "notify-cap", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        for (int i = 1; i <= 35; i++) {
            AppNotificationManager.addNotification(context, "bulk-" + i);
        }

        List<String> items = AppNotificationManager.getNotifications(context);
        assertEquals(30, items.size());
        assertTrue(items.get(0).contains("bulk-35"));
        assertTrue(items.get(items.size() - 1).contains("bulk-6"));
    }

    @Test
    public void replaceNotifications_withNullList_clearsItemsAndUnread() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(83, "notify-null", "normal");

        ArrayList<String> seeded = new ArrayList<>();
        seeded.add("hello");
        AppNotificationManager.replaceNotifications(context, seeded, true);

        AppNotificationManager.replaceNotifications(context, null, true);

        assertTrue(AppNotificationManager.getNotifications(context).isEmpty());
        assertTrue(!AppNotificationManager.hasUnreadNotifications(context));
    }

    @Test
    public void markNotificationsRead_turnsUnreadOff() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(84, "notify-read", "normal");

        AppNotificationManager.setHasUnreadNotifications(context, true);
        AppNotificationManager.markNotificationsRead(context);

        assertTrue(!AppNotificationManager.hasUnreadNotifications(context));
    }

    @Test
    public void getNewestNotifications_withNonPositiveLimit_returnsAll() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(85, "notify-limit0", "normal");

        ArrayList<String> seeded = new ArrayList<>();
        seeded.add("one");
        seeded.add("two");
        AppNotificationManager.replaceNotifications(context, seeded);

        List<String> newest = AppNotificationManager.getNewestNotifications(context, 0);
        assertEquals(2, newest.size());
        assertEquals("one", newest.get(0));
    }

    @Test
    public void replaceNotifications_trimsAndSkipsBlankItems() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(86, "notify-clean", "normal");

        ArrayList<String> seeded = new ArrayList<>();
        seeded.add("   keep me   ");
        seeded.add("   ");
        seeded.add(null);
        seeded.add("also-keep");

        AppNotificationManager.replaceNotifications(context, seeded, true);

        List<String> items = AppNotificationManager.getNotifications(context);
        assertEquals(2, items.size());
        assertEquals("keep me", items.get(0));
        assertEquals("also-keep", items.get(1));
        assertTrue(AppNotificationManager.hasUnreadNotifications(context));
    }

    @Test
    public void notifications_guestBucketIsUsedWhenNoUserSessionExists() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();
        SessionManager.clearSession(context);
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());

        AppNotificationManager.addNotification(context, "guest-message");

        List<String> items = AppNotificationManager.getNotifications(context);
        assertEquals(1, items.size());
        assertTrue(items.get(0).contains("guest-message"));
    }

    @Test
    public void notifications_preserveLeadingStyleMarkerEntries() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(87, "notify-style", "normal");

        ArrayList<String> seeded = new ArrayList<>();
        seeded.add("\u0001Bold item");
        seeded.add("\u0002Italic item");
        AppNotificationManager.replaceNotifications(context, seeded, true);

        List<String> items = AppNotificationManager.getNotifications(context);
        assertEquals('\u0001', items.get(0).charAt(0));
        assertEquals('\u0002', items.get(1).charAt(0));
    }
}
