package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsProfileRoleVisibilitySystemTest {

    @Test
    public void guestSession_usesFallbackIdentityAndKeepsAdminHidden() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        SessionManager.clearSession(context);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                LinearLayout adminSection = activity.findViewById(R.id.layoutAdminSection);
                TextView username = activity.findViewById(R.id.tvCurrentUsername);
                TextView role = activity.findViewById(R.id.tvCurrentRole);
                Button updateProfile = activity.findViewById(R.id.btnUpdateProfile);

                assertEquals(View.GONE, adminSection.getVisibility());
                assertTrue(username.getText().toString().startsWith("Username: "));
                assertTrue(role.getText().toString().startsWith("Role: "));
                assertTrue(updateProfile.isShown());
            });
        }
    }

    @Test
    public void normalUser_hidesAdminSectionAndShowsIdentityLabels() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(31, "gthenns-user", "normal");
        SessionManager.clearSession(context);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                LinearLayout adminSection = activity.findViewById(R.id.layoutAdminSection);
                TextView username = activity.findViewById(R.id.tvCurrentUsername);
                TextView role = activity.findViewById(R.id.tvCurrentRole);

                assertEquals(View.GONE, adminSection.getVisibility());
                assertTrue(username.getText().toString().startsWith("Username: "));
                assertTrue(role.getText().toString().toLowerCase().contains("role: "));
            });
        }
    }

    @Test
    public void adminUser_showsAdminSection() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(1, "gthenns-admin", "admin");
        SessionManager.clearSession(context);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                LinearLayout adminSection = activity.findViewById(R.id.layoutAdminSection);
                TextView role = activity.findViewById(R.id.tvCurrentRole);

                assertEquals(View.VISIBLE, adminSection.getVisibility());
                assertTrue(role.getText().toString().toLowerCase().contains("admin"));
            });
        }
    }

    @Test
    public void profileScreen_showsStableUserControlsForNormalUser() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.clearUser();
        keepUser.saveUser(32, "profile-user", "normal");
        SessionManager.clearSession(context);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button updateProfile = activity.findViewById(R.id.btnUpdateProfile);
                Button deletePage = activity.findViewById(R.id.btnGoToDeletePage);
                TextView userId = activity.findViewById(R.id.tvProfileUserId);

                assertTrue(updateProfile.isShown());
                assertTrue(deletePage.isShown());
                assertTrue(userId.getText().toString().startsWith("User ID: "));
            });
        }
    }
}
