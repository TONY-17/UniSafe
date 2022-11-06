package com.example.escortme.studentApp.nearby;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.card.MaterialCardView;


public class NearbyUtils extends NearbyAPI {
    /*
     * Nearby API Utils
     */
    private static final boolean DEBUG = true;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String SERVICE_ID =
            "com.example.escortme.studentApp.nearby.SERVICE_ID";
    private String mName = InitialActivity.username;

    Context context;
    MaterialCardView request;
    MaterialCardView search;
    public NearbyUtils(Context context,
                       MaterialCardView request) {
        this.context = context;
        this.request = request;
       // setContext(context);
        //enableAdvertisingMode();
    }

    public void showDeviceDetected(Context context,
                                   TextView name,
                                   TextView device,
                                   MaterialCardView request) {
        final Animation[] animation = new Animation[1];
        animation[0] = AnimationUtils.loadAnimation(context,
                R.anim.bottom_to_original);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            name.setVisibility(View.VISIBLE);
            device.setVisibility(View.VISIBLE);
            request.setVisibility(View.VISIBLE);
            name.setAnimation(animation[0]);
            device.setAnimation(animation[0]);

            // Enable the request button
            sendRequest();
        }, 3000);
    }

    public void sendRequest() {
        request.setOnClickListener(view -> {

            Toast.makeText(request.getContext(), "Happy", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {

    }

    @Override
    protected void onEndpointDisconnected(Endpoint e) {

    }

    @Override
    protected void onConnectionFailed(Endpoint remove) {

    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {

    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {

    }

    @Override
    protected void onAdvertisingStarted() {

    }

    @Override
    protected void onAdvertisingFailed() {

    }


    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    @Override
    protected void onDiscoveryStarted() {

    }

    @Override
    protected void onDiscoveryFailed() {

    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {

    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }


    @Override
    protected void onAcceptRequestFailed() {

    }
}
