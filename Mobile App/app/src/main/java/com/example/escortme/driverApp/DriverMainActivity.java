package com.example.escortme.driverApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.ebanx.swipebtn.SwipeButton;
import com.example.escortme.R;
import com.example.escortme.databinding.ActivityDriverMainBinding;
import com.example.escortme.utils.DriverLocationTracking;
import com.example.escortme.utils.MyCustomDialog;
import com.google.android.material.card.MaterialCardView;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class DriverMainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener{

    private static MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    DriverLocationTracking route;
    public static DirectionsRoute driverDriverRoute;

    //27.8678251
    public Point origin;
    public Point destination = Point.fromLngLat(27.9956147, -26.182546);


    private final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private final DriverMainActivityLocationCallback callback = new DriverMainActivityLocationCallback(this);

    MapView mapView;
    private PubNub pubnub;
    TextView answer;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        ActivityDriverMainBinding binding = ActivityDriverMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getWindow().setStatusBarColor(Color.WHITE);

        mapView = binding.mapViewOrder;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        MaterialCardView myLocation = findViewById(R.id.myLoc);
        EditText post = findViewById(R.id.postToPubNub);
        answer = findViewById(R.id.data);
        try {
            initPubNub();
        } catch (PubNubException e) {
            e.printStackTrace();
        }
        myLocation.setOnClickListener(v -> {
            String update = post.getText().toString().trim();
            System.out.println("UPDATE " + update);
            pubnub.publish()
                    .message(update)
                    .channel("EscortMe")
                    .async((result, status) -> {
                        // status.isError() to see if error happened and print status code if error
                        if (status.isError()) {
                            System.out.println("pub status code: " + status.getStatusCode());
                        }
            });

        });

        SwipeButton enableButton = findViewById(R.id.hold);
        enableButton.setOnStateChangeListener(active ->
                Toast.makeText(DriverMainActivity.this, "Offline", Toast.LENGTH_SHORT).show());

        enableButton.setOnActiveListener(() -> {
            Toast.makeText(DriverMainActivity.this, "Online", Toast.LENGTH_SHORT).show();
/*            dialog = new MyCustomDialog(DriverMainActivity.this,DriverMainActivity.this);
            buildNotification();
            dialog.showDialog("Auckland park, UJ GATE 3");
            dialog.navigate.setOnClickListener(v -> {
                getRouteFromOriginToDestination(origin,destination);
                dialog.dialog.dismiss();
            });*/
        });

        //listViewControls();


    }

    public void initPubNub() throws PubNubException {
        String uuid = "escortMeUUID";
        PNConfiguration pnConfiguration = new PNConfiguration(uuid);
        pnConfiguration.setPublishKey(getString(R.string.publish_key));
        pnConfiguration.setSubscribeKey(getString(R.string.subscribe_key));
        pnConfiguration.setSecure(true);

        pubnub = new PubNub(pnConfiguration);

        // Listen to location updates and messages that arrive at the channel
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final String msg = pnMessageResult.getMessage().toString().replace("\"", "");

                runOnUiThread(() -> {
                    try {
                        answer.setText(msg);
                    } catch (Exception e) {
                        System.out.println("Error");
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }

            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }
        });

        pubnub.subscribe().channels(Collections.singletonList("EscortMe")).execute();


    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "User location required", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        DriverMainActivity.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            mapboxMap.getUiSettings().setCompassEnabled(false);
            route = new DriverLocationTracking(this, mapboxMap);
            // Add heatmap
/*            try {
                HeatMap.addIncidentSource(style);
                HeatMap.addHeatMap(style);
                HeatMap.addLayer(style);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Log.d("2", "Supplied URL is invalid");
            }*/
        });
    }


    @SuppressWarnings({"MissingPermission"})
    public void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public static void animateToUserLocation(Location loc) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(loc.getLatitude(), loc.getLongitude())) // Sets the new camera position
                .zoom(12)
                .bearing(180)
                .tilt(0)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }


    private class DriverMainActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<DriverMainActivity> activityWeakReference;

        private DriverMainActivityLocationCallback(DriverMainActivity mainActivity) {
            this.activityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            DriverMainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                animateToUserLocation(location);
                System.out.println("My current location " + location);

                if (mapboxMap != null && result.getLastLocation() != null) {
                    mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }
}