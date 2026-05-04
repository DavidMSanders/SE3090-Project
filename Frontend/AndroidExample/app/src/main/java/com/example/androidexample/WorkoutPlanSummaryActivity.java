package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanSummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerWorkoutPlanSummaries;

    private final List<WorkoutPlanSummary> summaryList = new ArrayList<>();
    private WorkoutPlanSummaryAdapter adapter;

    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080/api/v1";
    //http://coms-3090-035.class.las.iastate.edu:8080/api/v1
    private int trainerId;

    private Button btnBackWorkoutPlanSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plan_summary);

        recyclerWorkoutPlanSummaries = findViewById(R.id.recyclerWorkoutPlanSummaries);


        btnBackWorkoutPlanSummary = findViewById(R.id.btnBackWorkoutPlanSummary);
        btnBackWorkoutPlanSummary.setOnClickListener(v -> finish());

        KeepUser keepUser = new KeepUser(this);
        trainerId = keepUser.getUserId();

        recyclerWorkoutPlanSummaries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutPlanSummaryAdapter(summaryList);
        recyclerWorkoutPlanSummaries.setAdapter(adapter);

        loadWorkoutPlanSummaries();
    }

    private void loadWorkoutPlanSummaries() {
        String url = BASE_URL + "/assigned-workouts/trainer/"+ trainerId+ "/plan-summaries";
        Toast.makeText(this, url, Toast.LENGTH_LONG).show();


        Toast.makeText(this, "Calling summary endpoint", Toast.LENGTH_SHORT).show();
        android.util.Log.d("PLAN_SUMMARY", "URL = " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    android.util.Log.d("PLAN_SUMMARY", "SUCCESS response = " + response.toString());
                    Toast.makeText(this, "Loaded workout plan summaries", Toast.LENGTH_SHORT).show();
                    parseWorkoutPlanSummaries(response);
                },
                error -> {
                    android.util.Log.e("PLAN_SUMMARY", "FAILED URL = " + url);
                    android.util.Log.e("PLAN_SUMMARY", "Volley error = " + error.toString());

                    if (error.networkResponse != null) {
                        android.util.Log.e("PLAN_SUMMARY", "Status code = " + error.networkResponse.statusCode);

                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            android.util.Log.e("PLAN_SUMMARY", "Error body = " + body);
                        } catch (Exception e) {
                            android.util.Log.e("PLAN_SUMMARY", "Could not read error body");
                        }
                    } else {
                        android.util.Log.e("PLAN_SUMMARY", "No networkResponse");
                    }

                    Toast.makeText(this, "Failed to load workout plan summaries", Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseWorkoutPlanSummaries(JSONArray response) {
        summaryList.clear();

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                WorkoutPlanSummary summary = new WorkoutPlanSummary();

                summary.setPlanId(obj.optInt("planId"));
                summary.setPlanName(obj.optString("planName"));

                summary.setAcceptedCount(obj.optInt("acceptedCount"));
                summary.setRejectedCount(obj.optInt("rejectedCount"));
                summary.setCompletedCount(obj.optInt("completedCount"));
                summary.setPendingCount(obj.optInt("pendingCount"));

                JSONObject usersObj = obj.optJSONObject("users");

                if (usersObj != null) {
                    summary.setAcceptedUsers(parseStringArray(usersObj.optJSONArray("accepted")));
                    summary.setRejectedUsers(parseStringArray(usersObj.optJSONArray("rejected")));
                    summary.setCompletedUsers(parseStringArray(usersObj.optJSONArray("completed")));
                    summary.setPendingUsers(parseStringArray(usersObj.optJSONArray("pending")));
                }

                summaryList.add(summary);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing workout plan summaries", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> parseStringArray(JSONArray array) {
        List<String> users = new ArrayList<>();

        if (array == null) {
            return users;
        }

        for (int i = 0; i < array.length(); i++) {
            users.add(array.optString(i));
        }

        return users;
    }
}