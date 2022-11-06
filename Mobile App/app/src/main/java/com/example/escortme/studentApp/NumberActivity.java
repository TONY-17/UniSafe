package com.example.escortme.studentApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.escortme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class NumberActivity extends AppCompatActivity {
    private MaterialButton continueToRegister;
    private TextInputEditText userPhoneNumber;
    private MaterialCardView upperCard;
    private MaterialCardView lowCard;
    private MaterialButton openSMS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        upperCard = findViewById(R.id.upperCard);
        lowCard = findViewById(R.id.lowCard);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        openSMS = findViewById(R.id.openSMScode);
        final Animation[] animation = new Animation[1];
        animation[0] = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_to_original);
        final Handler handler = new Handler();
        openSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSMS.setVisibility(View.GONE);
                lowCard.setVisibility(View.VISIBLE);
                lowCard.setAnimation(animation[0]);
                // delay the upper card
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        upperCard.setVisibility(View.VISIBLE);
                        upperCard.setAnimation(animation[0]);
                    }
                },2000);
            }
        });


        continueToRegister = findViewById(R.id.continueToRegister);
        continueToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NumberActivity.this,StudentSignUpActivity.class);
                startActivity(i);
            }
        });



        userPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(userPhoneNumber.length() > 9){

                }else{
                    lowCard.setVisibility(View.GONE);
                }
            }
        });
    }
}