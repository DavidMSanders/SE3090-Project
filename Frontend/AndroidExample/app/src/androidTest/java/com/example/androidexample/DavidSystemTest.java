package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Stable UI/system coverage tests.
 */
@RunWith(AndroidJUnit4.class)
public class DavidSystemTest {

    private static final String TEST_USERNAME = "test12345";
    private static final String TEST_PASSWORD = "password12345";
    private static final int TEST_USER_ID = 37;

    // ============================================================
    // Basic landing/login/signup smoke tests
    // ============================================================

    @Test
    public void landingPageOpensLoginPage() {
        ActivityScenario.launch(LandingActivity.class);
        pause(1000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.landing_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.landing_login_btn))
                .perform(click());

        pause(1000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));


    }

    private void openMainActivityDirectly() {
        saveTestUser();

        androidx.test.core.app.ActivityScenario.launch(MainActivity.class);

        pause(1000);
    }

    @Test
    public void loginPageCanTypeCredentialsWithoutBackendLogin() {
        ActivityScenario.launch(LoginActivity.class);
        pause(1000);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.login_username_edt))
                .check(matches(withText(TEST_USERNAME)));

        onView(withId(R.id.login_password_edt))
                .check(matches(withText(TEST_PASSWORD)));
    }

    @Test
    public void landingPageOpensSignupPage() {
        ActivityScenario.launch(LandingActivity.class);
        pause(1000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.landing_signup_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_dob_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupEmptyFieldsStaysOnSignupPage() {
        ActivityScenario.launch(SignupActivity.class);
        pause(1000);

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupCanTypeIntoFields() {
        ActivityScenario.launch(SignupActivity.class);
        pause(1000);

        onView(withId(R.id.signup_username_edt))
                .perform(replaceText("coverageUser"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_dob_edt))
                .perform(replaceText("01/01/2000"), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .check(matches(withText("coverageUser")));

        onView(withId(R.id.signup_password_edt))
                .check(matches(withText("password123")));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(withText("password123")));

        onView(withId(R.id.signup_dob_edt))
                .check(matches(withText("01/01/2000")));
    }

    @Test
    public void signupLoginButtonReturnsToLoginPage() {
        ActivityScenario.launch(SignupActivity.class);
        pause(1000);

        onView(withId(R.id.signup_login_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }





    // MainActivity direct tests




    @Test
    public void mainActivityShowsImportantButtons() {
        openMainActivityDirectly();

        onView(withId(R.id.tvCurrentUser))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnGroupList))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.WorkoutList))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnPersonalBests))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnWorkoutPlans))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnGoToDelete))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }


    // Group page direct tests


    @Test
    public void groupListPageLoadsDirectly() {
        openGroupListActivityDirectly();

        onView(withId(R.id.etSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSearchGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvGroups))
                .check(matches(isDisplayed()));
    }


    // Workout list/create-workout tests
 

    @Test
    public void workoutListPageShowsCreateForm() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutDescription))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCreateWorkout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutListEmptyCreateStaysOnWorkoutListPage() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.btnCreateWorkout))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCreateWorkout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutListHideAndShowCreateForm() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.checkHideCreate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(500);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(GONE)));

        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(500);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    @Test
    public void workoutListDurationButtonsWork() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.CreateitemWorkoutDuration))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        pause(300);

        onView(withId(R.id.CreatebtnDurationMinus))
                .perform(click());

        pause(300);

        onView(withId(R.id.CreateitemWorkoutDuration))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutCreateOnlyNameStaysOnPage() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.CreateitemWorkoutName))
                .perform(replaceText("Only Name Workout"), closeSoftKeyboard());

        onView(withId(R.id.btnCreateWorkout))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutCreateOnlyDateStaysOnPage() {
        openWorkoutListActivityDirectly();

        onView(withId(R.id.CreateitemWorkoutDate))
                .perform(replaceText("2026-05-04"), closeSoftKeyboard());

        onView(withId(R.id.btnCreateWorkout))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutListCreateWorkoutFormCanSubmit() {
        openWorkoutListActivityDirectly();

        String uniqueWorkoutName = "System Test Workout " + System.currentTimeMillis();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        onView(withId(R.id.CreateitemWorkoutName))
                .perform(replaceText(uniqueWorkoutName), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDate))
                .perform(replaceText(today), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDescription))
                .perform(replaceText("Created by DavidSystemTest"), closeSoftKeyboard());

        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        pause(500);

        onView(withId(R.id.btnCreateWorkout))
                .perform(tapCenterOfView());

        pause(2000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));
    }


    // Personal Best tests


    @Test
    public void personalBestPageOpensAndShowsViews() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.etExerciseName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etBestValue))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etUnit))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAdd))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvPersonalBests))
                .check(matches(isDisplayed()));
    }

    @Test
    public void personalBestEmptyAddStaysOnPage() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.btnAdd))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAdd))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.etExerciseName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAdd))
                .check(matches(isDisplayed()));
    }

    @Test
    public void personalBestCanFillValidFields() {
        openPersonalBestActivityDirectly();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        onView(withId(R.id.etExerciseName))
                .perform(replaceText("Bench Press"), closeSoftKeyboard());

        onView(withId(R.id.etBestValue))
                .perform(replaceText("225"), closeSoftKeyboard());

        onView(withId(R.id.etUnit))
                .perform(replaceText("lbs"), closeSoftKeyboard());

        onView(withId(R.id.etDate))
                .perform(replaceText(today), closeSoftKeyboard());

        onView(withId(R.id.etExerciseName))
                .check(matches(withText("Bench Press")));

        onView(withId(R.id.etBestValue))
                .check(matches(withText("225")));

        onView(withId(R.id.etUnit))
                .check(matches(withText("lbs")));

        onView(withId(R.id.etDate))
                .check(matches(withText(today)));
    }



    @Test
    public void personalBestBackButtonClosesPage() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.btnBackPersonalBests))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackPersonalBests))
                .perform(click());

        pause(500);
    }


    // Delete user tests


    @Test
    public void deleteUserPageShowsViews() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_dob_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_user_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_userid_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_userid_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_back_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserEmptyUsernameAndDobStaysOnPage() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_user_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_user_btn))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.delete_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserCanTypeUsernameAndDob() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_username_edt))
                .perform(replaceText("coverageUser"), closeSoftKeyboard());

        onView(withId(R.id.delete_dob_edt))
                .perform(replaceText("01/01/2000"), closeSoftKeyboard());

        pause(500);

        onView(withId(R.id.delete_username_edt))
                .check(matches(withText("coverageUser")));

        onView(withId(R.id.delete_dob_edt))
                .check(matches(withText("01/01/2000")));
    }

    @Test
    public void deleteUserEmptyUserIdStaysOnPage() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_userid_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_userid_btn))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.delete_userid_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserCanTypeUserId() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_userid_edt))
                .perform(replaceText("99999"), closeSoftKeyboard());

        pause(500);

        onView(withId(R.id.delete_userid_edt))
                .check(matches(withText("99999")));
    }

    @Test
    public void deleteUserBackButtonClosesPage() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_back_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_back_btn))
                .perform(click());

        pause(500);
    }


    // Workout plan tests


    @Test
    public void workoutPlanListPageShowsViews() {
        openWorkoutPlanListActivityDirectly();

        onView(withId(R.id.btnCreateWorkoutPlan))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnOpenAssignWorkoutPage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnOpenWorkoutPlanSummaryPage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerWorkoutPlans))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutPlanListBackButtonClosesPage() {
        openWorkoutPlanListActivityDirectly();

        onView(withId(R.id.btnBackWorkoutPlanList))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackWorkoutPlanList))
                .perform(click());

        pause(500);
    }

    @Test
    public void createWorkoutPlanPageShowsViews() {
        openCreateWorkoutPlanActivityDirectly();

        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etWorkoutPlanDescription))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSubmitWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createWorkoutPlanEmptyNameStaysOnPage() {
        openCreateWorkoutPlanActivityDirectly();

        onView(withId(R.id.btnSubmitWorkoutPlan))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createWorkoutPlanCanFillFields() {
        openCreateWorkoutPlanActivityDirectly();

        String uniquePlanName = "System Plan " + System.currentTimeMillis();

        onView(withId(R.id.etWorkoutPlanName))
                .perform(replaceText(uniquePlanName), closeSoftKeyboard());

        onView(withId(R.id.etWorkoutPlanDescription))
                .perform(replaceText("Created by system test"), closeSoftKeyboard());

        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(withText(uniquePlanName)));

        onView(withId(R.id.etWorkoutPlanDescription))
                .check(matches(withText("Created by system test")));
    }

    @Test
    public void createWorkoutPlanBackButtonClosesPage() {
        openCreateWorkoutPlanActivityDirectly();

        onView(withId(R.id.btnBackCreateWorkoutPlan))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackCreateWorkoutPlan))
                .perform(click());

        pause(500);
    }

    @Test
    public void assignWorkoutPlanPageShowsViews() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.spinnerGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.spinnerClients))
                .check(matches(isDisplayed()));

        onView(withId(R.id.spinnerWorkoutPlans))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etScheduledDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAssignWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanDateFieldCanBeClicked() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.etScheduledDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etScheduledDate))
                .perform(click());

        pause(500);

        try {
            onView(withText("OK"))
                    .perform(click());
        } catch (Exception ignored) {
            // Continue if the date picker did not open in this environment.
        }

        pause(500);

        onView(withId(R.id.etScheduledDate))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanEmptySubmitStaysOnPage() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.btnAssignWorkoutPlan))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.spinnerGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAssignWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanBackButtonClosesPage() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.btnBackAssignWorkoutPlan))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackAssignWorkoutPlan))
                .perform(click());

        pause(500);
    }

    @Test
    public void workoutPlanSummaryPageShowsViews() {
        openWorkoutPlanSummaryActivityDirectly();

        onView(withId(R.id.recyclerWorkoutPlanSummaries))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutPlanSummaryBackButtonClosesPage() {
        openWorkoutPlanSummaryActivityDirectly();

        onView(withId(R.id.btnBackWorkoutPlanSummary))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackWorkoutPlanSummary))
                .perform(click());

        pause(500);
    }


    @Test
    public void createWorkoutActivityOpensAndShowsViews() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreateitemWorkoutDescription))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCreateWorkout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createWorkoutActivityEmptySubmitStaysOnPage() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        pause(1000);

        onView(withId(R.id.btnCreateWorkout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createWorkoutActivityNameOnlySubmitStaysOnPage() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.CreateitemWorkoutName))
                .perform(replaceText("Coverage Workout"), closeSoftKeyboard());

        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        pause(1000);

        onView(withId(R.id.CreateitemWorkoutName))
                .check(matches(withText("Coverage Workout")));
    }

    @Test
    public void createWorkoutActivityDateOnlySubmitStaysOnPage() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.CreateitemWorkoutDate))
                .perform(replaceText("2026-05-04"), closeSoftKeyboard());

        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        pause(1000);

        onView(withId(R.id.CreateitemWorkoutDate))
                .check(matches(withText("2026-05-04")));
    }

    @Test
    public void createWorkoutActivityDurationButtonsWork() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        onView(withId(R.id.CreatebtnDurationMinus))
                .perform(click());

        pause(500);

        onView(withId(R.id.CreateitemWorkoutDuration))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createWorkoutActivityHideAndShowCreateBox() {
        openCreateWorkoutActivityDirectly();

        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(500);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(GONE)));

        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(500);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }





    @Test
    public void personalBestActivityDirectlyShowsViews() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.etExerciseName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etBestValue))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etUnit))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAdd))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvPersonalBests))
                .check(matches(isDisplayed()));
    }

    @Test
    public void personalBestActivityCanTypeFields() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.etExerciseName))
                .perform(replaceText("Deadlift"), closeSoftKeyboard());

        onView(withId(R.id.etBestValue))
                .perform(replaceText("405"), closeSoftKeyboard());

        onView(withId(R.id.etUnit))
                .perform(replaceText("lbs"), closeSoftKeyboard());

        onView(withId(R.id.etDate))
                .perform(replaceText("2026-05-04"), closeSoftKeyboard());

        onView(withId(R.id.etExerciseName))
                .check(matches(withText("Deadlift")));

        onView(withId(R.id.etBestValue))
                .check(matches(withText("405")));

        onView(withId(R.id.etUnit))
                .check(matches(withText("lbs")));

        onView(withId(R.id.etDate))
                .check(matches(withText("2026-05-04")));
    }

    @Test
    public void personalBestActivityEmptyAddStaysOnPage() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.btnAdd))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.etExerciseName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAdd))
                .check(matches(isDisplayed()));
    }

    @Test
    public void personalBestActivityInvalidNumberStaysOnPage() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.etExerciseName))
                .perform(replaceText("Bench Press"), closeSoftKeyboard());

        onView(withId(R.id.etBestValue))
                .perform(replaceText("100"), closeSoftKeyboard());

        onView(withId(R.id.etUnit))
                .perform(replaceText("lbs"), closeSoftKeyboard());

        onView(withId(R.id.etDate))
                .perform(replaceText("2026-05-04"), closeSoftKeyboard());

        pause(500);

        onView(withId(R.id.btnAdd))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.etExerciseName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void personalBestActivityBackButtonCanBeClicked() {
        openPersonalBestActivityDirectly();

        onView(withId(R.id.btnBackPersonalBests))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackPersonalBests))
                .perform(click());

        pause(500);
    }

    @Test
    public void deleteUserActivityDirectlyShowsViews() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_dob_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_user_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_userid_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_userid_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.delete_back_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserActivityCanTypeUsernameDobAndUserId() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_username_edt))
                .perform(replaceText("coverageUser"), closeSoftKeyboard());

        onView(withId(R.id.delete_dob_edt))
                .perform(replaceText("01/01/2000"), closeSoftKeyboard());

        onView(withId(R.id.delete_userid_edt))
                .perform(replaceText("99999"), closeSoftKeyboard());

        onView(withId(R.id.delete_username_edt))
                .check(matches(withText("coverageUser")));

        onView(withId(R.id.delete_dob_edt))
                .check(matches(withText("01/01/2000")));

        onView(withId(R.id.delete_userid_edt))
                .check(matches(withText("99999")));
    }

    @Test
    public void deleteUserActivityEmptyUsernameDeleteStaysOnPage() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_user_btn))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.delete_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserActivityEmptyUserIdDeleteStaysOnPage() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_userid_btn))
                .perform(tapCenterOfView());

        pause(1000);

        onView(withId(R.id.delete_userid_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteUserActivityBackButtonCanBeClicked() {
        openDeleteUserActivityDirectly();

        onView(withId(R.id.delete_back_btn))
                .perform(click());

        pause(500);
    }

    @Test
    public void signupActivityDirectlyShowsViews() {
        openSignupActivityDirectly();

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_dob_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupActivityCanTypeAllFields() {
        openSignupActivityDirectly();

        onView(withId(R.id.signup_username_edt))
                .perform(replaceText("coverageUser"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_dob_edt))
                .perform(replaceText("01/01/2000"), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .check(matches(withText("coverageUser")));

        onView(withId(R.id.signup_password_edt))
                .check(matches(withText("password123")));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(withText("password123")));

        onView(withId(R.id.signup_dob_edt))
                .check(matches(withText("01/01/2000")));
    }

    @Test
    public void signupActivityEmptySubmitStaysOnPage() {
        openSignupActivityDirectly();

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupActivityLoginButtonCanBeClicked() {
        openSignupActivityDirectly();

        onView(withId(R.id.signup_login_btn))
                .perform(click());

        pause(1000);

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));
    }


    @Test
    public void workoutPlanListDirectlyShowsViews() {
        openWorkoutPlanListActivityDirectly();

        onView(withId(R.id.btnCreateWorkoutPlan))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnOpenAssignWorkoutPage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnOpenWorkoutPlanSummaryPage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerWorkoutPlans))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutPlanListCreateButtonOpensCreatePage() {
        openWorkoutPlanListActivityDirectly();

        onView(withId(R.id.btnCreateWorkoutPlan))
                .perform(click());

        pause(1000);

        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutPlanSummaryDirectlyShowsViews() {
        openWorkoutPlanSummaryActivityDirectly();

        onView(withId(R.id.recyclerWorkoutPlanSummaries))
                .check(matches(isDisplayed()));
    }

    @Test
    public void workoutPlanSummaryBackButtonCanBeClicked() {
        openWorkoutPlanSummaryActivityDirectly();

        onView(withId(R.id.btnBackWorkoutPlanSummary))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackWorkoutPlanSummary))
                .perform(click());

        pause(500);
    }

    @Test
    public void assignWorkoutPlanDirectlyShowsViews() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.spinnerGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.spinnerClients))
                .check(matches(isDisplayed()));

        onView(withId(R.id.spinnerWorkoutPlans))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etScheduledDate))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAssignWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanDateFieldCanBeClickedDirectly() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.etScheduledDate))
                .perform(click());

        pause(1000);

        try {
            onView(withText("OK"))
                    .perform(click());
        } catch (Exception ignored) {
        }

        pause(500);

        onView(withId(R.id.etScheduledDate))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanEmptySubmitStaysOnPageDirectly() {
        openAssignWorkoutPlanActivityDirectly();

        onView(withId(R.id.btnAssignWorkoutPlan))
                .perform(click());

        pause(1000);

        onView(withId(R.id.btnAssignWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    
    // Direct launch helpers
    //helps to launch activities, keeps me organized
    



    private void saveTestUser() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);
        keepUser.saveUser(37, "test12345", "trainer");
    }

    private void openCreateWorkoutActivityDirectly() {
        saveTestUser();
        pause(1000);

        ActivityScenario.launch(WorkoutListActivity.class);
        pause(1000);
        allowNotificationPermissionIfShown();
    }

    private void openWorkoutListActivityDirectly() {
        saveTestUser();

        ActivityScenario.launch(WorkoutListActivity.class);
        pause(2000);
        allowNotificationPermissionIfShown();
    }

    private void openGroupListActivityDirectly() {
        saveTestUser();

        ActivityScenario.launch(GroupListActivity.class);

        pause(2000);
    }

    private void openPersonalBestActivityDirectly() {
        saveTestUser();
        ActivityScenario.launch(PersonalBestActivity.class);
        pause(1000);
    }

    private void openDeleteUserActivityDirectly() {
        saveTestUser();
        ActivityScenario.launch(DeleteUserActivity.class);
        pause(1000);
    }

    private void openSignupActivityDirectly() {
        ActivityScenario.launch(SignupActivity.class);
        pause(1000);
    }

    private void openWorkoutPlanListActivityDirectly() {
        saveTestUser();
        ActivityScenario.launch(WorkoutPlanListActivity.class);
        pause(2000);
    }

    private void openWorkoutPlanSummaryActivityDirectly() {
        saveTestUser();
        ActivityScenario.launch(WorkoutPlanSummaryActivity.class);
        pause(2000);
    }

    private void openAssignWorkoutPlanActivityDirectly() {
        saveTestUser();
        ActivityScenario.launch(AssignWorkoutPlanActivity.class);
        pause(3000);
    }

    private void openCreateWorkoutPlanActivityDirectly() {
        saveTestUser();

        ActivityScenario.launch(CreateWorkoutPlanActivity.class);

        pause(1000);
    }

    
    // Utility helpers , mostly help to prevent dumb crashes
    

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
            // Permission popup was not shown.
        }
    }

    private static ViewAction tapCenterOfView() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Tap center of view directly";
            }

            @Override
            public void perform(UiController uiController, View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);

                float x = location[0] + view.getWidth() / 2f;
                float y = location[1] + view.getHeight() / 2f;

                long downTime = SystemClock.uptimeMillis();

                MotionEvent down = MotionEvent.obtain(
                        downTime,
                        downTime,
                        MotionEvent.ACTION_DOWN,
                        x,
                        y,
                        0
                );

                MotionEvent up = MotionEvent.obtain(
                        downTime,
                        downTime + 100,
                        MotionEvent.ACTION_UP,
                        x,
                        y,
                        0
                );

                view.dispatchTouchEvent(down);
                view.dispatchTouchEvent(up);

                down.recycle();
                up.recycle();

                uiController.loopMainThreadForAtLeast(300);
            }
        };
    }

    private static void pause(long millis) {
        SystemClock.sleep(millis);
    }


}
