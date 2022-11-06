package com.example.escortme.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class InternetService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!hasNetwork(this)) {
            Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show();
        }


        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean hasNetwork(Context context) {
        boolean hasWifi = false;
        boolean hasMobile = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : netInfo) {
            if ("WIFI".equals(networkInfo.getTypeName()))
                if (networkInfo.isConnected())
                    hasWifi = true;
            if ("MOBILE".equals(networkInfo.getTypeName()))
                if (networkInfo.isConnected())
                    hasMobile = true;
        }

        return hasWifi || hasMobile;
    }
}
