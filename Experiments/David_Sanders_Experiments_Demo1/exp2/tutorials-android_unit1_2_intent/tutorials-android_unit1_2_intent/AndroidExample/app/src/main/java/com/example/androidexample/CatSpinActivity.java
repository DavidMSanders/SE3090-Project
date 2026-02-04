package com.example.androidexample;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.TextView;

import android.widget.ImageView;

public class CatSpinActivity extends AppCompatActivity {

    private Button cat_backBtn;     // define back button variable

    private ImageView catImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_spin);

        catImage = findViewById(R.id.imageCat);

        cat_backBtn = findViewById(R.id.cat_back_btn);

         Animation rotatingCatAnim = AnimationUtils.loadAnimation(this, R.anim.rotating_cat);
         catImage.startAnimation(rotatingCatAnim);

        cat_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatSpinActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
