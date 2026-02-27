package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateWorkoutActivity extends AppCompatActivity {
    private EditText editName, editDate, editDuration, editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        editName = findViewById(R.id.WorkoutName);
        editDate = findViewById(R.id.WorkoutDate);
        editDuration = findViewById(R.id.WorkoutDuration);
        editDescription = findViewById(R.id.WorkoutDescription);

        Button SaveButton = findViewById(R.id.btnSaveWorkout);
        Button CancelButton = findViewById(R.id.btnCancelWorkout);

        CancelButton.setOnClickListener( v -> finish());

        SaveButton.setOnClickListener(v -> {

            if(!validInputs()){
                return;
            }
            String name = editName.getText().toString();
            String date = editDate.getText().toString();
            int duration = Integer.parseInt(editDuration.getText().toString());
            String description = editDescription.getText().toString();

            Toast.makeText(this, "Saved workout:\n"
            + name + "\n"
            + date + "\n"
            + duration + "\n"
            + description, Toast.LENGTH_LONG).show();

            finish();


        });

    }// end of onCreate()


    private boolean validInputs(){
        String name = editName.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String durationStr = editDuration.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Workout name required");
            return false;
        }
        if (date.isEmpty() || !date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            editDate.setError("Workout date required");
            return false;
        }
        if (desc.isEmpty()) {
            editDescription.setError("Workout description required");
            return false;
        }
        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            editDuration.setError("Workout duration must be a number");
            return false;
        }
        if (duration <= 0) {
            editDuration.setError("Workout duration must be greater than 0");
            return false;
        }
        return true;
    } //end of validInputs()

}
