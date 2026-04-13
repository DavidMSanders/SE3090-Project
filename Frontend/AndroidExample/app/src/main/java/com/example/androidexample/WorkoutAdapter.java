package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Change this when switching from mock to real backendhttps://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String WORKOUTS_URL = BASE_URL + "/api/v1/workouts";
    private static final String ASSIGNED_WORKOUTS_URL = BASE_URL + "/api/v1/assigned-workouts";

    // Two row types
    private static final int TYPE_SELF = 1;
    private static final int TYPE_ASSIGNED = 2;

    private final Context context;
    private final List<Workout> workoutList;

    public WorkoutAdapter(Context context, List<Workout> workoutList) {
        this.context = context;
        this.workoutList = workoutList;
    }

    @Override
    public int getItemViewType(int position) {
        Workout workout = workoutList.get(position);
        return workout.isAssignedFromPlan() ? TYPE_ASSIGNED : TYPE_SELF;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_ASSIGNED) {
            View view = inflater.inflate(R.layout.workout_box_assigned, parent, false);
            return new AssignedWorkoutViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.workout_box, parent, false);
            return new SelfWorkoutViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Workout workout = workoutList.get(position);

        if (holder instanceof SelfWorkoutViewHolder) {
            bindSelfWorkout((SelfWorkoutViewHolder) holder, workout);
        } else if (holder instanceof AssignedWorkoutViewHolder) {
            bindAssignedWorkout((AssignedWorkoutViewHolder) holder, workout);
        }
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }


    // SELF-CREATED WORKOUT ROW

    private void bindSelfWorkout(@NonNull SelfWorkoutViewHolder holder, @NonNull Workout workout) {
        holder.editName.setText(workout.getWorkoutName());
        holder.editDate.setText(workout.getScheduledDate());
        holder.editDescription.setText(workout.getDescription());

        Integer duration = workout.getDurationMinutes();
        holder.txtDuration.setText(duration == null ? "0" : String.valueOf(duration));

        holder.txtSourceType.setText("Created by you");
        holder.txtStatus.setText("Status: " + workout.getStatus());

        holder.btnDurationMinus.setOnClickListener(v -> {
            Integer current = workout.getDurationMinutes();
            if (current == null) current = 0;

            if (current > 0) {
                current--;
                workout.setDurationMinutes(current);
                holder.txtDuration.setText(String.valueOf(current));
            }
        });

        holder.btnDurationPlus.setOnClickListener(v -> {
            Integer current = workout.getDurationMinutes();
            if (current == null) current = 0;

            if (current < 300) {
                current++;
                workout.setDurationMinutes(current);
                holder.txtDuration.setText(String.valueOf(current));
            }
        });

        holder.btnSave.setOnClickListener(v -> {
            String name = holder.editName.getText().toString().trim();
            String date = holder.editDate.getText().toString().trim();
            String desc = holder.editDescription.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_LONG).show();
                return;
            }

            workout.setWorkoutName(name);
            workout.setScheduledDate(date);
            workout.setDescription(desc);

            updateWorkout(workout);
        });

        holder.btnDelete.setOnClickListener(v -> deleteWorkout(workout, holder.getAdapterPosition()));
    }


    // ASSIGNED WORKOUT ROW

    private void bindAssignedWorkout(@NonNull AssignedWorkoutViewHolder holder, @NonNull Workout workout) {
        // Red background for assigned workouts
/*        holder.cardAssignedWorkout.setCardBackgroundColor(
                context.getResources().getColor(android.R.color.holo_red_light)
        );*/

        holder.txtName.setText(workout.getWorkoutName());
        holder.txtDate.setText(workout.getScheduledDate());
        holder.txtDescription.setText(workout.getDescription());

        Integer duration = workout.getDurationMinutes();
        //holder.txtDuration.setText(duration == null ? "-" : String.valueOf(duration));

        holder.txtSourceType.setText("Assigned by trainer");
        holder.txtStatus.setText("Status: " + workout.getStatus());

        String status = workout.getStatus() == null ? "" : workout.getStatus().toLowerCase();

        // Hide all status buttons first because RecyclerView reuses rows
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnComplete.setVisibility(View.GONE);

        if ("assigned".equals(status)) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else if ("accepted".equals(status) || "in_progress".equals(status)) {
            holder.btnComplete.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v ->
                updateAssignedWorkoutStatus(workout, "accepted", holder.getAdapterPosition()));

        holder.btnReject.setOnClickListener(v ->
                updateAssignedWorkoutStatus(workout, "rejected", holder.getAdapterPosition()));

        holder.btnComplete.setOnClickListener(v ->
                updateAssignedWorkoutStatus(workout, "completed", holder.getAdapterPosition()));
    }


    // UPDATE SELF WORKOUT

    private void updateWorkout(Workout workout) {
        String url = WORKOUTS_URL + "/" + workout.getWorkoutId();

        JSONObject body;
        try {
            body = workout.toUpdateJson();
        } catch (JSONException e) {
            Toast.makeText(context, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> Toast.makeText(context, "Workout updated!", Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(context, "Update failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    // DELETE SELF WORKOUT

    private void deleteWorkout(Workout workout, int position) {
        if (position == RecyclerView.NO_POSITION) return;

        String url = WORKOUTS_URL + "/" + workout.getWorkoutId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    workoutList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Workout deleted!", Toast.LENGTH_LONG).show();
                },
                error -> Toast.makeText(context, "Delete failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    // UPDATE ASSIGNED WORKOUT STATUS

    private void updateAssignedWorkoutStatus(Workout workout, String newStatus, int position) {
        if (position == RecyclerView.NO_POSITION) return;

        Integer assignedWorkoutId = workout.getAssignedWorkoutId();
        if (assignedWorkoutId == null) {
            Toast.makeText(context, "Missing assignedWorkoutId", Toast.LENGTH_LONG).show();
            return;
        }

        String url = ASSIGNED_WORKOUTS_URL + "/" + assignedWorkoutId + "/status";

        JSONObject body = new JSONObject();
        try {
            body.put("status", newStatus);
        } catch (JSONException e) {
            Toast.makeText(context, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    // Update local workout object too
                    workout.setStatus(newStatus);

                    // If rejected, remove it from the visible list
                    if ("rejected".equalsIgnoreCase(newStatus)) {
                        workoutList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Workout rejected", Toast.LENGTH_LONG).show();
                    } else {
                        notifyItemChanged(position);
                        Toast.makeText(context, "Status updated to " + newStatus, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(context, "Assigned workout update failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    // SELF VIEW HOLDER

    static class SelfWorkoutViewHolder extends RecyclerView.ViewHolder {
        EditText editName, editDate, editDescription;
        TextView txtDuration, txtStatus, txtSourceType;
        Button btnSave, btnDelete, btnDurationMinus, btnDurationPlus;

        public SelfWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);

            editName = itemView.findViewById(R.id.itemWorkoutName);
            editDate = itemView.findViewById(R.id.itemWorkoutDate);
            editDescription = itemView.findViewById(R.id.itemWorkoutDescription);

            txtDuration = itemView.findViewById(R.id.itemWorkoutDuration);
            txtStatus = itemView.findViewById(R.id.itemWorkoutStatus);
            txtSourceType = itemView.findViewById(R.id.itemWorkoutSourceType);

            btnSave = itemView.findViewById(R.id.btnItemSave);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);
            btnDurationMinus = itemView.findViewById(R.id.btnDurationMinus);
            btnDurationPlus = itemView.findViewById(R.id.btnDurationPlus);
        }
    }


    // ASSIGNED VIEW HOLDER

    static class AssignedWorkoutViewHolder extends RecyclerView.ViewHolder {
        CardView cardAssignedWorkout;
//, txtDuration
        TextView txtName, txtDate, txtDescription;
        TextView txtSourceType, txtStatus;

        Button btnAccept, btnReject, btnComplete;

        public AssignedWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);

            cardAssignedWorkout = itemView.findViewById(R.id.cardAssignedWorkout);

            txtName = itemView.findViewById(R.id.txtAssignedWorkoutName);
            txtDate = itemView.findViewById(R.id.txtAssignedWorkoutDate);
            //txtDuration = itemView.findViewById(R.id.txtAssignedWorkoutDuration);
            txtDescription = itemView.findViewById(R.id.txtAssignedWorkoutDescription);

            txtSourceType = itemView.findViewById(R.id.txtAssignedWorkoutSourceType);
            txtStatus = itemView.findViewById(R.id.txtAssignedWorkoutStatus);

            btnAccept = itemView.findViewById(R.id.btnAssignedAccept);
            btnReject = itemView.findViewById(R.id.btnAssignedReject);
            btnComplete = itemView.findViewById(R.id.btnAssignedComplete);
        }
    }
}




