package com.example.escortme.studentApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.escortme.InitialActivity;
import com.example.escortme.databinding.ActivityStudentSignUpBinding;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.AuthRequest;
import com.example.escortme.utils.Helpers;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentSignUpActivity extends AppCompatActivity{
    // Binding to access the views
    private ActivityStudentSignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentSignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getWindow().setStatusBarColor(Color.WHITE);
        // Register user button click
        binding.registerUser.setOnClickListener(v->register());
        // Back button click
        binding.imageView14.setOnClickListener(v -> StudentSignUpActivity.super.onBackPressed());
    }

    private void register(){

        String username = Objects.requireNonNull(binding.TxtUsername.getText()).toString().trim();
        if(username.isEmpty()){
            binding.outlinedTextField.setError("Username required!");
            return;
        }

        String email = Objects.requireNonNull(binding.TxtEmail.getText()).toString().trim();
        if(email.isEmpty()){
            binding.textInputLayout.setError("Email required!");
            return;
        }

        String password = Objects.requireNonNull(binding.TxtPassword.getText()).toString().trim();
        if(password.isEmpty()){
            binding.passwordToggle.setError("Password required!");
            return;
        }
        if(password.length() < 6){
            binding.passwordToggle.setError("Password should be at-least 6 characters long");
            return;
        }

        CircularProgressIndicator indicator = binding.circularProgressIndicator;
        indicator.setVisibility(View.VISIBLE);

        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);

        Call<ResponseBody> registerRequestCall = RetrofitClient.getRetrofitClient().getAPI().registerUser(request);
        registerRequestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Helpers.success(StudentSignUpActivity.this, "Account registered successfully");
                    Intent intent = new Intent(StudentSignUpActivity.this, InitialActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    try {
                        assert response.errorBody() != null;
                        Helpers.failure(StudentSignUpActivity.this, response.errorBody().string());
                        System.out.println("AUTHENTICATION ERROR " + response.errorBody().string());
                        indicator.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                indicator.setVisibility(View.GONE);
                Helpers.failure(StudentSignUpActivity.this, "Server error creating account");
            }
        });

    }

}