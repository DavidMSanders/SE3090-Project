package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutPlanSummaryAdapter extends RecyclerView.Adapter<WorkoutPlanSummaryAdapter.SummaryViewHolder> {

    private final List<WorkoutPlanSummary> summaryList;

    public WorkoutPlanSummaryAdapter(List<WorkoutPlanSummary> summaryList) {
        this.summaryList = summaryList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_plan_summary_box, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        WorkoutPlanSummary summary = summaryList.get(position);

        holder.txtPlanName.setText(summary.getPlanName());
        holder.txtPlanId.setText("Plan ID: " + summary.getPlanId());

        holder.txtCounts.setText(
                "Accepted: " + summary.getAcceptedCount()
                        + " | Rejected: " + summary.getRejectedCount()
                        + " | Completed: " + summary.getCompletedCount()
                        + " | Pending: " + summary.getPendingCount()
        );

        holder.txtAcceptedUsers.setText("Accepted: " + formatUsers(summary.getAcceptedUsers()));
        holder.txtRejectedUsers.setText("Rejected: " + formatUsers(summary.getRejectedUsers()));
        holder.txtCompletedUsers.setText("Completed: " + formatUsers(summary.getCompletedUsers()));
        holder.txtPendingUsers.setText("Pending: " + formatUsers(summary.getPendingUsers()));
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    private String formatUsers(List<String> users) {
        if (users == null || users.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < users.size(); i++) {
            builder.append(users.get(i));

            if (i < users.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    public static class SummaryViewHolder extends RecyclerView.ViewHolder {

        TextView txtPlanName;
        TextView txtPlanId;
        TextView txtCounts;
        TextView txtAcceptedUsers;
        TextView txtRejectedUsers;
        TextView txtCompletedUsers;
        TextView txtPendingUsers;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPlanName = itemView.findViewById(R.id.txtPlanName);
            txtPlanId = itemView.findViewById(R.id.txtPlanId);
            txtCounts = itemView.findViewById(R.id.txtCounts);
            txtAcceptedUsers = itemView.findViewById(R.id.txtAcceptedUsers);
            txtRejectedUsers = itemView.findViewById(R.id.txtRejectedUsers);
            txtCompletedUsers = itemView.findViewById(R.id.txtCompletedUsers);
            txtPendingUsers = itemView.findViewById(R.id.txtPendingUsers);
        }
    }
}