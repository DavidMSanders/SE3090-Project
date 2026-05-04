package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsTestingActivitySystemTest {

    @Before
    public void clearUser() {
        new KeepUser(ApplicationProvider.getApplicationContext()).clearUser();
    }

    @Test
    public void testingActivity_allRoleButtonsAreVisible() {
        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnTrainer = activity.findViewById(R.id.btnSetTrainer);
                Button btnAdmin = activity.findViewById(R.id.btnSetAdmin);
                Button btnClient = activity.findViewById(R.id.btnSetClient);

                assertNotNull(btnTrainer);
                assertNotNull(btnAdmin);
                assertNotNull(btnClient);

                assertTrue(btnTrainer.isShown());
                assertTrue(btnAdmin.isShown());
                assertTrue(btnClient.isShown());
            });
        }
    }

    @Test
    public void testingActivity_resultTextViewIsVisible() {
        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                TextView tvResult = activity.findViewById(R.id.tvResult);
                assertNotNull(tvResult);
                assertTrue(tvResult.isShown());
            });
        }
    }

    @Test
    public void testingActivity_setTrainer_withNoLoggedInUser_showsError() {
        Context context = ApplicationProvider.getApplicationContext();
        // Clear user so getUserId() returns -1
        new KeepUser(context).clearUser();

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnTrainer = activity.findViewById(R.id.btnSetTrainer);
                TextView tvResult = activity.findViewById(R.id.tvResult);

                btnTrainer.performClick();

                assertEquals("No logged in user found", tvResult.getText().toString());
            });
        }
    }

    @Test
    public void testingActivity_setAdmin_withNoLoggedInUser_showsError() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnAdmin = activity.findViewById(R.id.btnSetAdmin);
                TextView tvResult = activity.findViewById(R.id.tvResult);

                btnAdmin.performClick();

                assertEquals("No logged in user found", tvResult.getText().toString());
            });
        }
    }

    @Test
    public void testingActivity_setClient_withNoLoggedInUser_showsError() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnClient = activity.findViewById(R.id.btnSetClient);
                TextView tvResult = activity.findViewById(R.id.tvResult);

                btnClient.performClick();

                assertEquals("No logged in user found", tvResult.getText().toString());
            });
        }
    }

    @Test
    public void testingActivity_setTrainer_withLoggedInUser_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(43, "test-trainer", "normal");

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnTrainer = activity.findViewById(R.id.btnSetTrainer);
                // Network call will fail but must not crash
                btnTrainer.performClick();

                TextView tvResult = activity.findViewById(R.id.tvResult);
                assertNotNull(tvResult);
            });
        }
    }

    @Test
    public void testingActivity_setAdmin_withLoggedInUser_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(43, "test-admin-candidate", "normal");

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnAdmin = activity.findViewById(R.id.btnSetAdmin);
                btnAdmin.performClick();

                TextView tvResult = activity.findViewById(R.id.tvResult);
                assertNotNull(tvResult);
            });
        }
    }

    @Test
    public void testingActivity_setClient_withLoggedInUser_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(43, "test-client-candidate", "normal");

        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnClient = activity.findViewById(R.id.btnSetClient);
                btnClient.performClick();

                TextView tvResult = activity.findViewById(R.id.tvResult);
                assertNotNull(tvResult);
            });
        }
    }

    @Test
    public void testingActivity_trainerButtonLabel_isCorrect() {
        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnTrainer = activity.findViewById(R.id.btnSetTrainer);
                assertTrue("Trainer button text should reference trainer",
                        btnTrainer.getText().toString().toLowerCase().contains("trainer"));
            });
        }
    }

    @Test
    public void testingActivity_adminButtonLabel_isCorrect() {
        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnAdmin = activity.findViewById(R.id.btnSetAdmin);
                assertTrue("Admin button text should reference admin",
                        btnAdmin.getText().toString().toLowerCase().contains("admin"));
            });
        }
    }

    @Test
    public void testingActivity_clientButtonLabel_isCorrect() {
        try (ActivityScenario<TestingActivity> scenario = ActivityScenario.launch(TestingActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnClient = activity.findViewById(R.id.btnSetClient);
                assertTrue("Client button text should reference client",
                        btnClient.getText().toString().toLowerCase().contains("client"));
            });
        }
    }
}
