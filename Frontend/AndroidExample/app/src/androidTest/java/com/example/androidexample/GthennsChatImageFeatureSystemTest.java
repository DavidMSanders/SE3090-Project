package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;

@RunWith(AndroidJUnit4.class)
public class GthennsChatImageFeatureSystemTest {

    // Known test user/group seeded into KeepUser — no network calls made
    private static final String TEST_USERNAME = "test12345";
    private static final String TEST_ROLE = "trainer";
    private static final int TEST_USER_ID = 43;
    private static final String TEST_GROUP_NAME = "TestGroup";
    private static final String TEST_GROUP_TYPE = "trainer";
    private static final String TEST_GROUP_DESC = "testgroup don't delete";
    private static final int TEST_GROUP_ID = 35;

    @Before
    public void seedUser() {
        KeepUser keepUser = new KeepUser(ApplicationProvider.getApplicationContext());
        keepUser.saveUser(TEST_USER_ID, TEST_USERNAME, TEST_ROLE);
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void chatComposerAndImageControls_haveExpectedDefaults() {
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("groupId", TEST_GROUP_ID);

        try (ActivityScenario<GroupChatActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                EditText messageInput = activity.findViewById(R.id.etChatMessage);
                Button sendButton = activity.findViewById(R.id.btnSendMessage);
                Button sendImageButton = activity.findViewById(R.id.btnSendImage);

                assertEquals("Type a message", String.valueOf(messageInput.getHint()));
                assertTrue("Send button should show a valid permission state",
                        "Send".equals(sendButton.getText().toString())
                        || "MUTED".equals(sendButton.getText().toString()));
                assertEquals("Send image", String.valueOf(sendImageButton.getContentDescription()));
            });
        }
    }

    @Test
    public void chatGroupId_displayedInGroupIdLabel() {
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("groupId", TEST_GROUP_ID);
        intent.putExtra("groupName", TEST_GROUP_NAME);
        intent.putExtra("groupType", TEST_GROUP_TYPE);
        intent.putExtra("groupDescription", TEST_GROUP_DESC);

        try (ActivityScenario<GroupChatActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView tvGroupId = activity.findViewById(R.id.tvGroupId);
                assertNotNull("Group ID label must exist", tvGroupId);
                assertEquals("ID: " + TEST_GROUP_ID, tvGroupId.getText().toString());
            });
        }
    }

    @Test
    public void chatImageButton_hasAccessibleLabelAndCameraText() {
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("groupId", TEST_GROUP_ID);
        intent.putExtra("groupName", TEST_GROUP_NAME);
        intent.putExtra("groupType", TEST_GROUP_TYPE);
        intent.putExtra("groupDescription", TEST_GROUP_DESC);

        try (ActivityScenario<GroupChatActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                Button sendImageButton = activity.findViewById(R.id.btnSendImage);
                TextView title = activity.findViewById(R.id.tvChatGroupTitle);

                assertNotNull(sendImageButton);
                assertTrue(sendImageButton.isShown());
                assertEquals("Send image", String.valueOf(sendImageButton.getContentDescription()));
                assertTrue("Image button text should include a camera icon",
                        String.valueOf(sendImageButton.getText()).contains("📷"));
                assertEquals(TEST_GROUP_NAME, title.getText().toString());
            });
        }
    }

    @Test
    public void chatTitleFallsBackWhenGroupNameMissing() {
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("groupId", TEST_GROUP_ID);

        try (ActivityScenario<GroupChatActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.tvChatGroupTitle);
                assertEquals("Group Chat", title.getText().toString());
            });
        }
    }

    @Test
    public void sendMessageRequest_withNullOptionalFields_setsDefaults() {
        SendMessageRequest request = new SendMessageRequest(TEST_GROUP_ID, null, null, null);
        JSONObject json = request.toJson();

        assertEquals("SEND", json.optString("eventType"));
        assertEquals(TEST_GROUP_ID, json.optInt("groupId"));
        assertEquals("NORMAL", json.optString("messageType"));
        assertTrue("targetUserId should be explicit null", json.isNull("targetUserId"));
        assertEquals("", json.optString("textBody"));
    }

    @Test
    public void sendMessageRequest_withDirectImageFields_keepsProvidedValues() {
        SendMessageRequest request = new SendMessageRequest(TEST_GROUP_ID, "DIRECT", 99, "caption-text");
        JSONObject json = request.toJson();

        assertEquals("SEND", json.optString("eventType"));
        assertEquals(TEST_GROUP_ID, json.optInt("groupId"));
        assertEquals("DIRECT", json.optString("messageType"));
        assertEquals(99, json.optInt("targetUserId"));
        assertEquals("caption-text", json.optString("textBody"));
    }

    @Test
    public void imageMessage_fromJson_parsesExpectedFields() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", 321);
        payload.put("senderId", TEST_USER_ID);
        payload.put("groupId", TEST_GROUP_ID);
        payload.put("imageUrl", "https://example.com/image.png");
        payload.put("caption", "post workout");
        payload.put("sentAt", "2026-05-04T10:15:30");

        ImageMessage parsed = ImageMessage.fromJson(payload);

        assertEquals(321L, parsed.getId());
        assertEquals(TEST_USER_ID, parsed.getSenderId());
        assertEquals(TEST_GROUP_ID, parsed.getGroupId());
        assertEquals("https://example.com/image.png", parsed.getImageUrl());
        assertEquals("post workout", parsed.getCaption());
        assertEquals("2026-05-04T10:15:30", parsed.getSentAt());
    }

    @Test
    public void message_fromJson_supportsImageMessageTypeAndNullTarget() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", 444);
        payload.put("originatorId", TEST_USER_ID);
        payload.put("groupId", TEST_GROUP_ID);
        payload.put("username", TEST_USERNAME);
        payload.put("messageType", "IMAGE");
        payload.put("textBody", "caption body");
        payload.put("createdTimestamp", "2026-05-04T12:00:00");
        payload.put("targetUserId", JSONObject.NULL);

        Message parsed = Message.fromJson(payload);

        assertEquals(444L, parsed.getMessageId());
        assertEquals("IMAGE", parsed.getMessageType());
        assertEquals("caption body", parsed.getTextBody());
        assertEquals(TEST_GROUP_ID, parsed.getGroupId().intValue());
        assertTrue("targetUserId should stay null", parsed.getTargetUserId() == null);
    }
}
