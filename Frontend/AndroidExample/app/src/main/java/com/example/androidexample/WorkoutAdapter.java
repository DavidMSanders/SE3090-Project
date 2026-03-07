package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>{
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String WORKOUTS_URL = BASE_URL + "/api/v1/workouts";

    private final Context context;
    private final List<Workout> workoutList;

    public WorkoutAdapter(Context context, List<Workout> workoutList) {
        this.context = context;
        this.workoutList = workoutList;
    }
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workout_box, parent, false);
        return new WorkoutViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);

        holder.editName.setText(workout.getName());
        holder.editDate.setText(workout.getDate());
        holder.editDuration.setText(String.valueOf(workout.getDuration()));
        holder.editDescription.setText(workout.getDescription());
    holder.btnSave.setOnClickListener(v -> {


        String name = holder.editName.getText().toString().trim();
    String date = holder.editDate.getText().toString().trim();
    String durationStr = holder.editDuration.getText().toString().trim();
    String desc = holder.editDescription.getText().toString().trim();


    if(name.isEmpty() || date.isEmpty() || durationStr.isEmpty() || desc.isEmpty()){
        Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_LONG).show();
        return;
    }
    int duration;
    try{
        duration = Integer.parseInt(durationStr);
    } catch (NumberFormatException e){
        Toast.makeText(context, "Duration must be a number", Toast.LENGTH_LONG).show();
        return;
    }
    workout.setName(name);
    workout.setDate(date);
    workout.setDuration(duration);
    workout.setDescription(desc);

    updateWorkout(workout);


    });

    holder.btnDelete.setOnClickListener(v -> deleteWorkout(workout, holder.getAdapterPosition()));
}

@Override
public int getItemCount() {
        return workoutList.size();
    }
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        EditText editName, editDate, editDuration, editDescription;
        Button btnSave, btnDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            editName = itemView.findViewById(R.id.itemWorkoutName);
            editDate = itemView.findViewById(R.id.itemWorkoutDate);
            editDuration = itemView.findViewById(R.id.itemWorkoutDuration);
            editDescription = itemView.findViewById(R.id.itemWorkoutDescription);
            btnSave = itemView.findViewById(R.id.btnItemSave);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);

        }
    }
    private void updateWorkout(Workout workout) {
        String url = WORKOUTS_URL + "/" + workout.getID();

        JSONObject body;

        try {
            body = workout.toJson();

        } catch (JSONException e) {
            Toast.makeText(context, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response ->  Toast.makeText(context, "Workout updated!", Toast.LENGTH_LONG).show(),
                error ->  Toast.makeText(context, "Update failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
        private void deleteWorkout(Workout workout, int position) {
            if (position == RecyclerView.NO_POSITION) return;
            String url = WORKOUTS_URL + "/" + workout.getID();

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

    }




