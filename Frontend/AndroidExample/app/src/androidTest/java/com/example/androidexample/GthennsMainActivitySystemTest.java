package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class GthennsMainActivitySystemTest {

    private void seedUser(int userId, String username, String role) {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).clearUser();
        SessionManager.clearSession(context);
        new KeepUser(context).saveUser(userId, username, role);
    }

    @Test
    public void mainActivity_showsCoreButtonsAndTitle() {
        seedUser(21, "main-user", "normal");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.textView);
                Button groupList = activity.findViewById(R.id.btnGroupList);
                Button workoutList = activity.findViewById(R.id.WorkoutList);
                Button personalBests = activity.findViewById(R.id.btnPersonalBests);
                Button calendar = activity.findViewById(R.id.btnGoToCalendar);
                Button createWorkout = activity.findViewById(R.id.btnGoToWorkout);
                Button testingPage = activity.findViewById(R.id.btnTestingPage);

                assertTrue(title.getText().toString().contains("Personal Training Suite"));
                assertTrue(groupList.isShown());
                assertTrue(workoutList.isShown());
                assertTrue(personalBests.isShown());
                assertTrue(calendar.isShown());
                assertTrue(createWorkout.isShown());
                assertTrue(testingPage.isShown());
            });
        }
    }

    @Test
    public void mainActivity_hidesWorkoutPlansForNormalUser() {
        seedUser(22, "normal-main", "normal");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Button workoutPlans = activity.findViewById(R.id.btnWorkoutPlans);
                assertEquals(View.GONE, workoutPlans.getVisibility());
            });
        }
    }

    @Test
    public void mainActivity_showsWorkoutPlansForTrainer() {
        seedUser(23, "trainer-main", "trainer");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Button workoutPlans = activity.findViewById(R.id.btnWorkoutPlans);
                assertEquals(View.VISIBLE, workoutPlans.getVisibility());
                assertTrue(workoutPlans.isShown());
            });
        }
    }

    @Test
    public void mainActivity_topBarHidesBackButtonAndShowsControls() {
        seedUser(24, "topbar-main", "normal");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Button backButton = activity.findViewById(R.id.btnTopBack);
                Button homeButton = activity.findViewById(R.id.btnTopHome);
                ImageButton notificationButton = activity.findViewById(R.id.btnTopNotification);
                ImageButton userMenuButton = activity.findViewById(R.id.btnTopUserMenu);

                assertEquals(View.GONE, backButton.getVisibility());
                assertTrue(homeButton.isShown());
                assertNotNull(notificationButton);
                assertNotNull(userMenuButton);
                assertEquals("Notifications", String.valueOf(notificationButton.getContentDescription()));
                assertEquals("Profile menu", String.valueOf(userMenuButton.getContentDescription()));
            });
        }
    }

    @Test
    public void mainActivity_notificationDotReflectsUnreadStateOnResume() {
        Context context = ApplicationProvider.getApplicationContext();
        seedUser(25, "notify-main", "normal");
        AppNotificationManager.replaceNotifications(context, new ArrayList<>());
        AppNotificationManager.setHasUnreadNotifications(context, true);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View notificationDot = activity.findViewById(R.id.viewTopNotificationDot);
                assertEquals(View.VISIBLE, notificationDot.getVisibility());

                AppNotificationManager.markNotificationsRead(activity);
                activity.onResume();

                assertEquals(View.GONE, notificationDot.getVisibility());
            });
        }
    }
}