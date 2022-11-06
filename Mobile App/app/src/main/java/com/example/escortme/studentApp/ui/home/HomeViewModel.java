package com.example.escortme.studentApp.ui.home;

import android.widget.TextView;

import com.example.escortme.InitialActivity;
import com.example.escortme.network.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel  {


    public static void retrieveProfileInformation(TextView currentUsername){
        Call<ResponseBody> studentDetails = RetrofitClient.getRetrofitClient().getAPI().getStudentDetails(InitialActivity.currentUserID);
        studentDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String username = jsonObject.getString("username");

                        currentUsername.setText("Hello " + username);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


}