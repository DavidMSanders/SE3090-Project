package com.example.androidexample;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;



import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import android.content.Context;
import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Frontend system tests for the group flow.
 * LandingActivity -> LoginActivity -> MainActivity -> GroupListActivity -> GroupChatActivity
 *
 */
@RunWith(AndroidJUnit4.class)
public class DavidSystemTest {

    @Rule
    public ActivityScenarioRule<LandingActivity> activityRule =
            new ActivityScenarioRule<>(LandingActivity.class);

    // CHANGE THESE to a real backend user.
    // The user needs to have at least one joined group for test 4.
    private static final String TEST_USERNAME = "test12345";
    private static final String TEST_PASSWORD = "password12345";

    /**
     * Test 1:
     * Checks that LandingActivity can open LoginActivity.
     *
     * This tests the first real page transition:
     * LandingActivity -> LoginActivity
     */
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

    /**
     * Test 2:
     * Checks that a valid login opens MainActivity.
     *
     * This tests:
     * LandingActivity -> LoginActivity -> MainActivity
     */
    @Test
    public void validLoginOpensMainPage() {
        loginFromLandingPage();

        onView(withId(R.id.tvCurrentUser))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnGroupList))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 3:
     * Checks that the user can navigate from MainActivity to GroupListActivity.
     *
     *
     * LandingActivity -> LoginActivity -> MainActivity -> GroupListActivity
     *
     * GroupListActivity GET request:
     * GET /api/v1/groups?userId={userId}
     */
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
     * Test 4:
     * Full cross-page group chat
     * LandingActivity -> LoginActivity -> MainActivity -> GroupListActivity -> GroupChatActivity
     *
     */
    @Test
    public void validLoginOpensFirstGroupChatPage() {
        loginFromLandingPage();

        onView(withId(R.id.btnGroupList))
                .perform(click());

        pause(2000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.rvGroups))
                .check(matches(isDisplayed()));

        // Wait for GroupListActivity's backend GET request to finish.
        pause(2000);

        // Click the Go To Chat button inside the first group item.
        onView(withId(R.id.rvGroups))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnGoToChat)));

        pause(2000);
        allowNotificationPermissionIfShown();

        // Verify GroupChatActivity opened.
        onView(withId(R.id.tvChatGroupTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.rvChatMessages))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etChatMessage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSendMessage))
                .check(matches(isDisplayed()));
    }

    /**
     * Helper method:
     * Starts from LandingActivity, goes to LoginActivity,
     * enters credentials, and waits for MainActivity.
     *
     */
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

        // Wait for POST /api/v1/auth/login and transition to MainActivity.
        pause(2000);

        // MainActivity is where the notification permission popup appears.
        allowNotificationPermissionIfShown();

        // Give MainActivity time to resume after the popup is handled.
        pause(2000);
    }

    /**
     * Handles Android notification permission popup if it appears.
     *
     */
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
                pause(2000);
            }
        } catch (Exception ignored) {
            // If the popup is not shown, continue normally.
        }
    }

    /**
     * Helper method:
     *
     * Used for clicking btnGoToChat inside group_box.xml.
     */
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

    /**
     * Helper method:
     * pause during Activity transitions and permission dialogs.
     */
    private static void pause(long millis) {
        SystemClock.sleep(millis);
    }

    //workout test
    @Test
    public void validLoginCreatesWorkoutAndWorkoutAppearsInList() {
        loginFromLandingPage();

        // MainActivity -> WorkoutListActivity
        onView(withId(R.id.WorkoutList))
                .perform(click());

        pause(2000);
        allowNotificationPermissionIfShown();

        // Check that the listing screen loaded.
        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));


        String uniqueWorkoutName = "System Test Workout " + System.currentTimeMillis();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        // Fill out the create workout form.
        onView(withId(R.id.CreateitemWorkoutName))
                .perform(replaceText(uniqueWorkoutName), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDate))
                .perform(replaceText(today), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDescription))
                .perform(replaceText("Created by DavidSystemTest"), closeSoftKeyboard());

        // Optional: click plus once so duration button logic is touched too.
        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        pause(500);

        // Create the workout.
        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        // Wait for POST/create request and any reload/update of the list.
        pause(2000);

        // Verify that the workout list still exists.
        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        // Verify the newly created workout appears inside the RecyclerView.
        onView(withId(R.id.recyclerWorkouts))
                .perform(
                        scrollTo(
                                hasDescendant(withText(uniqueWorkoutName))
                        )
                );
    }

    /**
     * Helper:
     * Logs in, then opens WorkoutListActivity from MainActivity.
     */
    private void openWorkoutListFromLogin() {
        loginFromLandingPage();

        onView(withId(R.id.WorkoutList))
                .perform(click());

        pause(10000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));
    }

    /**
     * Workout Test 1:
     * Login -> MainActivity -> WorkoutListActivity.
     *
     * Checks that the workout list page and create form are displayed.
     */
    @Test
    public void validLoginOpensWorkoutListPageAndShowsListing() {
        openWorkoutListFromLogin();

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

    /**
     * Workout Test 2:
     * Try creating a workout with empty fields.
     *
     * This checks that validation keeps the user on the workout list page.
     */
    @Test
    public void workoutListEmptyCreateStaysOnWorkoutListPage() {
        openWorkoutListFromLogin();

        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        pause(1000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCreateWorkout))
                .check(matches(isDisplayed()));
    }

    /**
     * Workout Test 3:
     * Hide and show the create workout form.
     *
     * This covers the checkbox visibility behavior.
     */
    @Test
    public void workoutListHideAndShowCreateForm() {
        openWorkoutListFromLogin();

        onView(withId(R.id.checkHideCreate))
                .check(matches(isDisplayed()));

        // Hide create form.
        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(1000);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(GONE)));

        // Show create form again.
        onView(withId(R.id.checkHideCreate))
                .perform(click());

        pause(1000);

        onView(withId(R.id.create_workout_box))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

    /**
     * Workout Test 4:
     * Use the duration plus and minus buttons.
     *
     * This covers the duration button click listeners.
     */
    @Test
    public void workoutListDurationPlusAndMinusButtonsWork() {
        openWorkoutListFromLogin();

        onView(withId(R.id.CreateitemWorkoutDuration))
                .check(matches(isDisplayed()));

        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        pause(500);

        onView(withId(R.id.CreatebtnDurationMinus))
                .perform(click());

        pause(500);

        onView(withId(R.id.CreateitemWorkoutDuration))
                .check(matches(isDisplayed()));
    }

    /**
     * Workout Test 5:
     * Create a workout and verify it appears in the list.
     *
     * Full flow:
     * Login -> MainActivity -> WorkoutListActivity
     * -> fill workout form -> create workout -> verify workout appears.
     */
    @Test
    public void workoutListCreateWorkoutAndVerifyAppearsInList() {
        openWorkoutListFromLogin();

        String uniqueWorkoutName = "System Test Workout " + System.currentTimeMillis();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());

        onView(withId(R.id.CreateitemWorkoutName))
                .perform(replaceText(uniqueWorkoutName), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDate))
                .perform(replaceText(today), closeSoftKeyboard());

        onView(withId(R.id.CreateitemWorkoutDescription))
                .perform(replaceText("Created by DavidSystemTest"), closeSoftKeyboard());

        // Touch duration logic too.
        onView(withId(R.id.CreatebtnDurationPlus))
                .perform(click());

        pause(500);

        onView(withId(R.id.btnCreateWorkout))
                .perform(click());

        // Wait for POST request and list refresh.
        pause(2000);

        onView(withId(R.id.recyclerWorkouts))
                .check(matches(isDisplayed()));

        // Verify the newly created workout appears somewhere in the RecyclerView.
        onView(withId(R.id.recyclerWorkouts))
                .perform(scrollTo(hasDescendant(withText(uniqueWorkoutName))));
    }




// WORKOUT PLAN / ASSIGN WORKOUT PLAN TESTS USING REAL LOGIN


    /**
     * WorkoutPlan model coverage:
     * Covers constructor, getters, and setters.
     */
    @Test
    public void methodCoverageWorkoutPlanGettersAndSettersWork() {
        WorkoutPlan plan = new WorkoutPlan(
                10,
                37,
                "Push Day",
                "Chest, shoulders, triceps",
                "2026-05-01T10:00:00"
        );

        assertEquals(10, plan.getWorkoutPlanId());
        assertEquals(37, plan.getTrainerId());
        assertEquals("Push Day", plan.getpName());
        assertEquals("Chest, shoulders, triceps", plan.getpDescription());
        assertEquals("2026-05-01T10:00:00", plan.getCreatedAt());

        plan.setWorkoutPlanId(11);
        plan.setTrainerId(38);
        plan.setpName("Pull Day");
        plan.setpDescription("Back and biceps");
        plan.setCreatedAt("2026-05-02T11:00:00");

        assertEquals(11, plan.getWorkoutPlanId());
        assertEquals(38, plan.getTrainerId());
        assertEquals("Pull Day", plan.getpName());
        assertEquals("Back and biceps", plan.getpDescription());
        assertEquals("2026-05-02T11:00:00", plan.getCreatedAt());
    }

    /**
     * WorkoutPlanAdapter simple method coverage:
     * Covers constructor and getItemCount().
     */
    @Test
    public void methodCoverageWorkoutPlanAdapterGetItemCountWorks() {
        java.util.ArrayList<WorkoutPlan> plans = new java.util.ArrayList<>();

        plans.add(new WorkoutPlan(
                1,
                37,
                "Plan A",
                "Description A",
                "2026-05-01"
        ));

        plans.add(new WorkoutPlan(
                2,
                37,
                "Plan B",
                "Description B",
                "2026-05-02"
        ));

        WorkoutPlanAdapter adapter = new WorkoutPlanAdapter(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                plans
        );

        assertEquals(2, adapter.getItemCount());

        plans.remove(0);

        assertEquals(1, adapter.getItemCount());
    }

    /**
     * CreateWorkoutPlanActivity:
     * Real login first, then launch create workout plan screen.
     *
     * Covers:
     * onCreate()
     * createWorkoutPlan()
     * empty-name validation branch
     */
    @Test
    public void createWorkoutPlanEmptyNameStaysOnCreatePlanPageAfterLogin() {
        loginFromLandingPage();

        androidx.test.core.app.ActivityScenario.launch(CreateWorkoutPlanActivity.class);

        pause(1000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etWorkoutPlanDescription))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSubmitWorkoutPlan))
                .check(matches(isDisplayed()));

        // Click submit with empty name.
        onView(withId(R.id.btnSubmitWorkoutPlan))
                .perform(click());

        pause(1000);

        // Should still be on CreateWorkoutPlanActivity.
        onView(withId(R.id.etWorkoutPlanName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSubmitWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    /**
     * CreateWorkoutPlanActivity:
     * Real login first, then create a workout plan.
     *
     * Covers:
     * form filling
     * createWorkoutPlan()
     * JSON body construction
     * POST /api/v1/workout-plans
     */
    @Test
    public void createWorkoutPlanValidSubmitAfterLogin() {
        loginFromLandingPage();

        androidx.test.core.app.ActivityScenario.launch(CreateWorkoutPlanActivity.class);

        pause(1000);
        allowNotificationPermissionIfShown();

        String uniquePlanName = "System Plan " + System.currentTimeMillis();

        onView(withId(R.id.etWorkoutPlanName))
                .perform(replaceText(uniquePlanName), closeSoftKeyboard());

        onView(withId(R.id.etWorkoutPlanDescription))
                .perform(replaceText("Created by DavidSystemTest"), closeSoftKeyboard());

        onView(withId(R.id.btnSubmitWorkoutPlan))
                .perform(click());

        // Wait for backend POST.
        // If it succeeds, the activity finishes.
        // If it fails, we still covered the create request path.
        pause(5000);
    }

    /**
     * AssignWorkoutPlanActivity:
     * Real login first, then launch assign page.
     *
     * Covers:
     * onCreate()
     * setupAdapters()
     * setupDatePicker()
     * loadAssignableGroups()
     * loadWorkoutPlans()
     */
    @Test
    public void assignWorkoutPlanPageLoadsAfterTrainerLogin() {
        loginFromLandingPage();

        androidx.test.core.app.ActivityScenario.launch(AssignWorkoutPlanActivity.class);

        pause(6000);
        allowNotificationPermissionIfShown();

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

    /**
     * AssignWorkoutPlanActivity:
     */
    @Test
    public void assignWorkoutPlanEmptySelectionValidationAfterTrainerLogin() {
        loginFromLandingPage();

        androidx.test.core.app.ActivityScenario.launch(AssignWorkoutPlanActivity.class);

        pause(3000);
        allowNotificationPermissionIfShown();

        onView(withId(R.id.btnAssignWorkoutPlan))
                .perform(click());

        pause(1000);

        // Should still be on AssignWorkoutPlanActivity.
        onView(withId(R.id.spinnerGroups))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAssignWorkoutPlan))
                .check(matches(isDisplayed()));
    }

    @Test
    public void assignWorkoutPlanPageLoadsWithSavedTrainerUser() {
        Context context = ApplicationProvider.getApplicationContext();

        KeepUser keepUser = new KeepUser(context);
        keepUser.saveUser(37, "test12345", "trainer");

        androidx.test.core.app.ActivityScenario.launch(AssignWorkoutPlanActivity.class);

        pause(3000);
        allowNotificationPermissionIfShown();

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



    // METHOD COVERAGE HELPER TESTS
    @Test
    public void methodCoverageSendMessageRequestBuildsNormalJson() throws Exception {
        SendMessageRequest request = new SendMessageRequest(
                5,
                "NORMAL",
                null,
                "hello world"
        );

        JSONObject json = request.toJson();

        assertEquals("SEND", json.getString("eventType"));
        assertEquals(5, json.getInt("groupId"));
        assertEquals("NORMAL", json.getString("messageType"));
        assertTrue(json.isNull("targetUserId"));
        assertEquals("hello world", json.getString("textBody"));
    }

    @Test
    public void methodCoverageSendMessageRequestBuildsDirectJson() throws Exception {
        SendMessageRequest request = new SendMessageRequest(
                7,
                "DIRECT",
                99,
                "private message"
        );

        JSONObject json = request.toJson();

        assertEquals("SEND", json.getString("eventType"));
        assertEquals(7, json.getInt("groupId"));
        assertEquals("DIRECT", json.getString("messageType"));
        assertEquals(99, json.getInt("targetUserId"));
        assertEquals("private message", json.getString("textBody"));
    }

    @Test
    public void methodCoverageGroupGettersAndMembershipSettersWork() {
        Group group = new Group(
                12,
                "Workout Group",
                "TrainerGroup",
                "Group for system testing",
                null,
                null
        );

        assertEquals(12, group.getGroupID());
        assertEquals("Workout Group", group.getGroupName());
        assertEquals("TrainerGroup", group.getGroupType());
        assertEquals("Group for system testing", group.getGroupDescription());
        assertNull(group.getMembershipID());
        assertNull(group.getMembershipRole());
        assertFalse(group.isMember());

        group.setMembershipID(55);
        group.setMembershipRole("MEMBER");

        assertEquals(Integer.valueOf(55), group.getMembershipID());
        assertEquals("MEMBER", group.getMembershipRole());
        assertTrue(group.isMember());
    }

    @Test
    public void methodCoverageMessageFromJsonWithAllFieldsWorks() throws Exception {
        JSONObject json = new JSONObject();
        json.put("messageId", 101);
        json.put("originatorId", 37);
        json.put("groupId", 4);
        json.put("targetUserId", JSONObject.NULL);
        json.put("username", "david");
        json.put("messageType", "NORMAL");
        json.put("textBody", "hello chat");
        json.put("createdTimestamp", "2026-05-01T10:15:30");
        json.put("editTimestamp", JSONObject.NULL);

        Message message = Message.fromJson(json);

        assertEquals(101, message.getMessageId());
        assertEquals(37, message.getOriginatorId());
        assertEquals(Integer.valueOf(4), message.getGroupId());
        assertNull(message.getTargetUserId());
        assertEquals("david", message.getUsername());
        assertEquals("NORMAL", message.getMessageType());
        assertEquals("hello chat", message.getTextBody());
        assertEquals("2026-05-01T10:15:30", message.getCreatedTimestamp());
    }

    @Test
    public void methodCoverageMessageToJsonAndSettersWork() throws Exception {
        Message message = new Message();

        message.setMessageId(202);
        message.setOriginatorId(44);
        message.setGroupId(8);
        message.setTargetUserId(99);
        message.setUsername("trainer");
        message.setMessageType("DIRECT");
        message.setTextBody("direct test");
        message.setCreatedTimestamp("2026-05-01T11:00:00");
        message.setEditTimestamp("2026-05-01T11:05:00");

        JSONObject json = message.toJson();

        assertEquals(202, json.getLong("messageId"));
        assertEquals(44, json.getInt("originatorId"));
        assertEquals(8, json.getInt("groupId"));
        assertEquals(99, json.getInt("targetUserId"));
        assertEquals("trainer", json.getString("username"));
        assertEquals("DIRECT", json.getString("messageType"));
        assertEquals("direct test", json.getString("textBody"));
        assertEquals("2026-05-01T11:00:00", json.getString("createdTimestamp"));
        assertEquals("2026-05-01T11:05:00", json.getString("editTimestamp"));

        assertTrue(message.toString().contains("direct test"));
    }

    @Test
    public void methodCoverageChatSocketEventParsesConnectedEvent() throws Exception {
        JSONObject json = new JSONObject();
        json.put("eventType", "CONNECTED");
        json.put("userId", 37);
        json.put("username", "david");
        json.put("role", "trainer");

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("CONNECTED", event.getEventType());
        assertEquals(Integer.valueOf(37), event.getUserId());
        assertEquals("david", event.getUsername());
        assertEquals("trainer", event.getRole());
    }

    @Test
    public void methodCoverageChatSocketEventParsesErrorMessageString() throws Exception {
        JSONObject json = new JSONObject();
        json.put("eventType", "ERROR");
        json.put("message", "You cannot send messages");

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("ERROR", event.getEventType());
        assertEquals("You cannot send messages", event.getErrorMessage());
    }

    @Test
    public void methodCoverageChatSocketEventParsesPermissionUpdateFields() throws Exception {
        JSONObject json = new JSONObject();
        json.put("eventType", "GROUP_PERMISSION_UPDATED");
        json.put("groupId", 4);
        json.put("targetUserId", 37);
        json.put("membershipRole", "GROUP_LEADER");
        json.put("canSendMessages", true);
        json.put("canManageMembers", true);
        json.put("canPostAnnouncements", false);
        json.put("canAssignWorkouts", true);

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("GROUP_PERMISSION_UPDATED", event.getEventType());
        assertEquals(Integer.valueOf(4), event.getGroupId());
        assertEquals(Integer.valueOf(37), event.getTargetUserId());
        assertEquals("GROUP_LEADER", event.getMembershipRole());
        assertTrue(event.isCanSendMessages());
        assertTrue(event.isCanManageMembers());
        assertFalse(event.isCanPostAnnouncements());
        assertTrue(event.isCanAssignWorkouts());
    }

    @Test
    public void methodCoverageKeepUserSaveGetAndClearWork() {
        Context context = ApplicationProvider.getApplicationContext();
        KeepUser keepUser = new KeepUser(context);

        keepUser.clearUser();

        assertFalse(keepUser.isLoggedIn());
        assertEquals(-1, keepUser.getUserId());
        assertEquals("", keepUser.getUserIdString());
        assertEquals("Guest", keepUser.getDisplayUsername());
        assertEquals("normal", keepUser.getRoleOrDefault());

        keepUser.saveUser(37, "david", "trainer");

        assertTrue(keepUser.isLoggedIn());
        assertEquals(37, keepUser.getUserId());
        assertEquals("37", keepUser.getUserIdString());
        assertEquals("david", keepUser.getUsername());
        assertEquals("david", keepUser.getDisplayUsername());
        assertEquals("trainer", keepUser.getRole());
        assertEquals("trainer", keepUser.getRoleOrDefault());

        keepUser.clearUser();

        assertFalse(keepUser.isLoggedIn());
    }

    @Test
    public void methodCoverageMessageAdapterItemTypesWork() {
        ArrayList<Message> messages = new ArrayList<>();

        messages.add(new Message(
                1, 37, 10, null, "me", "NORMAL",
                "my normal message", "2026-05-01T10:00:00", null
        ));

        messages.add(new Message(
                2, 88, 10, null, "other", "NORMAL",
                "other normal message", "2026-05-01T10:01:00", null
        ));

        messages.add(new Message(
                3, 88, 10, null, "leader", "ANNOUNCEMENT",
                "announcement text", "2026-05-01T10:02:00", null
        ));

        messages.add(new Message(
                4, 88, 10, 37, "other", "DIRECT",
                "direct to me", "2026-05-01T10:03:00", null
        ));

        messages.add(new Message(
                5, 88, 10, null, "trainer", "TRAINER",
                "trainer message", "2026-05-01T10:04:00", null
        ));

        MessageAdapter adapter = new MessageAdapter(
                messages,
                37,
                true,
                new MessageAdapter.OnMessageMenuActionListener() {
                    @Override
                    public void onDirectMessage(Message message) {}

                    @Override
                    public void onEditPermissions(Message message) {}

                    @Override
                    public void onEditMessage(Message message) {}

                    @Override
                    public void onDeleteMessage(Message message) {}
                }
        );

        assertEquals(5, adapter.getItemCount());
        assertEquals(MessageAdapter.Type_Our_Message, adapter.getItemViewType(0));
        assertEquals(MessageAdapter.Type_Other_Message, adapter.getItemViewType(1));
        assertEquals(MessageAdapter.Type_Announcement_Message, adapter.getItemViewType(2));
        assertEquals(MessageAdapter.Type_Direct_Message, adapter.getItemViewType(3));
        assertEquals(MessageAdapter.Type_Trainer_Message, adapter.getItemViewType(4));
    }

    @Test
    public void methodCoverageMessageAdapterAddUpdateAndRemoveWork() {
        ArrayList<Message> messages = new ArrayList<>();

        MessageAdapter adapter = new MessageAdapter(
                messages,
                37,
                false,
                new MessageAdapter.OnMessageMenuActionListener() {
                    @Override
                    public void onDirectMessage(Message message) {}

                    @Override
                    public void onEditPermissions(Message message) {}

                    @Override
                    public void onEditMessage(Message message) {}

                    @Override
                    public void onDeleteMessage(Message message) {}
                }
        );

        Message original = new Message(
                10, 37, 5, null, "david", "NORMAL",
                "old text", "2026-05-01T12:00:00", null
        );

        adapter.addMessage(original);
        assertEquals(1, adapter.getItemCount());
        assertEquals("old text", messages.get(0).getTextBody());

        Message updated = new Message(
                10, 37, 5, null, "david", "NORMAL",
                "new text", "2026-05-01T12:00:00", null
        );

        adapter.updateMessage(updated);
        assertEquals("new text", messages.get(0).getTextBody());

        adapter.removeMessageById(10);
        assertEquals(0, adapter.getItemCount());

        adapter.removeMessageById(999);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void workoutPlanGettersAndSettersWork() {
        WorkoutPlan plan = new WorkoutPlan(
                1,
                37,
                "Push Day",
                "Chest and triceps",
                "2026-05-01"
        );

        assertEquals(1, plan.getWorkoutPlanId());
        assertEquals(37, plan.getTrainerId());
        assertEquals("Push Day", plan.getpName());
        assertEquals("Chest and triceps", plan.getpDescription());
        assertEquals("2026-05-01", plan.getCreatedAt());

        plan.setWorkoutPlanId(2);
        plan.setTrainerId(99);
        plan.setpName("Pull Day");
        plan.setpDescription("Back and biceps");
        plan.setCreatedAt("2026-05-02");

        assertEquals(2, plan.getWorkoutPlanId());
        assertEquals(99, plan.getTrainerId());
        assertEquals("Pull Day", plan.getpName());
        assertEquals("Back and biceps", plan.getpDescription());
        assertEquals("2026-05-02", plan.getCreatedAt());
    }

    // ============================================================
    // Group trivial method coverage
    // Covers getters, setters, and isMember().
    // ============================================================

    @Test
    public void groupGettersSettersAndIsMemberWork() {
        Group group = new Group(
                10,
                "Trainer Group",
                "TrainerGroup",
                "A group for testing",
                null,
                null
        );

        assertEquals(10, group.getGroupID());
        assertEquals("Trainer Group", group.getGroupName());
        assertEquals("TrainerGroup", group.getGroupType());
        assertEquals("A group for testing", group.getGroupDescription());
        assertNull(group.getMembershipID());
        assertNull(group.getMembershipRole());
        assertFalse(group.isMember());

        group.setMembershipID(123);
        group.setMembershipRole("MEMBER");

        assertEquals(Integer.valueOf(123), group.getMembershipID());
        assertEquals("MEMBER", group.getMembershipRole());
        assertTrue(group.isMember());
    }


    // SendMessageRequest trivial method coverage
    // Covers constructor and toJson().


    @Test
    public void sendMessageRequestNormalJsonWorks() throws Exception {
        SendMessageRequest request = new SendMessageRequest(
                5,
                "NORMAL",
                null,
                "hello"
        );

        JSONObject json = request.toJson();

        assertEquals("SEND", json.getString("eventType"));
        assertEquals(5, json.getInt("groupId"));
        assertEquals("NORMAL", json.getString("messageType"));
        assertTrue(json.isNull("targetUserId"));
        assertEquals("hello", json.getString("textBody"));
    }

    @Test
    public void sendMessageRequestDirectJsonWorks() throws Exception {
        SendMessageRequest request = new SendMessageRequest(
                5,
                "DIRECT",
                99,
                "private hello"
        );

        JSONObject json = request.toJson();

        assertEquals("SEND", json.getString("eventType"));
        assertEquals(5, json.getInt("groupId"));
        assertEquals("DIRECT", json.getString("messageType"));
        assertEquals(99, json.getInt("targetUserId"));
        assertEquals("private hello", json.getString("textBody"));
    }

    @Test
    public void sendMessageRequestNullDefaultsWork() throws Exception {
        SendMessageRequest request = new SendMessageRequest(
                5,
                null,
                null,
                null
        );

        JSONObject json = request.toJson();

        assertEquals("SEND", json.getString("eventType"));
        assertEquals(5, json.getInt("groupId"));
        assertEquals("NORMAL", json.getString("messageType"));
        assertTrue(json.isNull("targetUserId"));
        assertEquals("", json.getString("textBody"));
    }


    // Message trivial method coverage
    //constructor, getters, setters, fromJson(), toJson().


    @Test
    public void messageConstructorAndGettersWork() {
        Message message = new Message(
                1,
                37,
                10,
                null,
                "david",
                "NORMAL",
                "hello chat",
                "2026-05-01T10:00:00",
                null
        );

        assertEquals(1, message.getMessageId());
        assertEquals(37, message.getOriginatorId());
        assertEquals(Integer.valueOf(10), message.getGroupId());
        assertNull(message.getTargetUserId());
        assertEquals("david", message.getUsername());
        assertEquals("NORMAL", message.getMessageType());
        assertEquals("hello chat", message.getTextBody());
        assertEquals("2026-05-01T10:00:00", message.getCreatedTimestamp());
    }

    @Test
    public void messageSettersAndToJsonWork() throws Exception {
        Message message = new Message();

        message.setMessageId(2);
        message.setOriginatorId(44);
        message.setGroupId(11);
        message.setTargetUserId(99);
        message.setUsername("trainer");
        message.setMessageType("DIRECT");
        message.setTextBody("direct message");
        message.setCreatedTimestamp("2026-05-01T11:00:00");
        message.setEditTimestamp("2026-05-01T11:05:00");

        JSONObject json = message.toJson();

        assertEquals(2, json.getLong("messageId"));
        assertEquals(44, json.getInt("originatorId"));
        assertEquals(11, json.getInt("groupId"));
        assertEquals(99, json.getInt("targetUserId"));
        assertEquals("trainer", json.getString("username"));
        assertEquals("DIRECT", json.getString("messageType"));
        assertEquals("direct message", json.getString("textBody"));
        assertEquals("2026-05-01T11:00:00", json.getString("createdTimestamp"));
        assertEquals("2026-05-01T11:05:00", json.getString("editTimestamp"));
    }

    @Test
    public void messageFromJsonWorks() throws Exception {
        JSONObject json = new JSONObject();

        json.put("messageId", 3);
        json.put("originatorId", 37);
        json.put("groupId", 10);
        json.put("targetUserId", JSONObject.NULL);
        json.put("username", "david");
        json.put("messageType", "ANNOUNCEMENT");
        json.put("textBody", "announcement text");
        json.put("createdTimestamp", "2026-05-01T12:00:00");
        json.put("editTimestamp", JSONObject.NULL);

        Message message = Message.fromJson(json);

        assertEquals(3, message.getMessageId());
        assertEquals(37, message.getOriginatorId());
        assertEquals(Integer.valueOf(10), message.getGroupId());
        assertNull(message.getTargetUserId());
        assertEquals("david", message.getUsername());
        assertEquals("ANNOUNCEMENT", message.getMessageType());
        assertEquals("announcement text", message.getTextBody());
        assertEquals("2026-05-01T12:00:00", message.getCreatedTimestamp());
    }


    // ChatSocketEvent trivial method coverage
    // Covers setters, getters, boolean helper methods, and fromJson().


    @Test
    public void chatSocketEventSettersAndGettersWork() {
        ChatSocketEvent event = new ChatSocketEvent();

        Message message = new Message(
                1,
                37,
                10,
                null,
                "david",
                "NORMAL",
                "hello",
                "2026-05-01T10:00:00",
                null
        );

        event.setEventType("MESSAGE_CREATED");
        event.setMessage(message);
        event.setMessageId(1L);
        event.setGroupId(10);
        event.setTargetUserId(99);
        event.setMembershipRole("GROUP_LEADER");
        event.setCanSendMessages(true);
        event.setCanManageMembers(false);
        event.setCanPostAnnouncements(true);
        event.setCanAssignWorkouts(false);

        assertEquals("MESSAGE_CREATED", event.getEventType());
        assertEquals(message, event.getMessage());
        assertEquals(Long.valueOf(1L), event.getMessageId());
        assertEquals(Integer.valueOf(10), event.getGroupId());
        assertEquals(Integer.valueOf(99), event.getTargetUserId());
        assertEquals("GROUP_LEADER", event.getMembershipRole());
        assertTrue(event.isCanSendMessages());
        assertFalse(event.isCanManageMembers());
        assertTrue(event.isCanPostAnnouncements());
        assertFalse(event.isCanAssignWorkouts());
    }

    @Test
    public void chatSocketEventFromJsonConnectedWorks() throws Exception {
        JSONObject json = new JSONObject();

        json.put("eventType", "CONNECTED");
        json.put("userId", 37);
        json.put("username", "david");
        json.put("role", "trainer");

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("CONNECTED", event.getEventType());
        assertEquals(Integer.valueOf(37), event.getUserId());
        assertEquals("david", event.getUsername());
        assertEquals("trainer", event.getRole());
    }

    @Test
    public void chatSocketEventFromJsonErrorWorks() throws Exception {
        JSONObject json = new JSONObject();

        json.put("eventType", "ERROR");
        json.put("message", "Cannot send message");

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("ERROR", event.getEventType());
        assertEquals("Cannot send message", event.getErrorMessage());
    }

    @Test
    public void chatSocketEventFromJsonPermissionUpdateWorks() throws Exception {
        JSONObject json = new JSONObject();

        json.put("eventType", "GROUP_PERMISSION_UPDATED");
        json.put("groupId", 10);
        json.put("targetUserId", 37);
        json.put("membershipRole", "MEMBER");
        json.put("canSendMessages", true);
        json.put("canManageMembers", true);
        json.put("canPostAnnouncements", false);
        json.put("canAssignWorkouts", true);

        ChatSocketEvent event = ChatSocketEvent.fromJson(json);

        assertEquals("GROUP_PERMISSION_UPDATED", event.getEventType());
        assertEquals(Integer.valueOf(10), event.getGroupId());
        assertEquals(Integer.valueOf(37), event.getTargetUserId());
        assertEquals("MEMBER", event.getMembershipRole());
        assertTrue(event.isCanSendMessages());
        assertTrue(event.isCanManageMembers());
        assertFalse(event.isCanPostAnnouncements());
        assertTrue(event.isCanAssignWorkouts());
    }

    @Test
    public void chatSocketEventFromJsonMessageObjectWorks() throws Exception {
        JSONObject messageJson = new JSONObject();

        messageJson.put("messageId", 5);
        messageJson.put("originatorId", 37);
        messageJson.put("groupId", 10);
        messageJson.put("targetUserId", JSONObject.NULL);
        messageJson.put("username", "david");
        messageJson.put("messageType", "NORMAL");
        messageJson.put("textBody", "created message");
        messageJson.put("createdTimestamp", "2026-05-01T13:00:00");
        messageJson.put("editTimestamp", JSONObject.NULL);

        JSONObject eventJson = new JSONObject();

        eventJson.put("eventType", "MESSAGE_CREATED");
        eventJson.put("message", messageJson);
        eventJson.put("messageId", 5);
        eventJson.put("groupId", 10);

        ChatSocketEvent event = ChatSocketEvent.fromJson(eventJson);

        assertEquals("MESSAGE_CREATED", event.getEventType());
        assertEquals(Long.valueOf(5L), event.getMessageId());
        assertEquals(Integer.valueOf(10), event.getGroupId());
        assertEquals("created message", event.getMessage().getTextBody());
    }

}