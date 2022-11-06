package com.example.escortme.studentApp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.DriverHome;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.utils.BActivityResult;
import com.example.escortme.utils.Helpers;
import com.google.android.material.card.MaterialCardView;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import ir.mehdiyari.fallery.main.fallery.Fallery;
import ir.mehdiyari.fallery.main.fallery.FalleryBuilder;
import ir.mehdiyari.fallery.main.fallery.FalleryOptions;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadProfileImage extends AppCompatActivity {

    protected final BActivityResult<Intent, ActivityResult> activityResultBActivityResult = BActivityResult.registerActivityForResult(this);
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile);
        // Change status bar color
        getWindow().setStatusBarColor(Color.WHITE);

        String name = getIntent().getStringExtra("driverName");
        TextView description = findViewById(R.id.textView7);
        description.setText(name + ", drivers are required to upload pictures.");

        MaterialCardView cameraSelect = findViewById(R.id.takeAPicture);
        cameraSelect.setOnClickListener(view -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            activityResultBActivityResult.launch(intent, result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //imageView.setImageBitmap(imageBitmap);

                    File file = Helpers.saveBitmap(imageBitmap,name);

                    try{
                        RequestBody fileBody = RequestBody.create(file,MediaType.parse("multipart/form-data"));
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
                        Call<ResponseBody> upload = RetrofitClient.getRetrofitClient().getAPI().uploadDriverImage(
                                InitialActivity.driverId,
                                filePart
                        );
                        upload.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.isSuccessful()){
                                    Helpers.success(UploadProfileImage.this,"Profile uploaded");
                                    Intent driverIntent = new Intent(UploadProfileImage.this, DriverHome.class);
                                    startActivity(driverIntent);
                                    finish();
                                }else{
                                    Helpers.failure(UploadProfileImage.this,response.message().toString());
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Helpers.failure(UploadProfileImage.this,t.getMessage().toString());
                            }
                        });
                    }catch (NullPointerException e){
                        Helpers.success(UploadProfileImage.this,"Profile uploaded");
                        Intent driverIntent = new Intent(UploadProfileImage.this, DriverHome.class);
                        startActivity(driverIntent);
                        finish();
                    }


                }

            });
        });



        ImageView selectedImage = findViewById(R.id.selectedImage);
        MaterialCardView popUp = findViewById(R.id.imagePopUp);

        MaterialCardView gallerySelect = findViewById(R.id.chooseFromGallery);
        gallerySelect.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            activityResultBActivityResult.launch(intent, result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(result.getData().getData());
                        bitmap  = BitmapFactory.decodeStream(inputStream);
                        getWindow().setStatusBarColor(getResources().getColor(R.color.primary,null));
                        popUp.setVisibility(View.VISIBLE);
                        selectedImage.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        ImageView uploadImage = findViewById(R.id.happyWithHappy);
        uploadImage.setOnClickListener(view -> {

            File file = Helpers.saveBitmap(bitmap,name);
            RequestBody fileBody = RequestBody.create(file,MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
            Call<ResponseBody> upload = RetrofitClient.getRetrofitClient().getAPI().uploadDriverImage(
                    InitialActivity.driverId,
                    filePart
            );
            upload.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        Helpers.success(UploadProfileImage.this,"Profile uploaded");
                        Intent driverIntent = new Intent(UploadProfileImage.this, DriverHome.class);
                        startActivity(driverIntent);
                        finish();
                    }else{
                        Helpers.failure(UploadProfileImage.this,response.message().toString());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Helpers.failure(UploadProfileImage.this,t.getMessage().toString());
                }
            });

        });

        ImageView close = findViewById(R.id.closePop);
        close.setOnClickListener(view -> finish());
    }


}