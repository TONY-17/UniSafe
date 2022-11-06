package com.example.escortme.utils;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.escortme.R;

public class RideDetailsBottomSheet extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_details_bottom_sheet);
        Toast.makeText(this, "BottomSheetCreate", Toast.LENGTH_SHORT).show();
    }
}
