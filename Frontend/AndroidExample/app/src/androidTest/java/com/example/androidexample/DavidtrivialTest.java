package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TRIVIAL METHOD COVERAGE TESTS
 */
@RunWith(AndroidJUnit4.class)
public class DavidtrivialTest {


    // WorkoutPlan coverage

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

    @Test
    public void workoutPlanAdapterGetItemCountWorks() {
        ArrayList<WorkoutPlan> plans = new ArrayList<>();

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
                ApplicationProvider.getApplicationContext(),
                plans
        );

        assertEquals(2, adapter.getItemCount());

        plans.remove(0);

        assertEquals(1, adapter.getItemCount());
    }


    // Group coverage


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

    // SendMessageRequest coverage

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

    // Message coverage


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

    //ChatSocketEvent coverage

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


    // KeepUser coverage


    @Test
    public void keepUserSaveGetAndClearWork() {
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

    
    // MessageAdapter coverage
    

    @Test
    public void messageAdapterItemTypesWork() {
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
    public void messageAdapterAddUpdateAndRemoveWork() {
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
    //workoutPlan coverage
    @Test
    public void workoutPlanConstructorValuesCorrect() {
        WorkoutPlan plan = new WorkoutPlan();

        plan.setWorkoutPlanId(12);
        plan.setTrainerId(5);
        plan.setpName("Leg Day");
        plan.setpDescription("Squats and lunges");
        plan.setCreatedAt("2026-05-04");

        assertEquals(12, plan.getWorkoutPlanId());
        assertEquals(5, plan.getTrainerId());
        assertEquals("Leg Day", plan.getpName());
        assertEquals("Squats and lunges", plan.getpDescription());
        assertEquals("2026-05-04", plan.getCreatedAt());
    }

    @Test
    public void workoutPlanSetsValuesCorrectly() {
        WorkoutPlan plan = new WorkoutPlan(
                7,
                22,
                "Push Day",
                "Bench press and shoulders",
                "2026-05-04"
        );

        assertEquals(7, plan.getWorkoutPlanId());
        assertEquals(22, plan.getTrainerId());
        assertEquals("Push Day", plan.getpName());
        assertEquals("Bench press and shoulders", plan.getpDescription());
        assertEquals("2026-05-04", plan.getCreatedAt());
    }

    @Test
    public void workoutPlanOverwriteOldValues() {
        WorkoutPlan plan = new WorkoutPlan();

        plan.setWorkoutPlanId(1);
        plan.setTrainerId(2);
        plan.setpName("Old Name");
        plan.setpDescription("Old Description");
        plan.setCreatedAt("2026-01-01");

        plan.setWorkoutPlanId(10);
        plan.setTrainerId(20);
        plan.setpName("New Name");
        plan.setpDescription("New Description");
        plan.setCreatedAt("2026-12-31");

        assertEquals(10, plan.getWorkoutPlanId());
        assertEquals(20, plan.getTrainerId());
        assertEquals("New Name", plan.getpName());
        assertEquals("New Description", plan.getpDescription());
        assertEquals("2026-12-31", plan.getCreatedAt());
    }

    //workoutplan summary
    @Test
    public void workoutPlanSummary_defaultListsAreNotNull() {
        WorkoutPlanSummary summary = new WorkoutPlanSummary();

        assertNotNull(summary.getAcceptedUsers());
        assertNotNull(summary.getRejectedUsers());
        assertNotNull(summary.getCompletedUsers());
        assertNotNull(summary.getPendingUsers());

        assertEquals(0, summary.getAcceptedUsers().size());
        assertEquals(0, summary.getRejectedUsers().size());
        assertEquals(0, summary.getCompletedUsers().size());
        assertEquals(0, summary.getPendingUsers().size());
    }

    @Test
    public void workoutPlanSummary_setsAndGetsCountsCorrectly() {
        WorkoutPlanSummary summary = new WorkoutPlanSummary();

        summary.setPlanId(3);
        summary.setPlanName("Chest Plan");
        summary.setAcceptedCount(4);
        summary.setRejectedCount(1);
        summary.setCompletedCount(2);
        summary.setPendingCount(5);

        assertEquals(3, summary.getPlanId());
        assertEquals("Chest Plan", summary.getPlanName());
        assertEquals(4, summary.getAcceptedCount());
        assertEquals(1, summary.getRejectedCount());
        assertEquals(2, summary.getCompletedCount());
        assertEquals(5, summary.getPendingCount());
    }

    @Test
    public void workoutPlanSummary_setsAndGetsUserListsCorrectly() {
        WorkoutPlanSummary summary = new WorkoutPlanSummary();

        List<String> accepted = Arrays.asList("David", "Chris");
        List<String> rejected = Arrays.asList("Alex");
        List<String> completed = Arrays.asList("Sam", "Jordan");
        List<String> pending = Arrays.asList("Taylor");

        summary.setAcceptedUsers(accepted);
        summary.setRejectedUsers(rejected);
        summary.setCompletedUsers(completed);
        summary.setPendingUsers(pending);

        assertEquals(accepted, summary.getAcceptedUsers());
        assertEquals(rejected, summary.getRejectedUsers());
        assertEquals(completed, summary.getCompletedUsers());
        assertEquals(pending, summary.getPendingUsers());
    }

    @Test
    public void workoutPlanSummary_valuesCanBeOverwritten() {
        WorkoutPlanSummary summary = new WorkoutPlanSummary();

        summary.setPlanId(1);
        summary.setPlanName("Old Plan");
        summary.setAcceptedCount(1);

        summary.setPlanId(99);
        summary.setPlanName("Updated Plan");
        summary.setAcceptedCount(10);

        assertEquals(99, summary.getPlanId());
        assertEquals("Updated Plan", summary.getPlanName());
        assertEquals(10, summary.getAcceptedCount());
    }

    //personalBest coverage

    @Test
    public void personalBest_setsAndGetsValuesCorrectly() {
        PersonalBest personalBest = new PersonalBest();

        personalBest.setId(1);
        personalBest.setUserId(37);
        personalBest.setExerciseName("Bench Press");
        personalBest.setBestValue(225.0);
        personalBest.setUnit("lbs");
        personalBest.setRecordDate("2026-05-04");

        assertEquals(1, personalBest.getId());
        assertEquals(37, personalBest.getUserId());
        assertEquals("Bench Press", personalBest.getExerciseName());
        assertEquals(225.0, personalBest.getBestValue(), 0.001);
        assertEquals("lbs", personalBest.getUnit());
        assertEquals("2026-05-04", personalBest.getRecordDate());
    }

    @Test
    public void personalBest_valuesCanBeOverwritten() {
        PersonalBest personalBest = new PersonalBest();

        personalBest.setExerciseName("Old Exercise");
        personalBest.setBestValue(100.0);
        personalBest.setUnit("lbs");
        personalBest.setRecordDate("2026-01-01");

        personalBest.setExerciseName("Squat");
        personalBest.setBestValue(315.0);
        personalBest.setUnit("lbs");
        personalBest.setRecordDate("2026-06-01");

        assertEquals("Squat", personalBest.getExerciseName());
        assertEquals(315.0, personalBest.getBestValue(), 0.001);
        assertEquals("lbs", personalBest.getUnit());
        assertEquals("2026-06-01", personalBest.getRecordDate());
    }



    @Test
    public void workoutConstructorAndGettersWork() {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Bench Day",
                "Chest and triceps",
                45,
                "2026-05-04",
                "planned",
                "self_created"
        );

        assertEquals(1, workout.getWorkoutId());
        assertEquals(37, workout.getUserId());
        assertNull(workout.getAssignedWorkoutId());
        assertEquals(37, workout.getCreatedByUserId());
        assertEquals("Bench Day", workout.getWorkoutName());
        assertEquals("Chest and triceps", workout.getDescription());
        assertEquals(Integer.valueOf(45), workout.getDurationMinutes());
        assertEquals("2026-05-04", workout.getScheduledDate());
        assertEquals("planned", workout.getStatus());
        assertEquals("self_created", workout.getSourceType());
    }

    @Test
    public void workoutSettersOverwriteValuesCorrectly() {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Old Name",
                "Old Description",
                30,
                "2026-01-01",
                "planned",
                "self_created"
        );

        workout.setWorkoutId(2);
        workout.setUserId(99);
        workout.setAssignedWorkoutId(55);
        workout.setCreatedByUserId(88);
        workout.setWorkoutName("New Name");
        workout.setDescription("New Description");
        workout.setDurationMinutes(60);
        workout.setScheduledDate("2026-12-31");
        workout.setStatus("completed");
        workout.setSourceType("assigned_from_plan");

        assertEquals(2, workout.getWorkoutId());
        assertEquals(99, workout.getUserId());
        assertEquals(Integer.valueOf(55), workout.getAssignedWorkoutId());
        assertEquals(88, workout.getCreatedByUserId());
        assertEquals("New Name", workout.getWorkoutName());
        assertEquals("New Description", workout.getDescription());
        assertEquals(Integer.valueOf(60), workout.getDurationMinutes());
        assertEquals("2026-12-31", workout.getScheduledDate());
        assertEquals("completed", workout.getStatus());
        assertEquals("assigned_from_plan", workout.getSourceType());
    }

    @Test
    public void workoutFromJsonHandlesSelfCreatedWorkout() throws Exception {
        JSONObject json = new JSONObject();

        json.put("workoutId", 10);
        json.put("userId", 37);
        json.put("assignedWorkoutId", JSONObject.NULL);
        json.put("createdByUserId", 37);
        json.put("workoutName", "Leg Day");
        json.put("workoutDescription", "Squats and lunges");
        json.put("durationMinutes", 50);
        json.put("scheduledDate", "2026-05-04");
        json.put("status", "planned");
        json.put("sourceType", "self_created");

        Workout workout = Workout.fromJson(json);

        assertEquals(10, workout.getWorkoutId());
        assertEquals(37, workout.getUserId());
        assertNull(workout.getAssignedWorkoutId());
        assertEquals(37, workout.getCreatedByUserId());
        assertEquals("Leg Day", workout.getWorkoutName());
        assertEquals("Squats and lunges", workout.getDescription());
        assertEquals(Integer.valueOf(50), workout.getDurationMinutes());
        assertEquals("2026-05-04", workout.getScheduledDate());
        assertEquals("planned", workout.getStatus());
        assertEquals("self_created", workout.getSourceType());
    }

    @Test
    public void workoutFromJsonHandlesAssignedWorkoutWithNullDuration() throws Exception {
        JSONObject json = new JSONObject();

        json.put("workoutId", 20);
        json.put("userId", 37);
        json.put("assignedWorkoutId", 99);
        json.put("createdByUserId", 5);
        json.put("workoutName", "Trainer Plan");
        json.put("workoutDescription", "Assigned by trainer");
        json.put("durationMinutes", JSONObject.NULL);
        json.put("scheduledDate", "2026-06-01");
        json.put("status", "accepted");
        json.put("sourceType", "assigned_from_plan");

        Workout workout = Workout.fromJson(json);

        assertEquals(20, workout.getWorkoutId());
        assertEquals(37, workout.getUserId());
        assertEquals(Integer.valueOf(99), workout.getAssignedWorkoutId());
        assertEquals(5, workout.getCreatedByUserId());
        assertEquals("Trainer Plan", workout.getWorkoutName());
        assertEquals("Assigned by trainer", workout.getDescription());
        assertNull(workout.getDurationMinutes());
        assertEquals("2026-06-01", workout.getScheduledDate());
        assertEquals("accepted", workout.getStatus());
        assertEquals("assigned_from_plan", workout.getSourceType());
    }

    @Test
    public void workoutFromJsonUsesEmptyDescriptionWhenMissing() throws Exception {
        JSONObject json = new JSONObject();

        json.put("workoutId", 30);
        json.put("userId", 37);
        json.put("assignedWorkoutId", JSONObject.NULL);
        json.put("createdByUserId", 37);
        json.put("workoutName", "No Description Workout");
        json.put("durationMinutes", 25);
        json.put("scheduledDate", "2026-07-01");
        json.put("status", "planned");
        json.put("sourceType", "self_created");

        Workout workout = Workout.fromJson(json);

        assertEquals("No Description Workout", workout.getWorkoutName());
        assertEquals("", workout.getDescription());
    }

    @Test
    public void workoutToUpdateJsonIncludesExpectedFields() throws Exception {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Bench Day",
                "Chest and triceps",
                45,
                "2026-05-04",
                "planned",
                "self_created"
        );

        JSONObject json = workout.toUpdateJson();

        assertEquals("Bench Day", json.getString("workoutName"));
        assertEquals("Chest and triceps", json.getString("workoutDescription"));
        assertEquals(45, json.getInt("durationMinutes"));
        assertEquals("2026-05-04", json.getString("scheduledDate"));
    }

    @Test
    public void workoutToUpdateJsonHandlesNullDuration() throws Exception {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Stretch Day",
                "Mobility work",
                null,
                "2026-05-05",
                "planned",
                "self_created"
        );

        JSONObject json = workout.toUpdateJson();

        assertEquals("Stretch Day", json.getString("workoutName"));
        assertEquals("Mobility work", json.getString("workoutDescription"));
        assertTrue(json.isNull("durationMinutes"));
        assertEquals("2026-05-05", json.getString("scheduledDate"));
    }

    @Test
    public void workoutSourceTypeHelpersWork() {
        Workout selfCreated = new Workout(
                1,
                37,
                null,
                37,
                "Self Workout",
                "Made by user",
                30,
                "2026-05-04",
                "planned",
                "self_created"
        );

        Workout assigned = new Workout(
                2,
                37,
                100,
                5,
                "Assigned Workout",
                "Made by trainer",
                null,
                "2026-05-05",
                "accepted",
                "assigned_from_plan"
        );

        assertTrue(selfCreated.isSelfCreated());
        assertFalse(selfCreated.isAssignedFromPlan());

        assertFalse(assigned.isSelfCreated());
        assertTrue(assigned.isAssignedFromPlan());
    }

    @Test
    public void workoutSourceTypeHelpersAreCaseInsensitive() {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Mixed Case",
                "Testing case insensitive source type",
                30,
                "2026-05-04",
                "planned",
                "SELF_CREATED"
        );

        assertTrue(workout.isSelfCreated());
        assertFalse(workout.isAssignedFromPlan());

        workout.setSourceType("ASSIGNED_FROM_PLAN");

        assertFalse(workout.isSelfCreated());
        assertTrue(workout.isAssignedFromPlan());
    }

    @Test
    public void workoutSourceTypeHelpersReturnFalseForUnknownType() {
        Workout workout = new Workout(
                1,
                37,
                null,
                37,
                "Unknown Type",
                "Testing unknown source type",
                30,
                "2026-05-04",
                "planned",
                "other"
        );

        assertFalse(workout.isSelfCreated());
        assertFalse(workout.isAssignedFromPlan());
    }

//workout adapter

    @Test
    public void workoutAdapterItemCountAndViewTypesWork() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeSelfWorkout(1));
        workouts.add(makeAssignedWorkout(2, "assigned", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        assertEquals(2, adapter.getItemCount());

        int selfType = adapter.getItemViewType(0);
        int assignedType = adapter.getItemViewType(1);

        assertNotEquals(selfType, assignedType);
    }

    @Test
    public void workoutAdapterCreatesAndBindsSelfWorkoutHolder() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeSelfWorkout(1));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<RecyclerView.ViewHolder> holderRef = new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set(holder);
        });

        assertTrue(holderRef.get() instanceof WorkoutAdapter.SelfWorkoutViewHolder);

        WorkoutAdapter.SelfWorkoutViewHolder holder =
                (WorkoutAdapter.SelfWorkoutViewHolder) holderRef.get();

        assertEquals("Workout 1", holder.editName.getText().toString());
        assertEquals("2026-05-04", holder.editDate.getText().toString());
        assertEquals("Description 1", holder.editDescription.getText().toString());
        assertEquals("45", holder.txtDuration.getText().toString());
        assertEquals("Created by you", holder.txtSourceType.getText().toString());
        assertEquals("Status: planned", holder.txtStatus.getText().toString());
    }

    @Test
    public void workoutAdapterSelfWorkoutDurationButtonsWork() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        Workout workout = makeSelfWorkout(1);
        workout.setDurationMinutes(1);
        workouts.add(workout);

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.SelfWorkoutViewHolder> holderRef = new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.SelfWorkoutViewHolder) holder);

            holderRef.get().btnDurationPlus.performClick();
            holderRef.get().btnDurationMinus.performClick();
            holderRef.get().btnDurationMinus.performClick();
            holderRef.get().btnDurationMinus.performClick();
        });

        assertEquals(Integer.valueOf(0), workout.getDurationMinutes());
        assertEquals("0", holderRef.get().txtDuration.getText().toString());
    }

    @Test
    public void workoutAdapterSelfWorkoutNullDurationShowsZeroAndPlusWorks() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        Workout workout = makeSelfWorkout(1);
        workout.setDurationMinutes(null);
        workouts.add(workout);

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.SelfWorkoutViewHolder> holderRef = new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.SelfWorkoutViewHolder) holder);

            assertEquals("0", holderRef.get().txtDuration.getText().toString());

            holderRef.get().btnDurationPlus.performClick();
        });

        assertEquals(Integer.valueOf(1), workout.getDurationMinutes());
        assertEquals("1", holderRef.get().txtDuration.getText().toString());
    }

    @Test
    public void workoutAdapterSelfWorkoutSaveEmptyFieldsDoesNotUpdateWorkout() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        Workout workout = makeSelfWorkout(1);
        workouts.add(workout);

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            WorkoutAdapter.SelfWorkoutViewHolder selfHolder =
                    (WorkoutAdapter.SelfWorkoutViewHolder) holder;

            selfHolder.editName.setText("");
            selfHolder.btnSave.performClick();
        });

        assertEquals("Workout 1", workout.getWorkoutName());
    }

    @Test
    public void workoutAdapterSelfWorkoutSaveValidFieldsUpdatesLocalWorkout() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        Workout workout = makeSelfWorkout(1);
        workouts.add(workout);

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            WorkoutAdapter.SelfWorkoutViewHolder selfHolder =
                    (WorkoutAdapter.SelfWorkoutViewHolder) holder;

            selfHolder.editName.setText("Updated Workout");
            selfHolder.editDate.setText("2026-06-01");
            selfHolder.editDescription.setText("Updated Description");

            selfHolder.btnSave.performClick();
        });

        assertEquals("Updated Workout", workout.getWorkoutName());
        assertEquals("2026-06-01", workout.getScheduledDate());
        assertEquals("Updated Description", workout.getDescription());
    }

    @Test
    public void workoutAdapterSelfWorkoutDeleteClickDetachedDoesNotRemoveList() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeSelfWorkout(1));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            WorkoutAdapter.SelfWorkoutViewHolder selfHolder =
                    (WorkoutAdapter.SelfWorkoutViewHolder) holder;

            selfHolder.btnDelete.performClick();
        });

        assertEquals(1, workouts.size());
    }

    @Test
    public void workoutAdapterCreatesAndBindsAssignedWorkoutHolderAssignedStatus() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeAssignedWorkout(1, "assigned", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.AssignedWorkoutViewHolder> holderRef =
                new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.AssignedWorkoutViewHolder) holder);
        });

        WorkoutAdapter.AssignedWorkoutViewHolder holder = holderRef.get();

        assertEquals("Assigned Workout 1", holder.txtName.getText().toString());
        assertEquals("2026-05-05", holder.txtDate.getText().toString());
        assertEquals("Assigned Description 1", holder.txtDescription.getText().toString());
        assertEquals("Assigned by trainer", holder.txtSourceType.getText().toString());
        assertEquals("Status: assigned", holder.txtStatus.getText().toString());

        assertEquals(View.VISIBLE, holder.btnAccept.getVisibility());
        assertEquals(View.VISIBLE, holder.btnReject.getVisibility());
        assertEquals(View.GONE, holder.btnComplete.getVisibility());
    }

    @Test
    public void workoutAdapterAssignedWorkoutAcceptedStatusShowsCompleteButton() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeAssignedWorkout(1, "accepted", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.AssignedWorkoutViewHolder> holderRef =
                new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.AssignedWorkoutViewHolder) holder);
        });

        WorkoutAdapter.AssignedWorkoutViewHolder holder = holderRef.get();

        assertEquals(View.GONE, holder.btnAccept.getVisibility());
        assertEquals(View.GONE, holder.btnReject.getVisibility());
        assertEquals(View.VISIBLE, holder.btnComplete.getVisibility());
    }

    @Test
    public void workoutAdapterAssignedWorkoutInProgressStatusShowsCompleteButton() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeAssignedWorkout(1, "in_progress", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.AssignedWorkoutViewHolder> holderRef =
                new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.AssignedWorkoutViewHolder) holder);
        });

        WorkoutAdapter.AssignedWorkoutViewHolder holder = holderRef.get();

        assertEquals(View.GONE, holder.btnAccept.getVisibility());
        assertEquals(View.GONE, holder.btnReject.getVisibility());
        assertEquals(View.VISIBLE, holder.btnComplete.getVisibility());
    }

    @Test
    public void workoutAdapterAssignedWorkoutCompletedStatusHidesAllButtons() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeAssignedWorkout(1, "completed", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.AssignedWorkoutViewHolder> holderRef =
                new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.AssignedWorkoutViewHolder) holder);
        });

        WorkoutAdapter.AssignedWorkoutViewHolder holder = holderRef.get();

        assertEquals(View.GONE, holder.btnAccept.getVisibility());
        assertEquals(View.GONE, holder.btnReject.getVisibility());
        assertEquals(View.GONE, holder.btnComplete.getVisibility());
    }

    @Test
    public void workoutAdapterAssignedWorkoutNullStatusHidesAllButtons() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        Workout workout = makeAssignedWorkout(1, null, 100);
        workouts.add(workout);

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        AtomicReference<WorkoutAdapter.AssignedWorkoutViewHolder> holderRef =
                new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            holderRef.set((WorkoutAdapter.AssignedWorkoutViewHolder) holder);
        });

        WorkoutAdapter.AssignedWorkoutViewHolder holder = holderRef.get();

        assertEquals(View.GONE, holder.btnAccept.getVisibility());
        assertEquals(View.GONE, holder.btnReject.getVisibility());
        assertEquals(View.GONE, holder.btnComplete.getVisibility());
    }

    @Test
    public void workoutAdapterAssignedWorkoutStatusButtonsDetachedDoNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<Workout> workouts = new ArrayList<>();
        workouts.add(makeAssignedWorkout(1, "assigned", 100));

        WorkoutAdapter adapter = new WorkoutAdapter(context, workouts);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);

            RecyclerView.ViewHolder holder =
                    adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

            adapter.onBindViewHolder(holder, 0);

            WorkoutAdapter.AssignedWorkoutViewHolder assignedHolder =
                    (WorkoutAdapter.AssignedWorkoutViewHolder) holder;

            assignedHolder.btnAccept.performClick();
            assignedHolder.btnReject.performClick();
            assignedHolder.btnComplete.performClick();
        });

        assertEquals(1, workouts.size());
    }

    private Workout makeSelfWorkout(int id) {
        return new Workout(
                id,
                37,
                null,
                37,
                "Workout " + id,
                "Description " + id,
                45,
                "2026-05-04",
                "planned",
                "self_created"
        );
    }

    private Workout makeAssignedWorkout(int id, String status, Integer assignedWorkoutId) {
        return new Workout(
                id,
                37,
                assignedWorkoutId,
                5,
                "Assigned Workout " + id,
                "Assigned Description " + id,
                null,
                "2026-05-05",
                status,
                "assigned_from_plan"
        );
    }

}
