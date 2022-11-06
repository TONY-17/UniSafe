package com.example.escortme.studentApp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.escortme.R;
import com.example.escortme.databinding.ActivityHomeBinding;
import com.mapbox.mapboxsdk.Mapbox;


public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        /*getWindow().setNavigationBarColor(getColor(R.color.dark));*/

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(binding.getRoot());

        //BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_student);
        NavigationUI.setupWithNavController(binding.navViewStudent, navController);
    }



}