package com.example.escortme.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import androidx.core.content.res.ResourcesCompat;

import com.example.escortme.R;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.Comment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class Helpers {

    public static void success(Activity activity, String message) {
        MotionToast.Companion.createColorToast(activity, "Success",
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.w_bk));
    }

    public static void failure(Activity activity, String message) {
        MotionToast.Companion.createColorToast(activity, "Error",
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.w_bk));
    }




    public static boolean hasInternetConnection(Context context){
        boolean hasWifi = false;
        boolean hasMobile = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : netInfo) {
            if("WIFI".equals(networkInfo.getTypeName()))
                if (networkInfo.isConnected())
                    hasWifi = true;
            if ("MOBILE".equals(networkInfo.getTypeName()))
                if (networkInfo.isConnected())
                    hasMobile = true;
        }

        return hasWifi || hasMobile;
    }

    public static String estimatedTime(Location initial, Location destination) {
        String[] result = new String[]{" MIN", " HR +"};
        double meters = initial.distanceTo(destination);
        double kilometers = meters / 1000;
        double kmsPerMin = 0.5;
        double minsTaken = kilometers / kmsPerMin;
        int total = (int) minsTaken;
        if (total < 60) {
            return total + "\n" + result[0];
        }else{
            return  (total/60) + "\n" + result[1];
        }
    }

    public static void Alert(Activity activity, String title, String message) {
        MotionToast.Companion.createColorToast(activity, title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.w_bk));
    }

    public static String generatePassword() {
        StringBuilder password = new StringBuilder();
        /*
         * The password will contain the following characters:
         * [A-Z][a-z][0-9]
         */
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 5; i++) {
            int randomIndex = random.nextInt(characters.length());
            password.append(characters.charAt(randomIndex));
        }
        return password.toString();
    }

    public static File saveBitmap(Bitmap bmp, String name) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        File file = new File(extStorageDirectory, name + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, name + ".png");
        }
        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public static String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy MMMM dd ");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }


    // Api call
    public static void addComment(Long alertId, Long studentId, String content, Activity activity) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setDateCreated(null);
        Call<ResponseBody> addComment = RetrofitClient.getRetrofitClient().getAPI().addComment(alertId, studentId, comment);
        addComment.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Helpers.success(activity, "Posted comment: ");

                } else {
                    System.out.println("FAILED COMMENT " + response.errorBody());
                    Helpers.failure(activity, "Failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("FAILED COMMENT " + t.getMessage().toString());
                Helpers.failure(activity, "Failed: " + t.getMessage().toString());
            }
        });


    }

/*
    public void buildNotification() {
        NotificationChannel channel = new NotificationChannel("channel1",
                "hello",
                NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        //Creating the notification object
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "channel1");
        //notification.setAutoCancel(true);
        notification.setContentTitle("Tracking driver location");
        notification.setContentText("Remember to check if the car number plate matches the one on your booking");
        notification.setSmallIcon(R.drawable.ic_launcher_foreground);

        //make the notification manager to issue a notification on the notification's channel
        manager.notify(121, notification.build());

    }*/

}
