package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsGroupChatButtonStateSystemTest {

    private static final int TEST_USER_ID = 43;
    private static final String TEST_USERNAME = "chat-test-user";
    private static final String TEST_ROLE = "trainer";
    private static final int TEST_GROUP_ID = 35;
    private static final String TEST_GROUP_NAME = "Test Chat Group";
    private static final String TEST_GROUP_DESC = "A test group";
    private static final String TEST_GROUP_TYPE = "Trainer Group";

    @Before
    public void seedUser() {
        KeepUser keepUser = new KeepUser(ApplicationProvider.getApplicationContext());
        keepUser.saveUser(TEST_USER_ID, TEST_USERNAME, TEST_ROLE);
    }

    private Intent buildIntent(Context context) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("groupId", TEST_GROUP_ID);
        intent.putExtra("groupName", TEST_GROUP_NAME);
        intent.putExtra("groupDescription", TEST_GROUP_DESC);
        intent.putExtra("groupType", TEST_GROUP_TYPE);
        return intent;
    }

    @Test
    public void groupChat_leaveGroupButtonIsVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                Button btnLeave = activity.findViewById(R.id.btnLeaveGroup);
                assertNotNull("Leave Group button must exist", btnLeave);
                assertTrue("Leave Group button should be shown", btnLeave.isShown());
            });
        }
    }

    @Test
    public void groupChat_updateGroupButtonIsVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                Button btnUpdate = activity.findViewById(R.id.btnUpdateGroup);
                assertNotNull("Update Group button must exist", btnUpdate);
                assertTrue("Update Group button should be shown", btnUpdate.isShown());
            });
        }
    }

    @Test
    public void groupChat_announcementButtonIsVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                Button btnAnnounce = activity.findViewById(R.id.btnAnnouncement);
                assertNotNull("Announcement button must exist", btnAnnounce);
                assertTrue("Announcement button should be shown", btnAnnounce.isShown());
            });
        }
    }

    @Test
    public void groupChat_recyclerViewExists() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.rvChatMessages);
                assertNotNull("RecyclerView must exist", rv);
            });
        }
    }

    @Test
    public void groupChat_messageInputIsEmptyByDefault() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                EditText etInput = activity.findViewById(R.id.etChatMessage);
                assertNotNull(etInput);
                assertEquals("Message input should start empty", "", etInput.getText().toString());
            });
        }
    }

    @Test
    public void groupChat_groupTitleShownFromIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.tvChatGroupTitle);
                assertEquals(TEST_GROUP_NAME, title.getText().toString());
            });
        }
    }

    @Test
    public void groupChat_groupIdLabelShowsId() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                TextView tvGroupId = activity.findViewById(R.id.tvGroupId);
                assertNotNull(tvGroupId);
                assertTrue("Group ID label should contain the group id",
                        tvGroupId.getText().toString().contains(String.valueOf(TEST_GROUP_ID)));
            });
        }
    }

    @Test
    public void groupChat_sendButtonVisibleWithValidGroupId() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                Button btnSend = activity.findViewById(R.id.btnSendMessage);
                assertNotNull(btnSend);
                assertTrue(btnSend.isShown());
            });
        }
    }

    @Test
    public void groupChat_sendEmptyMessage_doesNothing() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                EditText etInput = activity.findViewById(R.id.etChatMessage);
                Button btnSend = activity.findViewById(R.id.btnSendMessage);

                etInput.setText("");
                btnSend.performClick();

                // Input remains empty — no crash, no side effect
                assertEquals("", etInput.getText().toString());
            });
        }
    }

    @Test
    public void groupChat_invalidGroupId_intentHandledGracefully() {
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent(context, GroupChatActivity.class);
        // Intentionally omit groupId so it defaults to -1
        intent.putExtra("groupName", "No-ID Group");

        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                // Group ID label should be hidden when id == -1
                TextView tvGroupId = activity.findViewById(R.id.tvGroupId);
                assertEquals(android.view.View.GONE, tvGroupId.getVisibility());
            });
        }
    }

    @Test
    public void groupChat_announcementButtonClick_doesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<GroupChatActivity> scenario =
                     ActivityScenario.launch(buildIntent(context))) {
            scenario.onActivity(activity -> {
                Button btnAnnounce = activity.findViewById(R.id.btnAnnouncement);
                // Click should toggle state without crashing
                btnAnnounce.performClick();
                assertTrue(btnAnnounce.isShown());
            });
        }
    }
}
