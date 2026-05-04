package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.ImageView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.toolbox.StringRequest;

import android.util.Log;


import com.android.volley.AuthFailureError;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for deleting user.
 */
public class DeleteUserActivity extends AppCompatActivity {


    private EditText usernameEditText;  // define username edittext variable
    private EditText dobEditText;       // define dob edittext variable
    private EditText userIdEditText;

    private static final String DELETE_URL =
            "http://coms-3090-035.class.las.iastate.edu:8080/api/v1/users";

    private static final String DELETE_BY_INFO_URL =
            "http://coms-3090-035.class.las.iastate.edu:8080/api/v1/users/delete";

    /**
     * Initializes delete user activity. Sets up UI elements and click listeners.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);
        AppTopBarController.setup(this);

        usernameEditText = findViewById(R.id.delete_username_edt);
        dobEditText = findViewById(R.id.delete_dob_edt);

        Button deleteButton = findViewById(R.id.delete_user_btn);

        userIdEditText = findViewById(R.id.delete_userid_edt);

        Button deleteByIdButton = findViewById(R.id.delete_userid_btn);

        deleteByIdButton.setOnClickListener(v -> {
            String userId = userIdEditText.getText().toString().trim();
            if(userId.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a user ID", Toast.LENGTH_LONG).show();
                return;
            }
            sendDeleteByIdRequest(userId);
        });


        deleteButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String dob = dobEditText.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG).show();
                return;
            }
            if (username.contains(" ")) {
                Toast.makeText(getApplicationContext(), "Username cannot contain spaces", Toast.LENGTH_LONG).show();
                return;
            }
            if (dob.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter your date of birth", Toast.LENGTH_LONG).show();
                return;
            }
            if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
                Toast.makeText(getApplicationContext(), "DOB must be MM/DD/YYYY", Toast.LENGTH_LONG).show();
                return;
            }
            sendDeleteRequest(username, dob);
        });
    }
    /**
     * Sends delete request to backend by username and dob
     * @param username String
     * @param dob String
     */
    private void sendDeleteRequest(String username, String dob) {
        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("dateOfBirth", dob);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DELETE_BY_INFO_URL,
                body,
                response -> {
                    Toast.makeText(getApplicationContext(),
                            "Deleted (username + DOB): " + response.toString(),
                            Toast.LENGTH_LONG).show();
                },
                error -> {
                    Toast.makeText(getApplicationContext(),
                            "Delete failed: " + readableVolleyError(error),
                            Toast.LENGTH_LONG).show();
                }
        ) {
            /**
             * Sets content type to JSON
             * @return
             */
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Sends delete request to backend by ID
     * @param idStr userId as a string
     */
    private void sendDeleteByIdRequest(String idStr) {
        String url = DELETE_URL + "/" + idStr;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {

                    Toast.makeText(getApplicationContext(),
                            "Deleted (ID). Response: " + (response == null ? "" : response),
                            Toast.LENGTH_LONG).show();
                },
                error -> {
                    Toast.makeText(getApplicationContext(),
                            "Delete failed: " +readableVolleyError(error),
                            Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Converts Volley error to readable string
     * @param error
     * @return String VolleyError
     */
    private String readableVolleyError(VolleyError error) {
            if(error == null) return "Unknown error";
            if(error.networkResponse != null) {
                return "HTTP " + error.networkResponse.statusCode;
            }
            else {
                return error.toString();
            }


    }
}