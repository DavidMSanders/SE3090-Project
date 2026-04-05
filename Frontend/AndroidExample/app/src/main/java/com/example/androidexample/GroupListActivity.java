package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {

    private EditText etSearchGroups;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;

    private Button btnBackFromGroups;
    private Button btnSearchGroups;

    private Button btnCreateGroup;



    private static final String BASE_URL =
            "http://coms-3090-035.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_group);

        etSearchGroups = findViewById(R.id.etSearchGroups);
        btnBackFromGroups = findViewById(R.id.btnBackFromGroups);
        btnSearchGroups = findViewById(R.id.btnSearchGroups);
        recyclerView = findViewById(R.id.rvGroups);

        btnCreateGroup = findViewById(R.id.btnCreateGroup);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //grouplist before adapter
        groupList = new ArrayList<>();
        adapter = new GroupAdapter(this, groupList);

        recyclerView.setAdapter(adapter);

        //initial loading of groups
        getGroups("");

        btnSearchGroups.setOnClickListener(v -> {
            String searchQuery = etSearchGroups.getText().toString().trim();
            getGroups(searchQuery);
        });

        KeepUser keepUser = new KeepUser(this);
        String role = keepUser.getRole(); //getRole()

        if (role != null && role.equalsIgnoreCase("trainer")) {
            btnCreateGroup.setVisibility(Button.VISIBLE);
        } else {
            btnCreateGroup.setVisibility(Button.GONE);
        }

        btnCreateGroup.setOnClickListener(v -> {
            Toast.makeText(this, "Go to Create Group page", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(GroupListActivity.this, GroupCreateActivity.class);
            startActivity(intent);
        });

        btnBackFromGroups.setOnClickListener(v -> {
            finish();
        });
    }

    private void getGroups(String searchQuery){
        KeepUser keepUser = new KeepUser(this);
        int userId = keepUser.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "No logged in user found", Toast.LENGTH_LONG).show();
            return;
        }

        String url = BASE_URL + "/api/v1/groups?userId=" + userId;
        if (!searchQuery.isEmpty()) {
            url += "&search=" + searchQuery;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::parseGroupsResponse,
                error -> Toast.makeText(
                        GroupListActivity.this,
                        "Failed to load groups: " + readableVolleyError(error),
                        Toast.LENGTH_LONG
                ).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }
    private void parseGroupsResponse(JSONArray response) {
        groupList.clear(); //first clear

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject groupJson = response.getJSONObject(i);
                int groupId = groupJson.getInt("groupId");
                String groupName = groupJson.getString("groupName");
                String groupType = groupJson.getString("groupType");
                String groupDescription = groupJson.getString("groupDescription");

                Integer membershipId = null;
                if (groupJson.has("membershipId") && !groupJson.isNull("membershipId")) {
                    membershipId = groupJson.getInt("membershipId");
                }

                String membershipRole = null;
                if (groupJson.has("membershipRole") && !groupJson.isNull("membershipRole")) {
                    membershipRole = groupJson.getString("membershipRole");
                }

                Group group = new Group(groupId, groupName, groupType, groupDescription, membershipId, membershipRole);

                groupList.add(group);


            } catch (Exception e) {
                Toast.makeText(
                        GroupListActivity.this,
                        "Failed to parse group: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

        }
        //update group adapter
        adapter.notifyDataSetChanged();

        if (groupList.isEmpty()) {
            Toast.makeText(this, "No groups found", Toast.LENGTH_SHORT).show();
        }

    }

    private String readableVolleyError(VolleyError error) {
        if(error==null) return "Unknown error";
        if(error.networkResponse!=null) {
            return "HTTP " + error.networkResponse.statusCode;
        }
        else {
            return error.toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroups("");
    }



}
