package com.example.escortme.driverApp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.example.escortme.R;
import com.example.escortme.databinding.ActivityDriverHomeBinding;

public class DriverHome extends AppCompatActivity {

    static ViewGroup parent;
    static View bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDriverHomeBinding binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.WHITE);
        parent = binding.container;
        bottomNavigationView = binding.navViewDriver;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_driver);
        NavigationUI.setupWithNavController(binding.navViewDriver, navController);
    }


    public static void showBottomNavigationView(boolean show){
        // Bottom navigation transactions
        Transition transitionBN = new Slide(Gravity.BOTTOM);
        transitionBN.setDuration(600);
        transitionBN.addTarget(bottomNavigationView);

        TransitionManager.beginDelayedTransition(parent, transitionBN);
        bottomNavigationView.setVisibility(show ? View.VISIBLE : View.GONE);

    }

}