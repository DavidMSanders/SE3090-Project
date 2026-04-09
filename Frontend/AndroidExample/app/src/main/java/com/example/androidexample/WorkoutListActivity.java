package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.Button;

public class WorkoutListActivity extends AppCompatActivity {
    //http://coms-3090-035.class.las.iastate.edu:8080
    private static final String BASE_URL = "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io";
    private static final String WORKOUTS_URL = BASE_URL + "/api/v1/workouts";

    private RecyclerView WorkoutRecycler;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList;

    private Button btnGoToMain1;

    private EditText createWorkoutName, createWorkoutDate, createWorkoutDescription;
    private Button btnCreateWorkout;

    private CheckBox checkHideCreate;
    private View createWorkoutBox;

    private Button CreatebtnDurationMinus, CreatebtnDurationPlus;
    private TextView txtDurationValue;
    private int durationValue = 30;

    private Handler durationHandler = new Handler();
    private boolean isPlusHeld = false;
    private boolean isMinusHeld = false;

    private Button btnSetToday;



    @SuppressLint("ClickableViewAccessibility")
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

        CreatebtnDurationMinus = findViewById(R.id.CreatebtnDurationMinus);
        CreatebtnDurationPlus = findViewById(R.id.CreatebtnDurationPlus);


        txtDurationValue = findViewById(R.id.CreateitemWorkoutDuration);

        btnSetToday = findViewById(R.id.btnSetToday);

        btnSetToday.setOnClickListener(v -> {
                    // Get the current date
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                    String today =  sdf.format(new Date());
                    createWorkoutDate.setText(today);
                });



        //single increase no hold
        CreatebtnDurationPlus.setOnClickListener(v -> {
                    if (durationValue < 300) {
                        durationValue++;
                        txtDurationValue.setText(String.valueOf(durationValue));
                    }
        });

        CreatebtnDurationPlus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isPlusHeld = true;
                    durationHandler.postDelayed(plusRunnable, 1000); // hold delay
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    isPlusHeld = false;
                    durationHandler.removeCallbacks(plusRunnable);
                    v.performClick(); //accessibility click (was warning)
                    return true;
                }
                return false;
            }
        });




        CreatebtnDurationMinus.setOnClickListener(v -> {
            if (durationValue > 0) {
                durationValue--;
                txtDurationValue.setText(String.valueOf(durationValue));
            }
        });

        CreatebtnDurationMinus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isMinusHeld = true;
                    durationHandler.postDelayed(minusRunnable, 1000); // hold delay
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    isMinusHeld = false;
                    durationHandler.removeCallbacks(minusRunnable);
                    v.performClick(); //accessibility click (was warning)
                    return true;
                }
                return false;
            }
        });




        createWorkoutName = findViewById(R.id.CreateitemWorkoutName);
        createWorkoutDate = findViewById(R.id.CreateitemWorkoutDate);

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
        KeepUser keepUser = new KeepUser(this);
        int userId = keepUser.getUserId();

        String url = WORKOUTS_URL + "/user/" + userId;


        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
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
        //String durationStr = createWorkoutDuration.getText().toString().trim();
        String desc = createWorkoutDescription.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_LONG).show();
            return;
        }

        KeepUser keepUser = new KeepUser(this);
        int userId = keepUser.getUserId();

        int duration = durationValue;
        /*
        try {
            duration = Integer.parseInt(durationStr);
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Duration must be a number", Toast.LENGTH_LONG).show();
            return;
        }
        */

        JSONObject body = new JSONObject();
        try {
            body.put("userId", userId);
            body.put("workoutName", name);
            body.put("workoutDescription", desc);
            body.put("durationMinutes", duration);
            body.put("scheduledDate", date);



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

                        loadAllWorkouts(); // reload instead
                        Toast.makeText(this, "Workout created!", Toast.LENGTH_LONG).show();
                        WorkoutRecycler.scrollToPosition(0);

                        createWorkoutName.setText("");
                        createWorkoutDate.setText("");

                        //createWorkoutDuration.setText("");

                        durationValue = 30;
                        txtDurationValue.setText(String.valueOf(durationValue));

                        createWorkoutDescription.setText("");

                        loadAllWorkouts();
                        //reload workouts from both self and assigned
                        Toast.makeText(this, "Workout created!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
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


    //Hold duration functions
    private final Runnable plusRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlusHeld && durationValue < 300) {
                durationValue += 5;
                if(durationValue > 300)
                {
                    durationValue = 300;
                }
                txtDurationValue.setText(String.valueOf(durationValue));
                durationHandler.postDelayed(this, 150); // repeat speed
            }
        }
    };

    private final Runnable minusRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMinusHeld && durationValue > 0) {
                durationValue -= 5;
                if(durationValue < 0)
                {
                    durationValue = 0;
                }
                txtDurationValue.setText(String.valueOf(durationValue));
                durationHandler.postDelayed(this, 150); // repeat speed
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        durationHandler.removeCallbacks(plusRunnable);
        durationHandler.removeCallbacks(minusRunnable);
    }

}
