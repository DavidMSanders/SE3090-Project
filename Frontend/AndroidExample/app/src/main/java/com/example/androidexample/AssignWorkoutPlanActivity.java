package com.example.androidexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AssignWorkoutPlanActivity extends AppCompatActivity {

    private Spinner spinnerGroups;
    private Spinner spinnerClients;
    private Spinner spinnerWorkoutPlans;
    private EditText etScheduledDate;
    private Button btnAssignWorkoutPlan;

    //http://coms-3090-035.class.las.iastate.edu:8080
    //hhtps
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080/api/v1";

    private int trainerId;

    private final List<GroupOption> groupOptions = new ArrayList<>();
    private final List<UserOption> clientOptions = new ArrayList<>();
    private final List<WorkoutPlanOption> workoutPlanOptions = new ArrayList<>();

    private ArrayAdapter<GroupOption> groupAdapter;
    private ArrayAdapter<UserOption> clientAdapter;
    private ArrayAdapter<WorkoutPlanOption> workoutPlanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_workout_plan);

        spinnerGroups = findViewById(R.id.spinnerGroups);
        spinnerClients = findViewById(R.id.spinnerClients);
        spinnerWorkoutPlans = findViewById(R.id.spinnerWorkoutPlans);
        etScheduledDate = findViewById(R.id.etScheduledDate);
        btnAssignWorkoutPlan = findViewById(R.id.btnAssignWorkoutPlan);

        KeepUser keepUser = new KeepUser(this);
        trainerId = keepUser.getUserId();

        setupAdapters();
        setupDatePicker();

        loadAssignableGroups();
        loadWorkoutPlans();

        spinnerGroups.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            GroupOption selectedGroup = getSelectedGroup();
            if (selectedGroup != null && selectedGroup.getGroupId() != -1) {
                loadAssignableClients(selectedGroup.getGroupId());
            } else {
                clearClients();
            }
        }));

        btnAssignWorkoutPlan.setOnClickListener(v -> assignWorkoutPlan());
    }

    private void setupAdapters() {
        groupOptions.add(new GroupOption(-1, "Select Group"));
        clientOptions.add(new UserOption(-1, "Select Client"));
        workoutPlanOptions.add(new WorkoutPlanOption(-1, "Select Workout Plan"));

        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupOptions);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroups.setAdapter(groupAdapter);

        clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientOptions);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClients.setAdapter(clientAdapter);

        workoutPlanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutPlanOptions);
        workoutPlanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkoutPlans.setAdapter(workoutPlanAdapter);
    }

    private void setupDatePicker() {
        etScheduledDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDay
                        );
                        etScheduledDate.setText(formattedDate);
                    },
                    year,
                    month,
                    day
            );
            dialog.show();
        });
    }

    private void loadAssignableGroups() {
        String url = BASE_URL + "/users/" + trainerId + "/assignable-groups";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    groupOptions.clear();
                    groupOptions.add(new GroupOption(-1, "Select Group"));

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int groupId = obj.optInt("groupId");
                            String groupName = obj.optString("groupName");
                            groupOptions.add(new GroupOption(groupId, groupName));
                        } catch (Exception e) {
                            Toast.makeText(this, "Error parsing groups", Toast.LENGTH_SHORT).show();
                        }
                    }
                    groupAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadAssignableClients(int groupId) {
        String url = BASE_URL + "/groups/" + groupId + "/assignable-clients?trainerId=" + trainerId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    clientOptions.clear();
                    clientOptions.add(new UserOption(-1, "Select Client"));

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int userId = obj.optInt("userId");
                            String username = obj.optString("username");
                            clientOptions.add(new UserOption(userId, username));
                        } catch (Exception e) {
                            Toast.makeText(this, "Error parsing clients", Toast.LENGTH_SHORT).show();
                        }
                    }
                    clientAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load clients", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadWorkoutPlans() {
        String url = BASE_URL + "/workout-plans/trainer/" + trainerId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    workoutPlanOptions.clear();
                    workoutPlanOptions.add(new WorkoutPlanOption(-1, "Select Workout Plan"));

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int workoutPlanId = obj.optInt("workoutPlanId");
                            String planName = obj.optString("planName");
                            workoutPlanOptions.add(new WorkoutPlanOption(workoutPlanId, planName));
                        } catch (Exception e) {
                            Toast.makeText(this, "Error parsing workout plans", Toast.LENGTH_SHORT).show();
                        }
                    }
                    workoutPlanAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load workout plans", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void assignWorkoutPlan() {
        GroupOption selectedGroup = getSelectedGroup();
        UserOption selectedClient = getSelectedClient();
        WorkoutPlanOption selectedPlan = getSelectedWorkoutPlan();
        String scheduledDate = etScheduledDate.getText().toString().trim();

        if (selectedGroup == null || selectedGroup.getGroupId() == -1) {
            Toast.makeText(this, "Please select a group", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedClient == null || selectedClient.getUserId() == -1) {
            Toast.makeText(this, "Please select a client", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPlan == null || selectedPlan.getWorkoutPlanId() == -1) {
            Toast.makeText(this, "Please select a workout plan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (scheduledDate.isEmpty()) {
            Toast.makeText(this, "Please choose a scheduled date", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/assigned-workouts";

        try {
            JSONObject body = new JSONObject();
            body.put("trainerId", trainerId);
            body.put("workoutPlanId", selectedPlan.getWorkoutPlanId());
            body.put("clientId", selectedClient.getUserId());
            body.put("groupId", selectedGroup.getGroupId());
            body.put("scheduledDate", scheduledDate);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Workout assigned successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Failed to assign workout", Toast.LENGTH_SHORT).show()
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error building assign request", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearClients() {
        clientOptions.clear();
        clientOptions.add(new UserOption(-1, "Select Client"));
        clientAdapter.notifyDataSetChanged();
    }

    private GroupOption getSelectedGroup() {
        Object selected = spinnerGroups.getSelectedItem();
        return selected instanceof GroupOption ? (GroupOption) selected : null;
    }

    private UserOption getSelectedClient() {
        Object selected = spinnerClients.getSelectedItem();
        return selected instanceof UserOption ? (UserOption) selected : null;
    }

    private WorkoutPlanOption getSelectedWorkoutPlan() {
        Object selected = spinnerWorkoutPlans.getSelectedItem();
        return selected instanceof WorkoutPlanOption ? (WorkoutPlanOption) selected : null;
    }

    // ---------- helper model classes ----------

    private static class GroupOption {
        private final int groupId;
        private final String groupName;

        GroupOption(int groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }

        int getGroupId() {
            return groupId;
        }

        @Override
        public String toString() {
            return groupName;
        }
    }

    private static class UserOption {
        private final int userId;
        private final String username;

        UserOption(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        int getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return username;
        }
    }

    private static class WorkoutPlanOption {
        private final int workoutPlanId;
        private final String planName;

        WorkoutPlanOption(int workoutPlanId, String planName) {
            this.workoutPlanId = workoutPlanId;
            this.planName = planName;
        }

        int getWorkoutPlanId() {
            return workoutPlanId;
        }

        @Override
        public String toString() {
            return planName;
        }
    }
}
