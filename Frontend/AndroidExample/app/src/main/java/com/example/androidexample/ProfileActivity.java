package com.example.androidexample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AppTopBarController.setup(this);

        TextView tvProfileUserId = findViewById(R.id.tvProfileUserId);
        String userId = new KeepUser(this).getUserIdString();
        if (userId.isEmpty()) {
            userId = SessionManager.getUserId(this);
        }
        tvProfileUserId.setText(userId.isEmpty() ? "User ID: Not available" : "User ID: " + userId);
    }
}
