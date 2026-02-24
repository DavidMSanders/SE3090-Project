package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView setsTxt;
    private TextView repsTxt;
    private TextView totalTxt;
    private Button repsincreaseBtn; // define increase button variable
    private Button repsdecreaseBtn; // define decrease button variable

    private Button setsincreaseBtn; // define increase button variable
    private Button setsdecreaseBtn; // define decrease button variable
    private Button backBtn;     // define back button variable

    private int sets = 0;    // counter variable
    private int reps = 0;    // counter variable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        /* initialize UI elements */
        setsTxt = findViewById(R.id.setsNumber3);
        totalTxt = findViewById(R.id.setsNumber);
        repsTxt = findViewById(R.id.repsNumber);

        repsincreaseBtn = findViewById(R.id.reps_increase_btn);
        repsdecreaseBtn = findViewById(R.id.reps_decrease_btn);
        setsincreaseBtn = findViewById(R.id.sets_increase_btn);
        setsdecreaseBtn = findViewById(R.id.sets_decrease_btn);
        backBtn = findViewById(R.id.counter_back_btn);



        /* Increase reps */
        repsincreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reps++;
                repsTxt.setText(String.valueOf(reps));
                totalTxt.setText(String.valueOf(reps / sets));
            }
        });

        /* Decrease reps */
        repsdecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reps > 0) {
                    reps--;
                    repsTxt.setText(String.valueOf(reps));
                    totalTxt.setText(String.valueOf(reps / sets));
                }
            }
        });
        /* Increase reps */
        setsincreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sets++;
                setsTxt.setText(String.valueOf(sets));
                totalTxt.setText(String.valueOf(reps / sets));
            }
        });

        /* Decrease reps */
       setsdecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sets > 0) {
                    sets--;
                    setsTxt.setText(String.valueOf(sets));
                    totalTxt.setText(String.valueOf(reps / sets));
                }
            }
        });


        /* when back btn is pressed, switch back to MainActivity */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CounterActivity.this, MainActivity.class);
                intent.putExtra("NUM", String.valueOf(reps));  // key-value to pass to the MainActivity
                startActivity(intent);
            }
        });

    }
}