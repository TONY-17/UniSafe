package com.example.escortme.utils;

import android.app.Application;
import android.content.Intent;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.search.MapboxSearchSdk;

public class SearchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationEngine locationEngineProvider = LocationEngineProvider.getBestLocationEngine(this);
        MapboxSearchSdk.initialize(
                this,
                getString(R.string.mapbox_access_token),
                locationEngineProvider
        );
        startService(new Intent(this, InternetService.class));

    }
}