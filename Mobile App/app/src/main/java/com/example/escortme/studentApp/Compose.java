package com.example.escortme.studentApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.AlertMediaRequest;
import com.example.escortme.network.model.AlertRequest;
import com.example.escortme.studentApp.menu.SelectedAdapter;
import com.example.escortme.studentApp.ui.home.HomeFragment;
import com.example.escortme.utils.Helpers;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

public class Compose extends AppCompatActivity {

    TextInputEditText tipText;
    MaterialAutoCompleteTextView categories;
    ImageLoader imageLoader = null;
    RecyclerView recyclerView;

    List<MultipartBody.Part> files;
    CircularProgressIndicator circularProgressIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        getWindow().setStatusBarColor(Color.WHITE);

        circularProgressIndicator = findViewById(R.id.spin_kit);
        imageLoader = new ImageLoader();
        // Populating the drop-down textBox with Alert categories
        categories = findViewById(R.id.alertCategories);
        categories.setAdapter(getCategories());

        MaterialButton sendTipBtn = findViewById(R.id.sendTipBtn);
        tipText = findViewById(R.id.TxtTip);

        files = new ArrayList<>();
        sendTipBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(categories.getText()) || !TextUtils.isEmpty(tipText.getText())) {
                    circularProgressIndicator.setVisibility(View.VISIBLE);
                    submitTip();

            } else {
                if (TextUtils.isEmpty(categories.getText())) {
                    categories.requestFocus();
                }
                if (TextUtils.isEmpty(tipText.getText())) {
                    tipText.requestFocus();
                }
                Helpers.failure(Compose.this, "Required fields are empty");
            }

        });

        ImageButton backBtn = findViewById(R.id.editProfileBackBtn);
        backBtn.setOnClickListener(v -> Compose.super.onBackPressed());

        // Allow user to select media files to add
        final FalleryOptions falleryOptions = new FalleryBuilder()
                .setImageLoader(imageLoader)
                .setTheme(R.style.FalleryLight)
                .build();

        MaterialButton addMedia = findViewById(R.id.addMedia);
        addMedia.setOnClickListener(v ->
                Fallery.startFalleryFromActivityWithOptions(
                        Compose.this, 1, falleryOptions
                ));

        recyclerView = findViewById(R.id.attachedPhotos);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String[] result = Fallery.getResultMediasFromIntent(data);
            String caption = Fallery.getCaptionFromIntent(data);
            handleRes(result);
        }
    }

    private void handleRes(String[] result) {
        List<Bitmap> images = new ArrayList<>();
        SelectedAdapter selectedAdapter = null;
        for (int i = 0; i < result.length; i++) {
            images.add(BitmapFactory.decodeFile(result[i]));
            selectedAdapter = new SelectedAdapter(images, Compose.this);
        }
        prepareToUpload(images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        recyclerView.setAdapter(selectedAdapter);
    }



    private void prepareToUpload(List<Bitmap> images){
        for(int i = 0; i < images.size();i++){
            String fileName = InitialActivity.username+i+ Helpers.generatePassword();
            File file = Helpers.saveBitmap(images.get(i),fileName);
            RequestBody fileBody = RequestBody.create(file,MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
            // Add each bitmap to list to send to server
            files.add(filePart);
        }
    }


    public ArrayAdapter<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Question");
        categories.add("Safety");
        categories.add("Alert");
        categories.add("Other");
        return new ArrayAdapter<>(Compose.this, R.layout.list_item, categories);
    }

    public void submitTip() {
        String category = categories.getText().toString().trim();
        String message = Objects.requireNonNull(tipText.getText()).toString().trim();

        //Long currentUserId = InitialActivity.currentUserID;
        Long currentUserId = HomeFragment.studentIdLong;

        // User has not selected any files
        if (files.size() == 0) {
            AlertRequest request = new AlertRequest();
            request.setTag(category);
            request.setBody(message);
            request.setSubject(category);
            request.setTimePosted(getCurrentTimeDate());
            request.setReported(false);
            System.out.println("NO MEDIA UPLOADED");
            Call<ResponseBody> reportIncidentCall = RetrofitClient.getRetrofitClient().getAPI().submitTip(
                    currentUserId,
                    request);
            reportIncidentCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        try {
                            String data = response.body().string();
                            System.out.println("POST DATA RESPONSE " + data);
                            finish();
                            circularProgressIndicator.setVisibility(View.GONE);
                            Helpers.success(Compose.this, "Alert posted successfully");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        circularProgressIndicator.setVisibility(View.GONE);
                        Helpers.failure(Compose.this, "ALERT ERROR ");
                        System.out.println("ALERT ERROR RESPONSE " + response.message());

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    circularProgressIndicator.setVisibility(View.GONE);
                    System.out.println("UPLOADING ERROR " + t.getMessage().toString());
                    Helpers.failure(Compose.this, "FAILURE:");
                }
            });
        }
        // User has selected media
        if(files.size() > 0){
            AlertMediaRequest alertMediaRequest = new AlertMediaRequest();
            alertMediaRequest.setTag(category.toString());
            alertMediaRequest.setBody(message);
            alertMediaRequest.setFiles(files);
            System.out.println("UPLOADED MEDIA " + alertMediaRequest.getTag());


            Call<ResponseBody> reportIncidentCall = RetrofitClient.getRetrofitClient().getAPI().submitMediaTip(
                    currentUserId,
                    alertMediaRequest.getTag(),
                    alertMediaRequest.getBody(),
                    files);

            reportIncidentCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        finish();
                        circularProgressIndicator.setVisibility(View.GONE);
                        Helpers.success(Compose.this, "Alert posted successfully");
                    } else {
                        circularProgressIndicator.setVisibility(View.GONE);
                        Helpers.failure(Compose.this, "ALERT ERROR ");
                        System.out.println("ALERT ERROR RESPONSE " + response.message().toString());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    circularProgressIndicator.setVisibility(View.GONE);
                    System.out.println("UPLOADING ERROR " + t.getMessage().toString());
                    Helpers.failure(Compose.this, "FILE LIMIT REACHED");
                }
            });
        }

    }

    public static String getCurrentTimeDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
}