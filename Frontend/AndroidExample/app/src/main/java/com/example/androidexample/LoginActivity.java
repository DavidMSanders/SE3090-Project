package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String LOGIN_URL = BASE_URL + "/api/v1/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter username and password", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Attempting login", Toast.LENGTH_LONG).show();
                loginButton.setEnabled(false);

                // Post request (login)
                sendLoginRequest(username, password);
            }
        });
    }


    private void sendLoginRequest(String username, String password) {
        JSONObject body = new JSONObject();
        try {
            // Include both key variants so the app can work with either backend naming style.
            body.put("username", username);
            body.put("password", password);
            body.put("accountName", username);
            body.put("userPassword", password);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, body, response -> {
            loginButton.setEnabled(true);

            boolean loginOk = extractLoginSuccess(response);
            String userId = extractUserId(response);
            String responseUsername = extractUsername(response, username);
            String role = response.optString("role", "normal");

            if (loginOk && !"000".equals(userId) && !userId.isEmpty()) {
                SessionManager.saveLoggedInUser(this, responseUsername, userId);
                try {
                    new KeepUser(this).saveUser(Integer.parseInt(userId), responseUsername, role);
                } catch (NumberFormatException ignored) {
                    new KeepUser(this).saveUser(-1, responseUsername, role);
                }
                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_LONG).show();
            }
        }, error -> {
            loginButton.setEnabled(true);
            String errorText = readableVolleyError(error);
            if ("login failed".equalsIgnoreCase(errorText.trim())) {
                Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Login failed: " + errorText, Toast.LENGTH_LONG).show();
            }
        }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private boolean extractLoginSuccess(JSONObject response) {
        if (response == null) {
            return false;
        }

        Object status = response.opt("status");
        if (status instanceof Boolean) {
            return (Boolean) status;
        }
        if (status instanceof String) {
            return "true".equalsIgnoreCase(((String) status).trim());
        }

        String message = response.optString("message", "").trim();
        if ("login successful".equalsIgnoreCase(message)) {
            return true;
        }

        return !extractUserId(response).isEmpty();
    }

    private String extractUsername(JSONObject response, String fallbackUsername) {
        if (response == null) {
            return fallbackUsername;
        }

        String backendUsername = response.optString("username", "").trim();
        if (!backendUsername.isEmpty()) {
            return backendUsername;
        }

        String backendAccountName = response.optString("accountName", "").trim();
        return backendAccountName.isEmpty() ? fallbackUsername : backendAccountName;
    }

    private String extractUserId(JSONObject response) {
        if (response == null) {
            return "";
        }

        Object userId = response.opt("userId");
        return userId == null ? "" : String.valueOf(userId).trim();
    }

    private String readableVolleyError(VolleyError error) {
        if (error == null) {
            return "Unknown error";
        }

        if (error.networkResponse != null) {
            String body = "";
            if (error.networkResponse.data != null && error.networkResponse.data.length > 0) {
                body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            }

            if (!body.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(body);
                    String message = json.optString("message", "").trim();
                    if (message.isEmpty()) {
                        message = json.optString("error", "").trim();
                    }
                    if (message.isEmpty()) {
                        message = json.optString("details", "").trim();
                    }
                    if (!message.isEmpty()) {
                        return message;
                    }
                } catch (JSONException ignored) {
                    return body;
                }
            }

            return "HTTP " + error.networkResponse.statusCode;
        }

        if (error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
            return error.getMessage();
        }

        return error.toString();
    }
}