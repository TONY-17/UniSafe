package com.example.escortme.driverApp.rating;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.DriverHome;
import com.example.escortme.driverApp.ui.driverHome.DriverHomeFragment;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.ReviewRequest;
import com.example.escortme.studentApp.Home;
import com.example.escortme.studentApp.OrderActivity;
import com.example.escortme.utils.Helpers;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import nl.bryanderidder.themedtogglebuttongroup.SelectAnimation;
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Rating extends AppCompatActivity {

    int rating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        getWindow().setStatusBarColor(Color.WHITE);

        initBottomSheetBehaviour();

        ThemedToggleButtonGroup themedToggleButtonGroup = findViewById(R.id.time);
        themedToggleButtonGroup.setSelectAnimation(SelectAnimation.CIRCULAR_REVEAL);
        List<ThemedButton> allButtons = themedToggleButtonGroup.getButtons();
        ThemedButton button = allButtons.get(0);
        button.setOnClickListener(v -> rating = 1);

        ThemedButton button1 = allButtons.get(1);
        button1.setOnClickListener(v -> rating = 2);

        ThemedButton button2 = allButtons.get(2);
        button2.setOnClickListener(v -> rating = 3);


        ThemedButton button3 = allButtons.get(3);
        button3.setOnClickListener(v -> rating = 4);

        ThemedButton button4 = allButtons.get(4);
        button4.setOnClickListener(v -> rating = 5);


        TextInputEditText comment = findViewById(R.id.reviewComment);

        MaterialButton submit = findViewById(R.id.submitReview);
        submit.setOnClickListener(v ->
                runOnUiThread(() -> sendComment(rating, comment.getText().toString())));

        // Clear the file
        SharedPreferences sharedPreferences = getSharedPreferences("MyRequests", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

    private void initBottomSheetBehaviour() {

        ConstraintLayout constraintLayout = findViewById(R.id.detail_container);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }


    private void sendComment(int rating, String comment) {

        SharedPreferences sharedPreferences = getSharedPreferences("MyRequests", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        ReviewRequest reviewRequest = new ReviewRequest(
                rating,
                comment
        );
        Call<ResponseBody> sendReview = RetrofitClient.getRetrofitClient().getAPI().createNewReview(
                InitialActivity.studentId,
                OrderActivity.driverId,
                reviewRequest
        );

        sendReview.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Helpers.success(Rating.this, "Thank you for rating");
                } else {
                    Log.d("RATING FAILED ", response.message().toString());
                }

                if(DriverHomeFragment.openRatingPage == true){

                    Intent driverIntent = new Intent(Rating.this, DriverHome.class);
                    startActivity(driverIntent);
                    finish();

                }else{
                    startActivity(new Intent(Rating.this, Home.class));
                    finish();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RATING FAILED ", t.getMessage().toString());
                finish();
            }
        });


    }

}