package com.example.escortme;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Change status bar to primary color
        getWindow().setStatusBarColor(getColor(R.color.primary));

        // Animate views from bottom to current location
        final Animation[] animation = new Animation[1];
        animation[0] = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_to_original);

        // View to be animated
        ImageView logo = findViewById(R.id.uniSafeLogo);
        logo.setAnimation(animation[0]);

        // Open the SignIn && Register activity after 2 seconds
        Handler handler = new Handler();

        handler.postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, InitialActivity.class));
            finish();
        },2000);
    }

}