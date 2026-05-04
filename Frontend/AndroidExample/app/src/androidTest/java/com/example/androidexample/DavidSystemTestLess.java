package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DavidSystemTestLess {

    @Rule
    public ActivityScenarioRule<LandingActivity> activityRule =
            new ActivityScenarioRule<>(LandingActivity.class);

    private static final String TEST_USERNAME = "test12345";
    private static final String TEST_PASSWORD = "password12345";


    @Test
    public void landingPageOpensLoginPage() {
        allowNotificationPermissionIfShown();

        onView(withId(R.id.landing_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.landing_login_btn))
                .perform(click());

        allowNotificationPermissionIfShown();

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void validLoginOpensMainPage() {
        loginFromLandingPage();

        onView(withId(R.id.tvCurrentUser))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnGroupList))
                .check(matches(isDisplayed()));
    }


    @Test
    public void validLoginOpensGroupListPage() {
        loginFromLandingPage();

        onView(withId(R.id.btnGroupList))
                .perform(click());

        pause(3000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.etSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvGroups))
                .check(matches(isDisplayed()));
    }

    /**
     * System Test 4:
     * LandingActivity -> LoginActivity -> MainActivity -> GroupListActivity -> GroupChatActivity
     */
    @Test
    public void validLoginOpensFirstGroupChatPage() {
        loginFromLandingPage();

        onView(withId(R.id.btnGroupList))
                .perform(click());

        pause(3000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.rvGroups))
                .check(matches(isDisplayed()));

        pause(3000);

        onView(withId(R.id.rvGroups))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnGoToChat)));

        pause(3000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.tvChatGroupTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvChatMessages))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etChatMessage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSendMessage))
                .check(matches(isDisplayed()));
    }


    private void loginFromLandingPage() {
        allowNotificationPermissionIfShown();

        onView(withId(R.id.landing_login_btn))
                .perform(click());

        pause(2000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.login_username_edt))
                .perform(replaceText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .perform(click());

        pause(4000);
        allowNotificationPermissionIfShown();

        pause(1000);
    }


    private void allowNotificationPermissionIfShown() {
        try {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation()
            );

            UiObject allowButton = device.findObject(
                    new UiSelector().textMatches("(?i)Allow")
            );

            if (allowButton.exists()) {
                allowButton.click();
                pause(1000);
            }
        } catch (Exception ignored) {
            // Popup was not shown.
        }
    }


    private static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click child view with id: " + id;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);

                if (childView != null) {
                    childView.performClick();
                }
            }
        };
    }


    private static void pause(long millis) {
        SystemClock.sleep(millis);
    }
}