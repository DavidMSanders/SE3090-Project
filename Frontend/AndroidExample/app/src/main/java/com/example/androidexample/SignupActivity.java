package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.ImageView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable

    private EditText dobEditText;       // define dob edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    //toDO mach port/path to backend

    private static final String SIGNUP_URL = "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io/api/v1/auth/signup";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */


        usernameEditText = findViewById(R.id.signup_username_edt);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password_edt);  // link to password edtext in the Signup activity XML
        confirmEditText = findViewById(R.id.signup_confirm_edt);    // link to confirm edtext in the Signup activity XML

        dobEditText = findViewById(R.id.signup_dob_edt);

        loginButton = findViewById(R.id.signup_login_btn);    // link to login button in the Signup activity XML
        signupButton = findViewById(R.id.signup_signup_btn);  // link to signup button in the Signup activity XML

        /* click listener on login button pressed -- UNEDITED CURRENTLY JUST TAKES BACK TO MAIN*/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);  // go to LoginActivity
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();

                String dob = dobEditText.getText().toString().trim();

                ImageView confetti = findViewById(R.id.Confetti);


                // username validation
                if (username.isEmpty() ) {
                    Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_LONG).show();
                    return;
                }
                if(username.contains(" ")){
                    Toast.makeText(getApplicationContext(), "Username cannot contain spaces", Toast.LENGTH_LONG).show();
                    return;
                }
                if(username.length() < 5){
                    Toast.makeText(getApplicationContext(), "Username must be at least 5 characters", Toast.LENGTH_LONG).show();
                    return;
                }
                //password validation

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();
                    return;
                }
                if (password.contains(" ")){
                    Toast.makeText(getApplicationContext(), "Password cannot contain spaces", Toast.LENGTH_LONG).show();
                    return;
                }
                if(password.length() < 10){
                    Toast.makeText(getApplicationContext(), "Password must be at least 10 characters", Toast.LENGTH_LONG).show();
                    return;
                }
                //confirm validation

                //DOB VALIDATION
                if (dob.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your date of birth", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    Toast.makeText(getApplicationContext(), "DOB must be MM/DD/YYYY", Toast.LENGTH_LONG).show();
                    return;
                }



                if (password.equals(confirm)){
                    Toast.makeText(getApplicationContext(), "Signing up", Toast.LENGTH_LONG).show();
                    confetti.setVisibility(View.VISIBLE);
                    confetti.postDelayed(() -> {
                        confetti.setVisibility(View.GONE);
                    }, 3000);

                    //Post request (signup)
                    sendSignupRequest(username, password, dob);

                }
                else {
                    Toast.makeText(getApplicationContext(), "Password don't match", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    private void sendSignupRequest(String username, String password, String dob) {
        JSONObject body = new JSONObject();
        try{
            body.put("username", username);
            body.put("password", password);
            body.put("dateOfBirth", dob);
        } catch (JSONException e){
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SIGNUP_URL, body, response -> {
            //success
            Toast.makeText(getApplicationContext(), "Signup successful", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
        }, error -> {
            //failure
            Toast.makeText(getApplicationContext(), "Signup failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
        ){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private String readableVolleyError(VolleyError error) {
        if (error == null) return "Unknown error";
        if(error.networkResponse != null) {
            return "HTTP " + error.networkResponse.statusCode;
        }
        else {
            return error.toString();
        }
    }
}