package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateWorkoutActivity extends AppCompatActivity {
    private EditText editName, editDate, editDuration, editDescription, editWorkoutID, getWorkoutJson;
    //currently on mock server
    private static final String BASE_URL = "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io";
    private static final String WORKOUTS_URL = BASE_URL + "/workouts";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        editName = findViewById(R.id.WorkoutName);
        editDate = findViewById(R.id.WorkoutDate);
        editDuration = findViewById(R.id.WorkoutDuration);
        editDescription = findViewById(R.id.WorkoutDescription);
        editWorkoutID = findViewById(R.id.workoutID);

        getWorkoutJson = findViewById(R.id.getWorkoutJson);



        Button SaveButton = findViewById(R.id.btnSaveWorkout);
        Button CancelButton = findViewById(R.id.btnCancelWorkout);

        Button UpdateButton = findViewById(R.id.btnUpdateWorkout);
        Button DeleteButton = findViewById(R.id.btnDeleteWorkout);

        Button getButton = findViewById(R.id.btnGetWorkout);


        CancelButton.setOnClickListener( v -> finish());
/*
        SaveButton.setOnClickListener(v -> {

            if(!validInputs()){
                return;
            }
            String name = editName.getText().toString();
            String date = editDate.getText().toString();
            int duration = Integer.parseInt(editDuration.getText().toString());
            String description = editDescription.getText().toString();

            Toast.makeText(this, "Saved workout:\n"
            + name + "\n"
            + date + "\n"
            + duration + "\n"
            + description, Toast.LENGTH_LONG).show();

            finish();


        });
*/
        // CREATE (POST)
        SaveButton.setOnClickListener(v -> {
            if (!validInputs()) return;

            JSONObject body = buildWorkoutJsonOrToast();
            if (body == null) return;

            createWorkout(body);
        });

        // UPDATE (PUT) by ID
        UpdateButton.setOnClickListener(v -> {
            Integer id = parseWorkoutIdOrError();
            if (id == null) return;

            if (!validInputs()) return;

            JSONObject body = buildWorkoutJsonWithIDOrToast(id);
            if (body == null) return;

            updateWorkout(id, body);
        });

        // DELETE (DELETE) by ID
        DeleteButton.setOnClickListener(v -> {
            Integer id = parseWorkoutIdOrError();
            if (id == null) return;

            deleteWorkout(id);
        });

        // GET (GET) by ID
        getButton.setOnClickListener(v -> {
            Integer id = parseWorkoutIdOrError(); // you already have this helper
            if (id == null) return;

            getWorkoutById(id);
        });


    }// end of onCreate()


    private boolean validInputs(){
        String name = editName.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String durationStr = editDuration.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Workout name required");
            return false;
        }
        if (date.isEmpty() || !date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            editDate.setError("Workout date required");
            return false;
        }
        if (desc.isEmpty()) {
            editDescription.setError("Workout description required");
            return false;
        }
        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            editDuration.setError("Workout duration must be a number");
            return false;
        }
        if (duration <= 0) {
            editDuration.setError("Workout duration must be greater than 0");
            return false;
        }
        return true;
    } //end of validInputs()

    private JSONObject buildWorkoutJsonOrToast() {
        String name = editName.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        int duration = Integer.parseInt(editDuration.getText().toString());
        String desc = editDescription.getText().toString().trim();

        try {
            JSONObject body = new JSONObject();

            body.put("name", name);
            body.put("date", date);
            body.put("durationMinutes", duration);
            body.put("description", desc);

            return body;
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }


    } //end of buildWorkoutJsonOrToast()

    private JSONObject buildWorkoutJsonWithIDOrToast(int id) {
        String name = editName.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        int duration = Integer.parseInt(editDuration.getText().toString());
        String desc = editDescription.getText().toString().trim();

        try {
            JSONObject body = new JSONObject();
            body.put("id", id);
            body.put("name", name);
            body.put("date", date);
            body.put("durationMinutes", duration);
            body.put("description", desc);

            return body;
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }


    } //end of buildWorkoutJsonOrToast()
    private Integer parseWorkoutIdOrError() {
        String IDString = editWorkoutID.getText().toString().trim();
        if (IDString.isEmpty()) {
            editWorkoutID.setError("Workout ID required");
            return null;
        }
        try{
            int id = Integer.parseInt(IDString);
            if(id <= 0){
                editWorkoutID.setError("Workout ID must be greater than 0");
                return null;
            }
            return id;
        } catch (NumberFormatException e) {
            editWorkoutID.setError("Workout ID must be a number");
            return null;
        }
    } // end of parseWorkoutIdOrError()

    //crud requests

    private void createWorkout(JSONObject body) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                WORKOUTS_URL,
                body,
                response -> {
                    Toast.makeText(getApplicationContext(),
                            "Created: " + response.toString(),
                            Toast.LENGTH_LONG).show();
                            finish();
                },
                error -> showVolleyError("Workout create failed", error))
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    } //end of POST workout

    private void updateWorkout(int id, JSONObject body) {


        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                WORKOUTS_URL,
                body,
                response -> {
                    Toast.makeText(this, "Workout updated!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> showVolleyError("Update failed", error)
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    } //end of PUT workout

    private void deleteWorkout(int id) {
        String url = WORKOUTS_URL + "/" + id;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Workout deleted!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> showVolleyError("Delete failed", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void getWorkoutById(int id) {
        String url = WORKOUTS_URL + "/" + id;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    getWorkoutJson.setText(response.toString());
                },
                error -> {
                    getWorkoutJson.setText("");
                    showVolleyError("Get failed", error);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void showVolleyError(String prefix, VolleyError error) {
        int code = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
        Toast.makeText(this, prefix + " (HTTP " + code + ")", Toast.LENGTH_LONG).show();
    }






}
