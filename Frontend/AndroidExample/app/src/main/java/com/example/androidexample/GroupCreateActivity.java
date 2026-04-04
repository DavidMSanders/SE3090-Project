package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class GroupCreateActivity extends AppCompatActivity {

    private EditText TextCreateGroupName;
    //private EditText TextCreateGroupType;
    private EditText TextCreateGroupDescription;
    private Button btnSubmitCreateGroup;
    private Button btnBackFromCreateGroup;

    private static final String BASE_URL =
            "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String GROUP_TYPE = "Trainer Group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        TextCreateGroupName = findViewById(R.id.etCreateGroupName);
        //TextCreateGroupType = findViewById(R.id.tvCreateGroupTypeValue);
        TextCreateGroupDescription = findViewById(R.id.etCreateGroupDescription);
        btnSubmitCreateGroup = findViewById(R.id.btnSubmitCreateGroup);
        btnBackFromCreateGroup = findViewById(R.id.btnBackFromCreateGroup);

        btnSubmitCreateGroup.setOnClickListener(v -> CreateGroup());
        btnBackFromCreateGroup.setOnClickListener(v -> finish());
    }

    private void CreateGroup() {
        String groupName = TextCreateGroupName.getText().toString().trim();
        //String groupType = TextCreateGroupType.getText().toString().trim();
        String groupDescription = TextCreateGroupDescription.getText().toString().trim();

        //basic checks
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_LONG).show();
            return;
        }
        if(groupDescription.isEmpty()){
            Toast.makeText(this, "Please enter a group description", Toast.LENGTH_LONG).show();
            return;
        }

        //import persistent user
        KeepUser keepUser = new KeepUser(this);
        int userId = keepUser.getUserId();

        //String role = keepUser.getRole();
        //could do FE role check but its already hidden otherwise so it should be fine

        sendCreateGroupRequest(groupName, groupDescription, userId);

    }
    private void sendCreateGroupRequest(String groupName, String groupDescription, int userId) {
        String url = BASE_URL + "/api/v1/groups";

        JSONObject body = new JSONObject();

        try{
            body.put("creatorId", userId);
            body.put("groupTitle", groupName);
            body.put("groupType", "Trainer Group");
            body.put("groupDetails", groupDescription);
        } catch (Exception e){
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;

        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(
                            GroupCreateActivity.this,
                            "Group created successfully",
                            Toast.LENGTH_LONG
                    ).show();


                    finish();
                },
                error -> {
                    Toast.makeText(
                            GroupCreateActivity.this,
                            "Failed to create group: " + readableVolleyError(error),
                            Toast.LENGTH_LONG
                    ).show();
                }
        ){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }
    private String readableVolleyError(com.android.volley.VolleyError error) {
        if(error==null) return "Unknown error";
        if(error.networkResponse!=null) {
            return "HTTP " + error.networkResponse.statusCode;
        }
        else {
            return error.toString();
        }
    }

}
