package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Update_Group extends AppCompatActivity {

    private TextView tvCurrentGroupId;
    private TextView tvGroupType;
    private EditText etUpdateGroupName;
    private EditText etUpdateGroupDescription;
    private Button btnBackFromUpdateGroup;
    private Button btnSaveGroupUpdate;
    private TextView tvUpdateGroupResult;

    private int currentGroupId;
    private String currentGroupType;

    private int currentUserId;

    private static final String BASE_URL =
            "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group);

        tvCurrentGroupId = findViewById(R.id.tvCurrentGroupId);
        tvGroupType = findViewById(R.id.tvGroupType);
        etUpdateGroupName = findViewById(R.id.etUpdateGroupName);
        etUpdateGroupDescription = findViewById(R.id.etUpdateGroupDescription);
        btnBackFromUpdateGroup = findViewById(R.id.btnBackFromUpdateGroup);
        btnSaveGroupUpdate = findViewById(R.id.btnSaveGroupUpdate);
        tvUpdateGroupResult = findViewById(R.id.tvUpdateGroupResult);

        currentGroupId = getIntent().getIntExtra("groupId", -1);
        currentGroupType = getIntent().getStringExtra("groupType");

        String groupName = getIntent().getStringExtra("groupName");
        String groupDescription = getIntent().getStringExtra("groupDescription");

        tvCurrentGroupId.setText(String.valueOf(currentGroupId));
        tvGroupType.setText(currentGroupType != null ? currentGroupType : "");

        KeepUser currentUser = new KeepUser(this);
        currentUserId = currentUser.getUserId();

        if (groupName != null) {
            etUpdateGroupName.setText(groupName);
        }

        if (groupDescription != null) {
            etUpdateGroupDescription.setText(groupDescription);
        }

        btnBackFromUpdateGroup.setOnClickListener(v -> finish());

        btnSaveGroupUpdate.setOnClickListener(v -> saveGroupUpdate());
    }

    private void saveGroupUpdate() {
        String newGroupName = etUpdateGroupName.getText().toString().trim();
        String newGroupDescription = etUpdateGroupDescription.getText().toString().trim();

        if (currentGroupId == -1) {
            Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newGroupName.isEmpty()) {
            etUpdateGroupName.setError("Group name is required");
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("groupId", currentGroupId);
            body.put("requestUserId", currentUserId);
            body.put("groupName", newGroupName);
            body.put("groupDescription", newGroupDescription);
            body.put("groupType", currentGroupType);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request body", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/v1/groups/" + currentGroupId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    //tvUpdateGroupResult.setText(response.toString());
                    Toast.makeText(
                            Activity_Update_Group.this,
                            "Group updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                },
                error -> {
                    //tvUpdateGroupResult.setText("Update failed");
                    Toast.makeText(
                            Activity_Update_Group.this,
                            "Failed to update group",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
