package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PersonalBestAdapter extends RecyclerView.Adapter<PersonalBestAdapter.ViewHolder> {

    public interface PersonalBestClickListener {
        void onUpdate(PersonalBest pb);
        void onDelete(PersonalBest pb);
    }

    private List<PersonalBest> personalBests;
    private PersonalBestClickListener listener;

    public PersonalBestAdapter(List<PersonalBest> personalBests, PersonalBestClickListener listener) {
        this.personalBests = personalBests;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExercise, tvValue, tvDate;
        Button btnUpdate, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvExercise = itemView.findViewById(R.id.tvExercise);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public PersonalBestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_best_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonalBestAdapter.ViewHolder holder, int position) {
        PersonalBest pb = personalBests.get(position);

        holder.tvExercise.setText(pb.exerciseName);
        holder.tvValue.setText(pb.bestValue + " " + pb.unit);
        holder.tvDate.setText(pb.recordDate);

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(pb));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(pb));
    }

    @Override
    public int getItemCount() {
        return personalBests.size();
    }
}