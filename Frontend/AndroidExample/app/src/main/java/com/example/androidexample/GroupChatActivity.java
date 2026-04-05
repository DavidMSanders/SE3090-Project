package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupChatActivity extends AppCompatActivity {


    private RecyclerView recyclerViewMessages;
    private EditText etMessageInput;
    private Button btnSendMessage;

    private Button btnBackFromChat;

    private Button btnUpdateGroup;


    private MessageAdapter messageAdapter;
    private final ArrayList<Message> messageList = new ArrayList<>();

    private int currentUserId;
    private String currentUsername;
    private int currentGroupId;
    private String groupName;
    private String groupDescription;
    private String groupType;




    //membership booleans and role
    private boolean canSendMessages = false;
    private boolean canManageMembers = false;
    private boolean canPostAnnouncements = false;
    private boolean canAssignWorkouts = false;

    private String membershipRole;

    private ChatWebSocketManager chatWebSocketManager;


    //flags for MESSAGE TYPE

    private boolean sendAsAnnouncement= false;
    private Button btnAnnouncement;
    private boolean sendAsDirect= false;
    private Integer pendingTargetUserId = null;


    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        recyclerViewMessages = findViewById(R.id.rvChatMessages);
        etMessageInput = findViewById(R.id.etChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnBackFromChat = findViewById(R.id.btnBackFromChat);

        btnAnnouncement = findViewById(R.id.btnAnnouncement);
        btnUpdateGroup = findViewById(R.id.btnUpdateGroup);

        KeepUser user = new KeepUser(this);
        currentUserId = user.getUserId();
        currentUsername = user.getUsername();

        //currentUserId = getIntent().getIntExtra("currentUserId", -1);
        //currentUsername = getIntent().getStringExtra("currentUsername");

        currentGroupId = getIntent().getIntExtra("groupId", -1);

        groupName = getIntent().getStringExtra("groupName");
        groupDescription = getIntent().getStringExtra("groupDescription");
        groupType = getIntent().getStringExtra("groupType");

        setupRecyclerView();
        fetchMembershipAndInit();
        //had to be put inside INIT function
        //loadInitialMessages();
        //setupWebSocket();
        setupSendButton();
        setupAnnouncementButton();

        btnUpdateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(GroupChatActivity.this, Activity_Update_Group.class);

            intent.putExtra("groupId", currentGroupId);
            intent.putExtra("groupName", groupName);
            intent.putExtra("groupDescription", groupDescription);
            intent.putExtra("groupType", groupType);

            startActivity(intent);
        });

        btnBackFromChat.setOnClickListener(v -> {
            finish();
        });
    }
    private void setupRecyclerView() {
        //messageAdapter = new MessageAdapter(messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this); // test -w
        layoutManager.setStackFromEnd(false); //

        recyclerViewMessages.setLayoutManager(layoutManager); //
        //recyclerViewMessages.setAdapter(messageAdapter);

    }

    //setup adapter now
    private void setupAdapter() {
        messageAdapter = new MessageAdapter(
                messageList,
                currentUserId,
                canManageMembers,
                new MessageAdapter.OnMessageMenuActionListener() {
                    @Override
                    public void onDirectMessage(Message message) {
                        // Turn on DIRECT mode for next outgoing message
                        enableDirectMode(message.getOriginatorId());
                        Toast.makeText(
                                GroupChatActivity.this,
                                "Next message will be direct to " + message.getUsername(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onEditPermissions(Message message) {
                        // Open the edit-permissions dialog
                        //for getting user info
                        fetchTargetMembershipAndShowDialog(message);
                    }

                    @Override
                    public void onEditMessage(Message message) {
                        // For now, just put existing text into input box for editing
                        etMessageInput.setText(message.getTextBody());
                        etMessageInput.setSelection(etMessageInput.getText().length());

                        Toast.makeText(
                                GroupChatActivity.this,
                                "Editing your message",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        recyclerViewMessages.setAdapter(messageAdapter);
    }
    private void loadInitialMessages() {

            String url = BASE_URL + "/api/v1/messages/group/" + currentGroupId;

            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        messageList.clear();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                Message message = Message.fromJson(obj);
                                if(canSeeMessage(message)) {
                                    messageList.add(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        messageAdapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    },
                    error -> Toast.makeText(
                            GroupChatActivity.this,
                            "Failed to load chat history",
                            Toast.LENGTH_SHORT
                    ).show()
            );

            Volley.newRequestQueue(this).add(request);
    }



    private void setupWebSocket() {
        chatWebSocketManager = new ChatWebSocketManager();

        chatWebSocketManager.connect(currentUserId, new ChatWebSocketManager.SocketConnectListener() {
            @Override
            public void onConnected() {
                chatWebSocketManager.setGroupEventListener(currentGroupId, new ChatWebSocketManager.GroupEventListener() {
                    @Override
                    public void onEventReceived(ChatSocketEvent event) {
                        runOnUiThread(() -> handleSocketEvent(event));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(GroupChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void handleSocketEvent(ChatSocketEvent event) {
        if (event == null || event.getEventType() == null) return;

        switch (event.getEventType()) {
            case "MESSAGE_CREATED":
                if (event.getMessage() != null && canSeeMessage(event.getMessage())) {
                    messageAdapter.addMessage(event.getMessage());
                    recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                }
                break;

            case "MESSAGE_UPDATED":
                if (event.getMessage() != null) {
                    messageAdapter.updateMessage(event.getMessage());
                }
                break;

            case "MESSAGE_DELETED":
                if (event.getMessageId() != null) {
                    messageAdapter.removeMessageById(event.getMessageId());
                }
                break;

            case "GROUP_MEMBER_REMOVED":
                // slightly redundant as user communication can already be blocked
                //likely would require a whole another tracking table related to banning
                // currently leaving unimplemented as functionally this should be enough without
                break;


            case "GROUP_PERMISSION_UPDATED":
                // Only react if this permission update is for the current user
                if (event.getTargetUserId() != null && event.getTargetUserId() == currentUserId) {

                    // update local permission state
                    if (event.getMembershipRole() != null) {
                        membershipRole = event.getMembershipRole();
                    }

                    canSendMessages = event.isCanSendMessages();
                    canManageMembers = event.isCanManageMembers();
                    canPostAnnouncements = event.isCanPostAnnouncements();
                    canAssignWorkouts = event.isCanAssignWorkouts();

                    // refresh buttons/visibility
                    updatePermissionUI();

                    // adapter menu logic depends on canManageMembers
                    setupAdapter();

                    // re-filter direct messages based on new permissions
                    loadInitialMessages();

                    Toast.makeText(
                            GroupChatActivity.this,
                            "Your group permissions were updated",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;

        }
    }
    private void setupSendButton() {
        btnSendMessage.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();

            if (text.isEmpty()) {
                return;
            }

            String messageType = resolveOutgoingMessageType();

            SendMessageRequest request = new SendMessageRequest(
                    currentGroupId,
                    messageType,
                    sendAsDirect ? pendingTargetUserId:null,
                    text
            );

            chatWebSocketManager.sendGroupMessage(request);
            etMessageInput.setText("");

            // reset one-time modes after sending
            if (sendAsAnnouncement) {
                disableAnnouncementMode();
            }

            if (sendAsDirect) {
                disableDirectMode();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chatWebSocketManager != null) {
            chatWebSocketManager.disconnect();
        }
    }

    private boolean canSeeMessage(Message message) {
        if(message == null) return false;

        // All non-direct messages are visible
        if (!"DIRECT".equalsIgnoreCase(message.getMessageType())) {
            return true;
        }

        //Sender can see their own direct message
        if (message.getOriginatorId() == currentUserId) {
            return true;
        }

        // Recipient can see it
        Integer targetUserId = message.getTargetUserId();
        if (targetUserId != null && targetUserId == currentUserId) {
            return true;
        }

        //members with management permission are able to see these messages
        if(canManageMembers)
        {
            return true;
        }
        return false;
    }

    private void fetchMembershipAndInit() {
        String url = BASE_URL + "/api/v1/groupMemberships/user/" + currentUserId + "/group/" + currentGroupId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        //SET ALL PERMISSIONS
                        membershipRole = response.getString("membershipRole");

                        canSendMessages = response.getBoolean("canSendMessages");
                        canManageMembers = response.getBoolean("canManageMembers");
                        canPostAnnouncements = response.getBoolean("canPostAnnouncements");
                        canAssignWorkouts = response.getBoolean("canAssignWorkouts");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // continue normal flow
                    setupAdapter();
                    loadInitialMessages();

                    if (canPostAnnouncements) {
                        btnAnnouncement.setVisibility(View.VISIBLE);
                    } else {
                        btnAnnouncement.setVisibility(View.GONE);
                    }
                    btnSendMessage.setEnabled(canSendMessages);
                    if(!canSendMessages){
                        btnSendMessage.setText("MUTED");
                    }

                    //update group button visibility
                    if ("GROUP_LEADER".equals(membershipRole)) {
                        btnUpdateGroup.setVisibility(View.VISIBLE);
                    } else {
                        btnUpdateGroup.setVisibility(View.GONE);
                    }

                    setupWebSocket();
                },
                error -> {
                    error.printStackTrace();

                    Toast.makeText(
                            GroupChatActivity.this,
                            "Failed to load membership",
                            Toast.LENGTH_SHORT
                    ).show();

                    // fallback: everything false
                    canSendMessages = false;
                    canManageMembers = false;
                    canPostAnnouncements = false;
                    canAssignWorkouts = false;
                    //needs to do it here because the req is asyn so it needs to do the checks first

                    setupAdapter();
                    loadInitialMessages();

                    if (canPostAnnouncements) {
                        btnAnnouncement.setVisibility(View.VISIBLE);
                    } else {
                        btnAnnouncement.setVisibility(View.GONE);
                    }
                    btnSendMessage.setEnabled(canSendMessages);
                    if(!canSendMessages){
                        btnSendMessage.setText("MUTED");
                    }
                    setupWebSocket();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void enableAnnouncementMode() {
        sendAsAnnouncement = true;
        sendAsDirect = false;
        pendingTargetUserId = null;

        btnAnnouncement.setText("Announcement ON");
    }

    private void disableAnnouncementMode() {
        sendAsAnnouncement = false;
        btnAnnouncement.setText("Announcement");
    }

    private void enableDirectMode(int targetUserId) {
        sendAsDirect = true;
        pendingTargetUserId = targetUserId;

        sendAsAnnouncement = false;
        btnAnnouncement.setText("Announcement");
    }

    private void disableDirectMode() {
        sendAsDirect = false;
        pendingTargetUserId = null;
    }

    private void setupAnnouncementButton() {
        btnAnnouncement.setOnClickListener(v -> {

            //toggle between announcement modes
            if (sendAsAnnouncement) {
                disableAnnouncementMode();
                //Toast.makeText(this, "Announcement mode cancelled", Toast.LENGTH_SHORT).show();
            } else {
                enableAnnouncementMode();
                //Toast.makeText(this, "Your next message will be sent as an announcement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String resolveOutgoingMessageType() {
        if (sendAsAnnouncement) {
            return "ANNOUNCEMENT";
        }

        if (sendAsDirect) {
            return "DIRECT";
        }

        if (membershipRole != null &&
                (membershipRole.equalsIgnoreCase("trainer")
                        || membershipRole.equalsIgnoreCase("group_leader"))) {
            return "TRAINER";
        }

        return "NORMAL";
    }

    private void fetchTargetMembershipAndShowDialog(Message message) {
        String url = BASE_URL + "/api/v1/groupMemberships/user/" + message.getOriginatorId() + "/group/" +currentGroupId ;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> showEditPermissionsDialog(message, response),
                error -> Toast.makeText(
                        GroupChatActivity.this,
                        "Failed to load target membership",
                        Toast.LENGTH_SHORT
                ).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showEditPermissionsDialog(Message message, JSONObject membershipJson) {
        View dialogView = getLayoutInflater().inflate(R.layout.chat_edit_permissions, null);

        TextView tvTargetUsername = dialogView.findViewById(R.id.tvTargetUsername);
        CheckBox cbCanSendMessages = dialogView.findViewById(R.id.cbCanSendMessages);
        CheckBox cbCanManageMembers = dialogView.findViewById(R.id.cbCanManageMembers);
        CheckBox cbCanPostAnnouncements = dialogView.findViewById(R.id.cbCanPostAnnouncements);
        CheckBox cbCanAssignWorkouts = dialogView.findViewById(R.id.cbCanAssignWorkouts);
        Button btnSavePermissions = dialogView.findViewById(R.id.btnSavePermissions);

        tvTargetUsername.setText("Editing: " + message.getUsername());

        try {
            cbCanSendMessages.setChecked(membershipJson.getBoolean("canSendMessages"));
            cbCanManageMembers.setChecked(membershipJson.getBoolean("canManageMembers"));
            cbCanPostAnnouncements.setChecked(membershipJson.getBoolean("canPostAnnouncements"));
            cbCanAssignWorkouts.setChecked(membershipJson.getBoolean("canAssignWorkouts"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSavePermissions.setOnClickListener(v -> {
            sendPermissionUpdate(
                    message.getOriginatorId(),
                    cbCanSendMessages.isChecked(),
                    cbCanManageMembers.isChecked(),
                    cbCanPostAnnouncements.isChecked(),
                    cbCanAssignWorkouts.isChecked()
            );

            dialog.dismiss();
        });

    }

    //fake rn
    private void sendPermissionUpdate(int targetUserId,
                                      boolean newCanSendMessages,
                                      boolean newCanManageMembers,
                                      boolean newCanPostAnnouncements,
                                      boolean newCanAssignWorkouts) {
        try {
            JSONObject payload = new JSONObject();

            payload.put("eventType" ,"GROUP_PERMISSION_UPDATED");
            payload.put("groupId", currentGroupId);
            //payload.put("requestUserId", currentUserId);
            payload.put("targetUserId", targetUserId);

            // full overwrite style
            payload.put("membershipRole", "MEMBER");
            payload.put("canSendMessages", newCanSendMessages);
            payload.put("canManageMembers", newCanManageMembers);
            payload.put("canPostAnnouncements", newCanPostAnnouncements);
            payload.put("canAssignWorkouts", newCanAssignWorkouts);

            // TODO:
            // Later send this to backend via ws

            // chatWebSocketManager.sendPermissionUpdate(payload);

            Toast.makeText(
                    this,
                    "Permissions saved for user " + targetUserId,
                    Toast.LENGTH_SHORT
            ).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build permission update", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePermissionUI() {
        if (canPostAnnouncements) {
            btnAnnouncement.setVisibility(View.VISIBLE);
        } else {
            btnAnnouncement.setVisibility(View.GONE);
            disableAnnouncementMode();
        }

        btnSendMessage.setEnabled(canSendMessages);

        if (canSendMessages) {
            btnSendMessage.setText("Send");
        } else {
            btnSendMessage.setText("MUTED");
        }
    }
}
