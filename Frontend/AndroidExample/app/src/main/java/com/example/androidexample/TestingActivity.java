package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class TestingActivity extends AppCompatActivity {

    private Button btnSetTrainer;
    private Button btnSetAdmin;
    private Button btnSetClient;
    private TextView tvResult;

    private KeepUser keepUser;

    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        AppTopBarController.setup(this);

        btnSetTrainer = findViewById(R.id.btnSetTrainer);
        btnSetAdmin = findViewById(R.id.btnSetAdmin);
        btnSetClient = findViewById(R.id.btnSetClient);
        tvResult = findViewById(R.id.tvResult);

        keepUser = new KeepUser(this);

        btnSetTrainer.setOnClickListener(v -> updateRole("set-trainer"));
        btnSetAdmin.setOnClickListener(v -> updateRole("set-admin"));
        btnSetClient.setOnClickListener(v -> updateRole("set-client"));
    }

    private void updateRole(String roleEndpoint) {
        try {
            int userId = keepUser.getUserId();

            if (userId == -1) {
                tvResult.setText("No logged in user found");
                return;
            }

            String url = BASE_URL + "/api/v1/users/" + userId + "/" + roleEndpoint;

            JSONObject body = new JSONObject(); // empty body

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    body,
                    response -> {
                        tvResult.setText(response.toString());

                        try {
                            int updatedUserId = response.getInt("userId");
                            String updatedRole = response.getString("role");
                            String currentUsername = keepUser.getUsername();

                            keepUser.saveUser(updatedUserId, currentUsername, updatedRole);

                        } catch (Exception e) {
                            e.printStackTrace();
                            tvResult.setText("Response parsing error");
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            tvResult.setText("Error: " + error.networkResponse.statusCode);
                        } else {
                            tvResult.setText("Network error: " + error.toString());
                        }
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("Unexpected error");
        }
    }
}