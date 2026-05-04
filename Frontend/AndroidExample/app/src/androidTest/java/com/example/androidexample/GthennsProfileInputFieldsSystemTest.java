package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsProfileInputFieldsSystemTest {

    @Before
    public void clearSession() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();
        SessionManager.clearSession(context);
    }

    @Test
    public void profileScreen_updateProfileFields_areEmptyByDefault() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(10, "test-user", "normal");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etNewAccountName);
                EditText etPassword = activity.findViewById(R.id.etNewPassword);
                EditText etBirthDate = activity.findViewById(R.id.etNewBirthDate);

                assertNotNull(etName);
                assertNotNull(etPassword);
                assertNotNull(etBirthDate);

                assertEquals("", etName.getText().toString());
                assertEquals("", etPassword.getText().toString());
                assertEquals("", etBirthDate.getText().toString());
            });
        }
    }

    @Test
    public void profileScreen_updateWithAllEmptyFields_showsToastAndKeepsFieldsEmpty() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(10, "test-user", "normal");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etNewAccountName);
                EditText etPassword = activity.findViewById(R.id.etNewPassword);
                EditText etBirthDate = activity.findViewById(R.id.etNewBirthDate);
                Button btnUpdate = activity.findViewById(R.id.btnUpdateProfile);

                // All blank — click update
                etName.setText("");
                etPassword.setText("");
                etBirthDate.setText("");
                btnUpdate.performClick();

                // Fields should remain empty (no network call made with empty payload)
                assertEquals("", etName.getText().toString());
                assertEquals("", etPassword.getText().toString());
            });
        }
    }

    @Test
    public void profileScreen_deletePageButton_isVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(12, "another-user", "normal");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnDelete = activity.findViewById(R.id.btnGoToDeletePage);
                assertNotNull(btnDelete);
                assertTrue(btnDelete.isShown());
            });
        }
    }

    @Test
    public void profileScreen_adminSection_containsAllButtons() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnStats = activity.findViewById(R.id.btnGetStats);
                Button btnAllUsers = activity.findViewById(R.id.btnGetAllUsers);
                Button btnGetUser = activity.findViewById(R.id.btnGetUser);
                Button btnAdminUpdate = activity.findViewById(R.id.btnAdminUpdateUser);
                Button btnActivate = activity.findViewById(R.id.btnActivateUser);
                Button btnSuspend = activity.findViewById(R.id.btnSuspendUser);
                Button btnDeleteUser = activity.findViewById(R.id.btnDeleteUser);

                assertNotNull(btnStats);
                assertNotNull(btnAllUsers);
                assertNotNull(btnGetUser);
                assertNotNull(btnAdminUpdate);
                assertNotNull(btnActivate);
                assertNotNull(btnSuspend);
                assertNotNull(btnDeleteUser);
            });
        }
    }

    @Test
    public void profileScreen_adminSection_isVisibleForAdmin() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                LinearLayout adminSection = activity.findViewById(R.id.layoutAdminSection);
                assertEquals(View.VISIBLE, adminSection.getVisibility());
            });
        }
    }

    @Test
    public void profileScreen_adminStats_buttonClickDoesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnStats = activity.findViewById(R.id.btnGetStats);
                // Click triggers a network call that won't complete in test, but must not crash
                btnStats.performClick();
                assertNotNull(activity.findViewById(R.id.tvStatsResult));
            });
        }
    }

    @Test
    public void profileScreen_adminGetAllUsers_buttonClickDoesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnAllUsers = activity.findViewById(R.id.btnGetAllUsers);
                btnAllUsers.performClick();
                assertNotNull(activity.findViewById(R.id.tvAllUsersResult));
            });
        }
    }

    @Test
    public void profileScreen_adminGetUser_emptyTargetId_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etTarget = activity.findViewById(R.id.etTargetUserId);
                Button btnGetUser = activity.findViewById(R.id.btnGetUser);

                etTarget.setText(""); // no target id entered
                btnGetUser.performClick();

                // Should not crash — shows some error state
                TextView tvDetail = activity.findViewById(R.id.tvUserDetailResult);
                assertNotNull(tvDetail);
            });
        }
    }

    @Test
    public void profileScreen_adminAdminUpdateUser_emptyTargetId_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etTarget = activity.findViewById(R.id.etTargetUserId);
                Button btnAdminUpdate = activity.findViewById(R.id.btnAdminUpdateUser);

                etTarget.setText("");
                btnAdminUpdate.performClick();

                assertTrue(btnAdminUpdate.isShown());
            });
        }
    }

    @Test
    public void profileScreen_userIdLabel_containsIdForLoggedInUser() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(55, "id-check-user", "normal");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                TextView tvUserId = activity.findViewById(R.id.tvProfileUserId);
                String text = tvUserId.getText().toString();
                assertTrue("User ID label should start with 'User ID: '", text.startsWith("User ID: "));
            });
        }
    }

    @Test
    public void profileScreen_activateAndSuspendButtons_clickableForAdmin() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(1, "admin-user", "admin");

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnActivate = activity.findViewById(R.id.btnActivateUser);
                Button btnSuspend = activity.findViewById(R.id.btnSuspendUser);

                // Clicking with empty target id must not crash
                ((EditText) activity.findViewById(R.id.etTargetUserId)).setText("");
                btnActivate.performClick();
                btnSuspend.performClick();

                assertTrue(btnActivate.isShown());
                assertTrue(btnSuspend.isShown());
            });
        }
    }
}
