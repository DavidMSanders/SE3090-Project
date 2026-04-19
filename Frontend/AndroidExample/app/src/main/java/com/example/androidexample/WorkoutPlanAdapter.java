package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.WorkoutPlanViewHolder> {

    private final List<WorkoutPlan> workoutPlanList;
    private Context context;
//http://coms-3090-035.class.las.iastate.edu:8080
    //https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io
    private static final String BASE_URL = "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io";



    public WorkoutPlanAdapter(Context context,List<WorkoutPlan> workoutPlanList) {
        this.workoutPlanList = workoutPlanList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkoutPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_plan_box, parent, false);
        return new WorkoutPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutPlanViewHolder holder, int position) {
        WorkoutPlan plan = workoutPlanList.get(position);
        holder.tvPlanName.setText(plan.getpName());
        holder.tvPlanDescription.setText(plan.getpDescription());

        holder.btnDelete.setOnClickListener(v -> {
            int workoutPlanId = workoutPlanList.get(position).getWorkoutPlanId();

            deleteWorkoutPlan(workoutPlanId, position);
        });
    }

    @Override
    public int getItemCount() {
        return workoutPlanList.size();
    }

    static class WorkoutPlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanDescription;

        ImageButton btnDelete;

        public WorkoutPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanDescription = itemView.findViewById(R.id.tvPlanDescription);

            btnDelete = itemView.findViewById(R.id.btnDeleteWorkoutPlan);
        }
    }


    private void deleteWorkoutPlan(int workoutPlanId, int position) {

        String url = BASE_URL + "/api/v1/workout-plans/" + workoutPlanId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();

                    //remember to remove from list
                    workoutPlanList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, workoutPlanList.size());
                },
                error -> {
                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}
