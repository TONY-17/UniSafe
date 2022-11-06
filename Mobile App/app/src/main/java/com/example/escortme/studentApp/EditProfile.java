package com.example.escortme.studentApp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.ui.driverDashboard.DriverDashboardFragment;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.DriverRequest;
import com.example.escortme.network.model.StudentInfo;
import com.example.escortme.studentApp.menu.MenuAdapter;
import com.example.escortme.studentApp.ui.settings.NotificationsFragment;
import com.example.escortme.utils.Helpers;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfile extends AppCompatActivity {
    long studentId;
    Long userId;
    TextInputEditText txtUsername, txtDriverSurname, txtDriverTitle, txtDriverGender;
    TextInputEditText txtEmail;

    TextInputLayout tempUsernameStore, tempEmailStore;
    ImageButton editProfileBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getWindow().setStatusBarColor(Color.WHITE);

        userId = InitialActivity.currentUserID;


        txtUsername = findViewById(R.id.TxtUsername);
        txtDriverSurname = findViewById(R.id.txtDriverSurname);
        txtDriverTitle = findViewById(R.id.txtDriverTitle);
        txtDriverGender = findViewById(R.id.txtDriverGender);
        txtEmail = findViewById(R.id.txtEmail);
        tempUsernameStore = findViewById(R.id.tempUsernameStore);
        tempEmailStore = findViewById(R.id.tempEmailStore);


        boolean isDriver = MenuAdapter.isDriver;
        if (isDriver) {
            // Retrieve details about a driver
            retrieveDriverProfileDetails();
        } else {

            studentId = InitialActivity.studentId;
            // Not a driver but a student so retrieve details about a student
            retrieveProfileDetails();
        }


        // Update button pressed
        MaterialButton updateProfile = findViewById(R.id.updateProfile);
        updateProfile.setOnClickListener(v -> {
            if (isDriver) {
                updateDriverProfile();
            } else {
                updateStudentProfile();
                // Refresh the profile response from this page

            }
        });

        // Back button pressed
        editProfileBackBtn = findViewById(R.id.editProfileBackBtn);
        editProfileBackBtn.setOnClickListener(v -> EditProfile.super.onBackPressed());

    }

    private void retrieveDriverProfileDetails() {
        Long driverId = InitialActivity.driverId;
        Call<ResponseBody> getDriverInfo = RetrofitClient.getRetrofitClient().getAPI().getDriverInfo(driverId);
        getDriverInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String title = jsonObject.getString("title");
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        String gender = jsonObject.getString("gender");
                        String rating = jsonObject.getString("rating");
                        txtDriverTitle.setText(title);
                        txtUsername.setText(firstName);
                        txtDriverSurname.setText(lastName);
                        txtEmail.setText(email);
                        txtDriverGender.setText(gender);


                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Helpers.failure(EditProfile.this, "Failed to retrieve profile info");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(EditProfile.this, "Server retrieval error");
            }
        });
    }

    private void updateDriverProfile() {
        // check if the fields are empty
        String title = txtDriverTitle.getText().toString().trim();
        String firstName = txtUsername.getText().toString().trim();
        String lastName = txtDriverSurname.getText().toString().trim();
        String gender = txtDriverGender.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        if (title.isEmpty()) {
            txtDriverTitle.setError("Title required");
            txtDriverTitle.requestFocus();
            return;
        }
        if (firstName.isEmpty()) {
            txtUsername.setError("First name required");
            txtUsername.requestFocus();
            return;
        }


        DriverRequest driverRequest = new DriverRequest();
        driverRequest.setTitle(title);
        driverRequest.setFirstName(firstName);
        driverRequest.setLastName(lastName);
        driverRequest.setEmail(email);
        driverRequest.setGender(gender);

        Call<ResponseBody> updateDriverInfo = RetrofitClient.getRetrofitClient().getAPI().updateDriver(
                InitialActivity.driverId,
                driverRequest
        );

        updateDriverInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Helpers.success(EditProfile.this, "Details updated");
                    DriverDashboardFragment.getDriverProfileInfo();
                    finish();
                    // Reload the data from api on the settings page

                } else {
                    Helpers.failure(EditProfile.this, "Failed to update");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(EditProfile.this, "Server error");
            }
        });
    }

    private void retrieveProfileDetails() {
        // The student does not have the following records
        //txtDriverTitle.setVisibility(View.GONE);
        txtDriverSurname.setVisibility(View.GONE);
        txtDriverGender.setVisibility(View.GONE);
        txtEmail.setVisibility(View.GONE);

        Call<ResponseBody> studentDetails = RetrofitClient.getRetrofitClient().getAPI().getStudentDetails(InitialActivity.currentUserID);
        studentDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String username = jsonObject.getString("username");
                        String email = jsonObject.getString("email");
                        tempUsernameStore.setHint("Username");
                        txtDriverTitle.setText(username);
                        tempEmailStore.setHint("Email");
                        txtUsername.setText(email);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Helpers.failure(EditProfile.this, "Can't retrieve profile details");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }


    private void updateStudentProfile() {

        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setUsername(txtDriverTitle.getText().toString().trim());
        studentInfo.setEmail(txtUsername.getText().toString().trim());

        Call<ResponseBody> update = RetrofitClient.getRetrofitClient().getAPI().updateStudentInfo(
                studentId,
                userId,
                studentInfo
        );

        update.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        System.out.println("UPDATE RESPONSE "  + response.body().string()) ;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Helpers.success(EditProfile.this, "You have successfully update your info");
                    finish();
                    NotificationsFragment.retrieveProfileInformation();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("UPDATE ERROR " + t.getMessage());
                Helpers.failure(EditProfile.this, "Failed to update");
            }
        });
    }
}