package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class DavidSystemTestLess {

    private static final int TEST_USER_ID = 37;
    private static final String TEST_USERNAME = "test12345";
    private static final String TEST_ROLE = "trainer";

    private void saveTestUser() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.saveUser(TEST_USER_ID, TEST_USERNAME, TEST_ROLE);
    }

    private static void pause(long millis) {
        SystemClock.sleep(millis);
    }

    @Test
    public void landingPageOpensLoginPage() {
        ActivityScenario<LandingActivity> scenario = ActivityScenario.launch(LandingActivity.class);

        onView(withId(R.id.landing_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.landing_login_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void mainPageOpensDirectlyWithSavedUser() {
        saveTestUser();

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        pause(1000);

        onView(withId(R.id.tvCurrentUser))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnGroupList))
                .check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void groupListPageOpensDirectlyWithSavedUser() {
        saveTestUser();

        ActivityScenario<GroupListActivity> scenario = ActivityScenario.launch(GroupListActivity.class);

        pause(2000);

        onView(withId(R.id.etSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvGroups))
                .check(matches(isDisplayed()));

        scenario.close();
    }
}
