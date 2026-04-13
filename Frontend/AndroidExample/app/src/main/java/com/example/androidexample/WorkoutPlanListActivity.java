package com.example.androidexample;

import android.content.Intent;
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

import java.util.List;
import java.util.ArrayList;


public class WorkoutPlanListActivity extends AppCompatActivity {

    private RecyclerView recyclerWorkoutPlans;
    private Button btnCreateWorkoutPlan;

    private Button btnOpenAssignWorkoutPage;


    private final List<WorkoutPlan> workoutPlanList = new ArrayList<>();
    private WorkoutPlanAdapter adapter;

    //https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080/api/v1";

    private int trainerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workout_plan_list);

        recyclerWorkoutPlans = findViewById(R.id.recyclerWorkoutPlans);
        btnCreateWorkoutPlan = findViewById(R.id.btnCreateWorkoutPlan);

        btnOpenAssignWorkoutPage = findViewById(R.id.btnOpenAssignWorkoutPage);

        KeepUser keepUser = new KeepUser(this);
        trainerId = keepUser.getUserId();

        recyclerWorkoutPlans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutPlanAdapter(this, workoutPlanList);
        recyclerWorkoutPlans.setAdapter(adapter);

        btnCreateWorkoutPlan.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutPlanListActivity.this, CreateWorkoutPlanActivity.class);
            startActivity(intent);
        });

        btnOpenAssignWorkoutPage.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutPlanListActivity.this, AssignWorkoutPlanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkoutPlans();
    }

    private void loadWorkoutPlans() {
        String url = BASE_URL + "/workout-plans/trainer/" + trainerId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> parseWorkoutPlans(response),
                error -> Toast.makeText(this, "Failed to load workout plans", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseWorkoutPlans(JSONArray response) {
        workoutPlanList.clear();

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                WorkoutPlan plan = new WorkoutPlan();
                plan.setWorkoutPlanId(obj.optInt("workoutPlanId"));
                plan.setTrainerId(obj.optInt("trainerId"));
                plan.setpName(obj.optString("planName"));
                plan.setpDescription(obj.optString("planDescription"));
                plan.setCreatedAt(obj.optString("createdAt"));

                workoutPlanList.add(plan);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing workout plans", Toast.LENGTH_SHORT).show();
        }
    }
}
