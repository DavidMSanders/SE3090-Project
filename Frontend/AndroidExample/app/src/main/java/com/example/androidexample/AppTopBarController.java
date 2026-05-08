package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.activity.ComponentActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public final class AppTopBarController {

    private AppTopBarController() {
    }

    public static void refreshNotificationIndicator(Activity activity) {
        if (activity == null) {
            return;
        }
        View notificationDot = activity.findViewById(R.id.viewTopNotificationDot);
        updateNotificationDot(notificationDot, activity);
    }

    public static void setup(Activity activity) {
        Button backButton = activity.findViewById(R.id.btnTopBack);
        View notificationButton = activity.findViewById(R.id.btnTopNotification);
        View notificationDot = activity.findViewById(R.id.viewTopNotificationDot);
        Button homeButton = activity.findViewById(R.id.btnTopHome);
        View userMenuButton = activity.findViewById(R.id.btnTopUserMenu);

        if (backButton == null || notificationButton == null || homeButton == null || userMenuButton == null) {
            return;
        }

        View topBarContainer = activity.findViewById(R.id.topBarContainer);
        if (topBarContainer == null) {
            View parent = (notificationButton.getParent() instanceof View) ? (View) notificationButton.getParent() : null;
            View grandParent = (parent != null && parent.getParent() instanceof View) ? (View) parent.getParent() : null;
            topBarContainer = grandParent != null ? grandParent : parent;
        }

        if (topBarContainer == null) {
            return;
        }

        final int baseTopPadding = topBarContainer.getPaddingTop();
        final int baseBottomPadding = topBarContainer.getPaddingBottom();
        final int baseStartPadding = topBarContainer.getPaddingStart();
        final int baseEndPadding = topBarContainer.getPaddingEnd();
        final boolean isCalendarScreen = activity instanceof CalendarActivity;

        ViewCompat.setOnApplyWindowInsetsListener(topBarContainer, (view, insets) -> {
            int statusBarInsetTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int adjustedBaseTopPadding = isCalendarScreen ? 0 : baseTopPadding;
            view.setPadding(
                    baseStartPadding,
                adjustedBaseTopPadding + statusBarInsetTop,
                    baseEndPadding,
                    baseBottomPadding
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(topBarContainer);

        KeepUser keepUser = new KeepUser(activity);
        String username = keepUser.getDisplayUsername();

        if (activity instanceof MainActivity) {
            backButton.setVisibility(View.GONE);
        } else {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(v -> {
                if (activity instanceof ComponentActivity) {
                    ((ComponentActivity) activity).getOnBackPressedDispatcher().onBackPressed();
                } else {
                    activity.onBackPressed();
                }
            });
        }

        AppNotificationManager.requestNotificationPermissionIfNeeded(activity);
        NotificationSyncManager.getInstance(activity).startForCurrentUser();

        if ("No notifications yet".equals(AppNotificationManager.getLatestNotification(activity))) {
            AppNotificationManager.addNotification(activity, "Welcome, " + username + "!");
        }

        updateNotificationDot(notificationDot, activity);

        homeButton.setOnClickListener(v -> {
            if (!(activity instanceof MainActivity)) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        });

        notificationButton.setOnClickListener(v -> {
            showNotificationsDropdown(activity, v);
            AppNotificationManager.markNotificationsRead(activity);
            updateNotificationDot(notificationDot, activity);
        });

        userMenuButton.setOnClickListener(v -> showUserMenu(activity, v));
    }

    private static void updateNotificationDot(View notificationDot, Activity activity) {
        if (notificationDot == null) {
            return;
        }

        boolean hasUnread = AppNotificationManager.hasUnreadNotifications(activity);
        notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    private static void showNotificationsDropdown(Activity activity, View anchor) {
        List<String> notifications = AppNotificationManager.getNewestNotifications(activity, 5);
        PopupMenu popupMenu = new PopupMenu(activity, anchor);

        popupMenu.getMenu().add(0, 2000, 0, "Newest notifications").setEnabled(false);

        if (notifications.isEmpty()) {
            popupMenu.getMenu().add(0, 2001, 1, "No notifications yet").setEnabled(false);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                String raw = notifications.get(i);
                CharSequence label;
                if (raw.startsWith("\u0001")) {
                    String text = raw.substring(1);
                    SpannableString bold = new SpannableString(text);
                    bold.setSpan(new StyleSpan(Typeface.BOLD), 0, bold.length(), 0);
                    label = bold;
                } else if (raw.startsWith("\u0002")) {
                    String text = raw.substring(1);
                    SpannableString italic = new SpannableString(text);
                    italic.setSpan(new StyleSpan(Typeface.ITALIC), 0, italic.length(), 0);
                    label = italic;
                } else {
                    label = raw;
                }
                popupMenu.getMenu().add(0, 2100 + i, i + 1, label).setEnabled(false);
            }
        }

        popupMenu.show();
    }

    private static void showUserMenu(Activity activity, View anchor) {
        PopupMenu popupMenu = new PopupMenu(activity, anchor);
        KeepUser keepUser = new KeepUser(activity);
        popupMenu.getMenuInflater().inflate(R.menu.user_dropdown_menu, popupMenu.getMenu());

        popupMenu.getMenu().findItem(R.id.menu_username)
                .setTitle("Signed in as: " + keepUser.getDisplayUsername());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.menu_profile) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
                return true;
            }

            if (itemId == R.id.menu_inbox) {
                activity.startActivity(new Intent(activity, GroupChatActivity.class));
                return true;
            }

            if (itemId == R.id.menu_logout) {
                NotificationSyncManager.getInstance(activity).stop();
                keepUser.clearUser();
                SessionManager.clearSession(activity);
                Intent intent = new Intent(activity, LandingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

}
