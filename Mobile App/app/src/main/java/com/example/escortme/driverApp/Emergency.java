package com.example.escortme.driverApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.escortme.R;
import com.google.android.material.card.MaterialCardView;

public class Emergency extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        getWindow().setStatusBarColor(Color.WHITE);


        TextView Txtlocation = findViewById(R.id.emergencyLocation);
        TextView Txtuser = findViewById(R.id.emergencyStudent);
        String user = getIntent().getStringExtra("user");
        String address = getIntent().getStringExtra("address");
        String location = getIntent().getStringExtra("slocation");

        runOnUiThread(() -> {
            Txtuser.setText(user);
            Txtlocation.setText(address);
        });

        MaterialCardView assist = findViewById(R.id.assistStudent);
        assist.setOnClickListener(v -> {
            Intent i = new Intent(Emergency.this, DriverHome.class);
            i.putExtra("emergencyLocation",location);
            i.putExtra("emergencyAddress",address);
            i.putExtra("emergencyUser",user);
            startActivity(i);
            finish();
        });


    }
}