package com.example.androidexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class PersonalBestActivity extends AppCompatActivity {


    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";

    private EditText etExerciseName, etBestValue, etUnit, etDate;
    private Button btnAdd;
    private RecyclerView rvPersonalBests;

    private ArrayList<PersonalBest> personalBestList;
    private PersonalBestAdapter adapter;

    private KeepUser keepUser;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_best);

        keepUser = new KeepUser(this);
        userId = keepUser.getUserId();

        etExerciseName = findViewById(R.id.etExerciseName);
        etBestValue = findViewById(R.id.etBestValue);
        etUnit = findViewById(R.id.etUnit);
        etDate = findViewById(R.id.etDate);
        btnAdd = findViewById(R.id.btnAdd);
        rvPersonalBests = findViewById(R.id.rvPersonalBests);

        personalBestList = new ArrayList<>();

        adapter = new PersonalBestAdapter(personalBestList, new PersonalBestAdapter.PersonalBestClickListener() {
            @Override
            public void onUpdate(PersonalBest pb) {
                showUpdateDialog(pb);
            }

            @Override
            public void onDelete(PersonalBest pb) {
                deletePersonalBest(pb.id);
            }
        });

        rvPersonalBests.setLayoutManager(new LinearLayoutManager(this));
        rvPersonalBests.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addPersonalBest());

        // Load by default when page opens
        loadPersonalBests();
    }

    private void loadPersonalBests() {
        String url = BASE_URL + "/api/v1/users/" + userId + "/personalbests";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    personalBestList.clear();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            PersonalBest pb = new PersonalBest(
                                    obj.getInt("id"),
                                    obj.getInt("userId"),
                                    obj.getString("exerciseName"),
                                    obj.getDouble("bestValue"),
                                    obj.getString("unit"),
                                    obj.getString("recordDate")
                            );

                            personalBestList.add(pb);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error reading personal bests", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load personal bests", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void addPersonalBest() {
        String exerciseName = etExerciseName.getText().toString().trim();
        String bestValueText = etBestValue.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String recordDate = etDate.getText().toString().trim();

        if (exerciseName.isEmpty() || bestValueText.isEmpty() || unit.isEmpty() || recordDate.isEmpty()) {
            Toast.makeText(this, "Fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double bestValue = Double.parseDouble(bestValueText);

        String url = BASE_URL + "/api/v1/personalbests";

        JSONObject body = new JSONObject();
        try {
            body.put("userId", userId);
            body.put("exerciseName", exerciseName);
            body.put("bestValue", bestValue);
            body.put("unit", unit);
            body.put("recordDate", recordDate);
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Personal best added", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    loadPersonalBests();
                },
                error -> Toast.makeText(this, "Failed to add personal best", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showUpdateDialog(PersonalBest pb) {
        ViewWrapper wrapper = new ViewWrapper(this);

        wrapper.etExercise.setText(pb.exerciseName);
        wrapper.etValue.setText(String.valueOf(pb.bestValue));
        wrapper.etUnit.setText(pb.unit);
        wrapper.etDate.setText(pb.recordDate);

        new AlertDialog.Builder(this)
                .setTitle("Update Personal Best")
                .setView(wrapper.view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String exerciseName = wrapper.etExercise.getText().toString().trim();
                    String valueText = wrapper.etValue.getText().toString().trim();
                    String unit = wrapper.etUnit.getText().toString().trim();
                    String recordDate = wrapper.etDate.getText().toString().trim();

                    if (exerciseName.isEmpty() || valueText.isEmpty() || unit.isEmpty() || recordDate.isEmpty()) {
                        Toast.makeText(this, "Fill out all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePersonalBest(pb.id, exerciseName, Double.parseDouble(valueText), unit, recordDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePersonalBest(int id, String exerciseName, double bestValue, String unit, String recordDate) {
        String url = BASE_URL + "/api/v1/personalbests/" + id;

        JSONObject body = new JSONObject();
        try {
            body.put("exerciseName", exerciseName);
            body.put("bestValue", bestValue);
            body.put("unit", unit);
            body.put("recordDate", recordDate);
        } catch (JSONException e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Personal best updated", Toast.LENGTH_SHORT).show();
                    loadPersonalBests();
                },
                error -> Toast.makeText(this, "Failed to update personal best", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void deletePersonalBest(int id) {
        String url = BASE_URL + "/api/v1/personalbests/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Personal best deleted", Toast.LENGTH_SHORT).show();
                    loadPersonalBests();
                },
                error -> Toast.makeText(this, "Failed to delete personal best", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void clearInputs() {
        etExerciseName.setText("");
        etBestValue.setText("");
        etUnit.setText("");
        etDate.setText("");
    }

    private static class ViewWrapper {
        android.view.View view;
        EditText etExercise, etValue, etUnit, etDate;

        ViewWrapper(android.content.Context context) {
            android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(32, 16, 32, 16);

            etExercise = new EditText(context);
            etExercise.setHint("Exercise Name");

            etValue = new EditText(context);
            etValue.setHint("Best Value");
            etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

            etUnit = new EditText(context);
            etUnit.setHint("Unit");

            etDate = new EditText(context);
            etDate.setHint("YYYY-MM-DD");

            layout.addView(etExercise);
            layout.addView(etValue);
            layout.addView(etUnit);
            layout.addView(etDate);

            view = layout;
        }
    }
}
