package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class CreateWorkoutPlanActivity extends AppCompatActivity {

    private EditText etWorkoutPlanName, etWorkoutPlanDescription;
    private Button btnSubmitWorkoutPlan;
    //http://coms-3090-035.class.las.iastate.edu:8080
//https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io
    private static final String BASE_URL = "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io/api/v1";

    private int trainerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout_plan);

        etWorkoutPlanName = findViewById(R.id.etWorkoutPlanName);
        etWorkoutPlanDescription = findViewById(R.id.etWorkoutPlanDescription);
        btnSubmitWorkoutPlan = findViewById(R.id.btnSubmitWorkoutPlan);

        KeepUser keepUser = new KeepUser(this);
        trainerId = keepUser.getUserId();

        btnSubmitWorkoutPlan.setOnClickListener(v -> createWorkoutPlan());
    }

    private void createWorkoutPlan() {
        String planName = etWorkoutPlanName.getText().toString().trim();
        String planDescription = etWorkoutPlanDescription.getText().toString().trim();

        if (planName.isEmpty()) {
            Toast.makeText(this, "Enter a workout plan name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/workout-plans";

        try {
            JSONObject body = new JSONObject();
            body.put("trainerId", trainerId);
            body.put("planName", planName);
            body.put("planDescription", planDescription);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Workout plan created", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Failed to create workout plan", Toast.LENGTH_SHORT).show()
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
        }
    }
}
