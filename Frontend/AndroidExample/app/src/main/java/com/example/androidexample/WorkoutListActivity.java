package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.widget.Button;

public class WorkoutListActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String WORKOUTS_URL = BASE_URL + "/api/v1/workouts";

    private RecyclerView WorkoutRecycler;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList;

    private Button btnGoToMain1;

    private EditText createWorkoutName, createWorkoutDate, createWorkoutDuration, createWorkoutDescription;
    private Button btnCreateWorkout;

    private CheckBox checkHideCreate;
    private View createWorkoutBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_list);
        //list dashboard
        btnGoToMain1 = findViewById(R.id.btnGoToMain1);
        btnGoToMain1.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutListActivity.this, MainActivity.class);
            startActivity(intent);
        });


        checkHideCreate = findViewById(R.id.checkHideCreate);
        createWorkoutBox = findViewById(R.id.create_workout_box);

        checkHideCreate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                createWorkoutBox.setVisibility(View.GONE);
            } else {
                createWorkoutBox.setVisibility(View.VISIBLE);
            }
        });





        createWorkoutName = findViewById(R.id.CreateitemWorkoutName);
        createWorkoutDate = findViewById(R.id.CreateitemWorkoutDate);
        createWorkoutDuration = findViewById(R.id.CreateitemWorkoutDuration);
        createWorkoutDescription = findViewById(R.id.CreateitemWorkoutDescription);

        btnCreateWorkout = findViewById(R.id.btnCreateWorkout);
        btnCreateWorkout.setOnClickListener(v -> {
            createWorkout();
        });


        WorkoutRecycler = findViewById(R.id.recyclerWorkouts);
        WorkoutRecycler.setLayoutManager(new LinearLayoutManager(this));
        WorkoutRecycler.setHasFixedSize(true);

        workoutList = new ArrayList<>();

        adapter = new WorkoutAdapter(this, workoutList);
        WorkoutRecycler.setAdapter(adapter);

        loadAllWorkouts();
    }


    private void loadAllWorkouts() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                WORKOUTS_URL,
                null,
                response -> {
                    workoutList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject workoutJson = response.getJSONObject(i);
                            Workout workout = Workout.fromJson(workoutJson);
                            workoutList.add(workout);

                        } catch (JSONException e) {
                            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    } //end of Load all workouts

    private void createWorkout() {
        String name = createWorkoutName.getText().toString().trim();
        String date = createWorkoutDate.getText().toString().trim();
        String durationStr = createWorkoutDuration.getText().toString().trim();
        String desc = createWorkoutDescription.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || durationStr.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_LONG).show();
            return;
        }
        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Duration must be a number", Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("date", date);
            body.put("durationMinutes", duration);
            body.put("description", desc);
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;

        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                WORKOUTS_URL,
                body,
                response -> {
                    try{
                        Workout createdWorkout = Workout.fromJson(response);
                        workoutList.add(0, createdWorkout); //index create workout at the top of the list
                        adapter.notifyItemInserted(0);
                        WorkoutRecycler.scrollToPosition(0);

                        createWorkoutName.setText("");
                        createWorkoutDate.setText("");
                        createWorkoutDuration.setText("");
                        createWorkoutDescription.setText("");
                        Toast.makeText(this, "Workout created!", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        loadAllWorkouts();
                    }
                },

                error -> {
                    Toast.makeText(this, "Create failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
