package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String TAG = "ProfileActivity";

    // ---- session ----
    private long currentUserId;
    private String currentRole;

    // ---- user profile views ----
    private TextView tvCurrentUsername;
    private TextView tvCurrentRole;
    private TextView tvCurrentBirthDate;
    private TextView tvProfileUserId;
    private EditText etNewAccountName;
    private EditText etNewPassword;
    private EditText etNewBirthDate;
    private Button btnUpdateProfile;
    private Button btnGoToDeletePage;

    // ---- admin views ----
    private LinearLayout layoutAdminSection;
    private Button btnGetStats;
    private TextView tvStatsResult;
    private Button btnGetAllUsers;
    private TextView tvAllUsersResult;
    private EditText etTargetUserId;
    private Button btnGetUser;
    private TextView tvUserDetailResult;
    private EditText etAdminNewName;
    private EditText etAdminNewPassword;
    private EditText etAdminNewBirthDate;
    private Button btnAdminUpdateUser;
    private Button btnActivateUser;
    private Button btnSuspendUser;
    private Button btnDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AppTopBarController.setup(this);

        // Resolve current user
        KeepUser keepUser = new KeepUser(this);
        String userIdStr = keepUser.getUserIdString();
        if (userIdStr.isEmpty()) {
            userIdStr = SessionManager.getUserId(this);
        }
        currentUserId = userIdStr.isEmpty() ? 1 : Long.parseLong(userIdStr);
        currentRole = keepUser.getRoleOrDefault();

        // Bind user profile views
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        tvCurrentRole = findViewById(R.id.tvCurrentRole);
        tvCurrentBirthDate = findViewById(R.id.tvCurrentBirthDate);
        tvProfileUserId = findViewById(R.id.tvProfileUserId);
        etNewAccountName = findViewById(R.id.etNewAccountName);
        etNewPassword = findViewById(R.id.etNewPassword);
        etNewBirthDate = findViewById(R.id.etNewBirthDate);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnGoToDeletePage = findViewById(R.id.btnGoToDeletePage);

        // Bind admin views
        layoutAdminSection = findViewById(R.id.layoutAdminSection);
        btnGetStats = findViewById(R.id.btnGetStats);
        tvStatsResult = findViewById(R.id.tvStatsResult);
        btnGetAllUsers = findViewById(R.id.btnGetAllUsers);
        tvAllUsersResult = findViewById(R.id.tvAllUsersResult);
        etTargetUserId = findViewById(R.id.etTargetUserId);
        btnGetUser = findViewById(R.id.btnGetUser);
        tvUserDetailResult = findViewById(R.id.tvUserDetailResult);
        etAdminNewName = findViewById(R.id.etAdminNewName);
        etAdminNewPassword = findViewById(R.id.etAdminNewPassword);
        etAdminNewBirthDate = findViewById(R.id.etAdminNewBirthDate);
        btnAdminUpdateUser = findViewById(R.id.btnAdminUpdateUser);
        btnActivateUser = findViewById(R.id.btnActivateUser);
        btnSuspendUser = findViewById(R.id.btnSuspendUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        // Populate current user info labels with cached session values first.
        applyProfileSummary(keepUser.getDisplayUsername(), currentRole, null, currentUserId);
        fetchOwnProfile();

        // Show admin section only for admins
        if (isAdmin()) {
            layoutAdminSection.setVisibility(View.VISIBLE);
        }

        // Listeners
        btnUpdateProfile.setOnClickListener(v -> updateOwnProfile());
        btnGoToDeletePage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DeleteUserActivity.class);
            startActivity(intent);
        });

        if (isAdmin()) {
            btnGetStats.setOnClickListener(v -> fetchAppStats());
            btnGetAllUsers.setOnClickListener(v -> fetchAllUsers());
            btnGetUser.setOnClickListener(v -> fetchUserDetails());
            btnAdminUpdateUser.setOnClickListener(v -> adminUpdateUser());
            btnActivateUser.setOnClickListener(v -> setUserStatus(true));
            btnSuspendUser.setOnClickListener(v -> setUserStatus(false));
            btnDeleteUser.setOnClickListener(v -> confirmDeleteUser());
        }
    }

    private boolean isAdmin() {
        return currentRole != null && currentRole.toLowerCase().contains("admin");
    }

    // -------------------------------------------------------------------------
    // USER: PUT /api/v1/users/{userId}/profile
    // -------------------------------------------------------------------------
    private void updateOwnProfile() {
        String newName = etNewAccountName.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String newBirth = etNewBirthDate.getText().toString().trim();

        if (newName.isEmpty() && newPass.isEmpty() && newBirth.isEmpty()) {
            Toast.makeText(this, "Enter at least one field to update", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            if (!newName.isEmpty())  body.put("accountName", newName);
            if (!newPass.isEmpty())  body.put("userPassword", newPass);
            if (!newBirth.isEmpty()) body.put("birthDate", newBirth);
        } catch (JSONException e) {
            Log.e(TAG, "JSON build error", e);
            return;
        }

        String url = BASE_URL + "/api/v1/users/" + currentUserId + "/profile";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Log.d(TAG, "Profile updated: " + response);
                    applyProfileFromResponse(response);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    etNewAccountName.setText("");
                    etNewPassword.setText("");
                    etNewBirthDate.setText("");
                },
                error -> {
                    Log.e(TAG, "Profile update error", error);
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    private void fetchOwnProfile() {
        String url = BASE_URL + "/api/v1/users/" + currentUserId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Loaded profile: " + response);
                    applyProfileJson(response);
                },
                error -> Log.e(TAG, "Profile fetch error", error));
        Volley.newRequestQueue(this).add(req);
    }

    private void applyProfileFromResponse(JSONObject response) {
        JSONObject user = response.optJSONObject("user");
        applyProfileJson(user != null ? user : response);
    }

    private void applyProfileJson(JSONObject profileJson) {
        if (profileJson == null) {
            return;
        }

        String username = profileJson.optString("accountName",
                profileJson.optString("username", tvCurrentUsername.getText().toString()));
        String role = profileJson.optString("role", currentRole);
        String birthDate = profileJson.optString("birthDate",
                profileJson.optString("dateOfBirth", ""));
        long userId = profileJson.optLong("id", profileJson.optLong("userId", currentUserId));

        currentRole = role;
        currentUserId = userId;

        syncStoredUser(username, role, userId);
        applyProfileSummary(username, role, birthDate, userId);
    }

    private void applyProfileSummary(String username, String role, String birthDate, long userId) {
        tvCurrentUsername.setText("Username: " + safeText(username, "-"));
        tvCurrentRole.setText("Role: " + safeText(role, "-"));
        tvCurrentBirthDate.setText("Birth Date: " + safeText(formatBirthDate(birthDate), "-"));
        tvProfileUserId.setText("User ID: " + userId);
    }

    private void syncStoredUser(String username, String role, long userId) {
        SessionManager.saveLoggedInUser(this, username, String.valueOf(userId));
        int storedUserId;
        try {
            storedUserId = Math.toIntExact(userId);
        } catch (ArithmeticException e) {
            storedUserId = -1;
        }
        new KeepUser(this).saveUser(storedUserId, username, role);
    }

    private String safeText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String formatBirthDate(String birthDate) {
        String normalizedBirthDate = safeText(birthDate, "");
        if (normalizedBirthDate.isEmpty()) {
            return "";
        }

        try {
            LocalDate parsedDate = LocalDate.parse(normalizedBirthDate);
            return parsedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } catch (DateTimeParseException ignored) {
            return normalizedBirthDate;
        }
    }

    // -------------------------------------------------------------------------
    // ADMIN: GET /api/v1/admin/stats?adminId={adminId}
    // -------------------------------------------------------------------------
    private void fetchAppStats() {
        String url = BASE_URL + "/api/v1/admin/stats?adminId=" + currentUserId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Stats: " + response);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Total Users: ").append(response.optInt("totalUsers", 0)).append("\n");
                    sb.append("Admins: ").append(response.optInt("totalAdmins", 0)).append("\n");
                    sb.append("Trainers: ").append(response.optInt("totalTrainers", 0)).append("\n");
                    sb.append("Clients: ").append(response.optInt("totalClients", 0)).append("\n");
                    sb.append("Active: ").append(response.optInt("activeUsers", 0)).append("\n");
                    sb.append("Suspended: ").append(response.optInt("suspendedUsers", 0));
                    tvStatsResult.setText(sb.toString());
                },
                error -> {
                    Log.e(TAG, "Stats error", error);
                    tvStatsResult.setText("Error fetching stats");
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // ADMIN: GET /api/v1/admin/users?adminId={adminId}
    // -------------------------------------------------------------------------
    private void fetchAllUsers() {
        String url = BASE_URL + "/api/v1/admin/users?adminId=" + currentUserId;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "All users: " + response);
                    StringBuilder sb = new StringBuilder();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject user = response.getJSONObject(i);
                            long id = user.optLong("id", user.optLong("userId", -1));
                            String name = user.optString("accountName", user.optString("username", "?"));
                            String role = user.optString("role", "?");
                            boolean enabled = user.optBoolean("enabled", true);
                            sb.append("ID:").append(id)
                              .append(" | ").append(name)
                              .append(" | ").append(role)
                              .append(enabled ? "" : " [SUSPENDED]")
                              .append("\n");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse error", e);
                        sb.append("Error parsing user list");
                    }
                    tvAllUsersResult.setText(sb.length() == 0 ? "No users found" : sb.toString());
                },
                error -> {
                    Log.e(TAG, "Get all users error", error);
                    tvAllUsersResult.setText("Error fetching users");
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // ADMIN: GET /api/v1/admin/users/{personId}?adminId={adminId}
    // -------------------------------------------------------------------------
    private void fetchUserDetails() {
        long targetId = getTargetUserId();
        if (targetId == -1) return;

        String url = BASE_URL + "/api/v1/admin/users/" + targetId + "?adminId=" + currentUserId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "User detail: " + response);
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID: ").append(response.optLong("id", response.optLong("userId", -1))).append("\n");
                    sb.append("Username: ").append(response.optString("accountName", response.optString("username", "?"))).append("\n");
                    sb.append("Role: ").append(response.optString("role", "?")).append("\n");
                    sb.append("Birth Date: ").append(response.optString("birthDate", "?")).append("\n");
                    sb.append("Enabled: ").append(response.optBoolean("enabled", true));
                    tvUserDetailResult.setText(sb.toString());
                },
                error -> {
                    Log.e(TAG, "Get user detail error", error);
                    tvUserDetailResult.setText("Error fetching user details");
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // ADMIN: PUT /api/v1/admin/users/{personId}?adminId={adminId}
    // -------------------------------------------------------------------------
    private void adminUpdateUser() {
        long targetId = getTargetUserId();
        if (targetId == -1) return;

        String newName = etAdminNewName.getText().toString().trim();
        String newPass = etAdminNewPassword.getText().toString().trim();
        String newBirth = etAdminNewBirthDate.getText().toString().trim();

        if (newName.isEmpty() && newPass.isEmpty() && newBirth.isEmpty()) {
            Toast.makeText(this, "Enter at least one field to update", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            if (!newName.isEmpty())  body.put("accountName", newName);
            if (!newPass.isEmpty())  body.put("userPassword", newPass);
            if (!newBirth.isEmpty()) body.put("birthDate", newBirth);
        } catch (JSONException e) {
            Log.e(TAG, "JSON build error", e);
            return;
        }

        String url = BASE_URL + "/api/v1/admin/users/" + targetId + "?adminId=" + currentUserId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Log.d(TAG, "Admin update user response: " + response);
                    Toast.makeText(this, "User " + targetId + " updated", Toast.LENGTH_SHORT).show();
                    etAdminNewName.setText("");
                    etAdminNewPassword.setText("");
                    etAdminNewBirthDate.setText("");
                },
                error -> {
                    Log.e(TAG, "Admin update user error", error);
                    Toast.makeText(this, "Error updating user", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // ADMIN: PUT /api/v1/admin/users/{personId}/status?adminId={adminId}&enabled={bool}
    // -------------------------------------------------------------------------
    private void setUserStatus(boolean enabled) {
        long targetId = getTargetUserId();
        if (targetId == -1) return;

        String url = BASE_URL + "/api/v1/admin/users/" + targetId
                + "/status?adminId=" + currentUserId
                + "&enabled=" + enabled;

        StringRequest req = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Log.d(TAG, "Status update response: " + response);
                    String msg = enabled ? "User " + targetId + " activated" : "User " + targetId + " suspended";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e(TAG, "Status update error", error);
                    Toast.makeText(this, "Error updating account status", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // ADMIN: DELETE /api/v1/admin/users/{personId}?adminId={adminId}
    // -------------------------------------------------------------------------
    private void confirmDeleteUser() {
        long targetId = getTargetUserId();
        if (targetId == -1) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Permanently delete user ID " + targetId + "? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(targetId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(long targetId) {
        String url = BASE_URL + "/api/v1/admin/users/" + targetId + "?adminId=" + currentUserId;
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Log.d(TAG, "Delete user response: " + response);
                    Toast.makeText(this, "User " + targetId + " deleted", Toast.LENGTH_SHORT).show();
                    etTargetUserId.setText("");
                    tvUserDetailResult.setText("");
                },
                error -> {
                    Log.e(TAG, "Delete user error", error);
                    Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------
    private long getTargetUserId() {
        String text = etTargetUserId.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
            return -1;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "User ID must be a number", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
}
