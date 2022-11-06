package com.example.escortme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.escortme.databinding.ActivityInitialBinding;
import com.example.escortme.driverApp.DriverHome;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.AuthRequest;
import com.example.escortme.studentApp.Home;
import com.example.escortme.studentApp.StudentSignUpActivity;
import com.example.escortme.studentApp.UploadProfileImage;
import com.example.escortme.utils.Helpers;
import com.example.escortme.utils.InternetService;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pubnub.api.PubNub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitialActivity extends AppCompatActivity {

    private ActivityInitialBinding binding;

    // Variables used across the whole project
    public static Long currentUserID;
    public static String email;
    public static String organisation;
    public static String username;
    public static Long orgId;
    public static Long orgIdDriver;
    public static Long studentId;
    public static Long driverId;
    public static List<String> pickUpPoints;
    CircularProgressIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInitialBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getWindow().setStatusBarColor(Color.WHITE);
        indicator = binding.circularProgressIndicator;
        // Log current user in
        binding.userLogin.setOnClickListener(v -> {
            loginUser();
        });

        // Go to the registration page
        binding.userRegister.setOnClickListener(v -> {
            startActivity(new Intent(InitialActivity.this, StudentSignUpActivity.class));
        });

    }

    public void loginUser() {

        // TextBoxes from view binding
        TextInputEditText txtEmail = binding.txtEmail;
        TextInputEditText txtPassword = binding.txtPassword;
        // Get data from textBoxes
        String email = Objects.requireNonNull(txtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(txtPassword.getText()).toString().trim();

        if (email.isEmpty()) {
            binding.tempEmailStore.setError("Username required");
            txtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            binding.textInputLayout7.setError("Password required");
            txtPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            txtPassword.setError("Password less than 6 characters");
            txtPassword.requestFocus();
            return;
        }
        indicator.setVisibility(View.VISIBLE);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(email);
        authRequest.setPassword(password);

        Call<ResponseBody> loginRequestCall = RetrofitClient.getRetrofitClient().getAPI().signInUser(authRequest);
        loginRequestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    try {
                        assert response.body() != null;
                        String data = response.body().string();
                        System.out.println("DATA RESPONSE " + data);
                        redirectToCorrectActivity(data);
                        indicator.setVisibility(View.GONE);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    try {
                        assert response.errorBody() != null;
                        System.out.println("AUTHENTICATION ERROR " + response.errorBody().string());
                        Helpers.failure(InitialActivity.this, "Authentication Error: Check credentials");
                        indicator.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(InitialActivity.this, "SERVER ERROR");
                indicator.setVisibility(View.GONE);
            }
        });

    }

    // Based on the user type show the respective activity
    public void redirectToCorrectActivity(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        // Extract user role from the array
        JSONArray jsonArray = jsonObject.getJSONArray("role");
        int size = jsonArray.length();
        ArrayList<String> type = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            type.add(jsonArray.get(i).toString());
        }
        switch (type.get(0)) {
            case "USER":
                currentUserID = jsonObject.getLong("id");
                email = jsonObject.getString("email");
                username = jsonObject.getString("username");
                organisation = jsonObject.getString("organisation");
                orgId = jsonObject.getLong("orgId");
                studentId = jsonObject.getLong("studentId");
                JSONArray points = jsonObject.getJSONArray("pickUpPoints");
                pickUpPoints = new ArrayList<>();

                for (int i = 0; i < points.length(); i++) {
                    pickUpPoints.add(points.getString(i).toUpperCase());
                }
                System.out.println("CURRENT STUDENT DETAILS " + data);
                FirebaseMessaging.getInstance().subscribeToTopic(username);

                this.runOnUiThread(() -> {

                    Helpers.success(InitialActivity.this, "Student logged in");
                    Intent userIntent = new Intent(InitialActivity.this, Home.class);
                    startActivity(userIntent);
                    finish();
                });
                break;

            case "DRIVER":
                String name = jsonObject.getString("name");
                driverId = jsonObject.getLong("driverId");
                orgIdDriver = jsonObject.getLong("orgId");
                System.out.println("DRIVER ID " + driverId);
                boolean hasProfile = jsonObject.getBoolean("profileUploaded");

                if (hasProfile == true) {
                    this.runOnUiThread(() -> {
                        Helpers.success(InitialActivity.this, "Driver logged in");
                        Intent driverIntent = new Intent(InitialActivity.this, DriverHome.class);
                        startActivity(driverIntent);
                        finish();
                    });
                }

                if (hasProfile == false) {
                    Intent driverIntent = new Intent(InitialActivity.this, UploadProfileImage.class);
                    driverIntent.putExtra("driverName", name);
                    startActivity(driverIntent);
                    finish();
                }

                break;

            default:
                break;

        }
    }


}