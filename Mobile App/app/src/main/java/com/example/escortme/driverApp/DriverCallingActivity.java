package com.example.escortme.driverApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.escortme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class DriverCallingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary, null));

        Chronometer timer = findViewById(R.id.chronometer2);
        timer.start();


        TextView studentInitial = findViewById(R.id.studentCalledInitial);
        TextView studentName = findViewById(R.id.studentName);

        String result = getIntent().getStringExtra("studentName");
        studentInitial.setText(String.valueOf(result.charAt(0)));
        studentName.setText(result);


        MaterialCardView hangUp = findViewById(R.id.hangUpCall);
        hangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}