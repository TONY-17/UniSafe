package com.example.escortme.studentApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.ActivityMainBinding;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.EmergencyRequest;
import com.example.escortme.studentApp.ui.home.HomeFragment;
import com.example.escortme.utils.Helpers;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private static MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    // used for the timer that is displayed at the bottom of the screen
    Chronometer timer;
    // used for the global channel connection
    static PubNub pubnub;
    Location location;
    String strLocation;
    static boolean inEmergency = false;
    String emergencyAddress;
    private CharSequence[] emergencyType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requires mapbox API Key
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        // Will use viewBinding to access the view IDs
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ConstraintLayout parentLayout = binding.emergencyParentLayout;
        final Animation[] animation = new Animation[1];
        animation[0] = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_to_original);
        parentLayout.setAnimation(animation[0]);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white,null));
        Location location = HomeFragment.userLocation;
        emergencyType = new CharSequence[]{
                "Medical",
                "Crime",
                "Accident"
        };

        mapView = binding.mapViewOrder;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        timer = binding.emergencyTimer;
        // When the activity is created run the timer
        timer.start();


        MaterialCardView stopBroadcasting = binding.stopBroadcastingLocation;
        stopBroadcasting.setOnClickListener(v -> {
            // Open a dialog asking the user if they are safe?
            Point userLocation = Point.fromLngLat(location.getLongitude(),location.getLatitude());
            updateCameraPosition(userLocation,8f);
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
            dialogBuilder.setTitle("Emergency type");

            AtomicInteger choice = new AtomicInteger();

            dialogBuilder.setBackground(getResources().getDrawable(R.drawable.alert_dialog_shape,null));
            dialogBuilder.setSingleChoiceItems(emergencyType, 0, (dialog, which) -> {

                choice.set(which);
            });

            //dialogBuilder.setMessage("What best describes the emergency?");
            dialogBuilder.setPositiveButton("SEND", (dialog, which) -> {
                String type = String.valueOf(emergencyType[choice.intValue()]);
                pubnub.disconnect();
                timer.stop();
                sendEmergencyRequest(type,location);
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();

            });
            dialogBuilder.setNeutralButton("DISMISS", (dialog, which) -> {
                Point userLocation1 = Point.fromLngLat(location.getLongitude(),location.getLatitude());
                updateCameraPosition(userLocation1,14f);
            });
            dialogBuilder.show();
            // Disable the SOS button from the Home Page of the student app


        });

        try {
            /*
             * PubNub is required to provide the app with real time architecture
             * Student applications will be publishing emergency requests
             */
            initPubNubEmergencyChannel("escortMeEmergencies");
            /*
             * The student needs to notify the driver about their name & current location
             */


            strLocation = location.getLatitude() + "," + location.getLongitude();
            MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .query(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    if(response.isSuccessful()){
                        String data = response.body().toString();
                        List<CarmenFeature> results = response.body().features();

                        emergencyAddress = results.get(0).placeName();
                        String message =  InitialActivity.username +  " : "
                                + emergencyAddress +  " : " +
                                strLocation;
                        System.out.println("ADDRESS OBTAINED " + message);
                        sendEmergencyMessage(message,"escortMeEmergencies");
                    }
                }
                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {

                }
            });

        } catch (PubNubException e) {
            e.printStackTrace();
        }


    }

    private void sendEmergencyRequest(String type, Location location) {
        long elapsedMillis = SystemClock.elapsedRealtime() - timer.getBase();
        int h = (int) (elapsedMillis / 3600000);
        int m = (int) (elapsedMillis - h * 3600000) / 60000;
        int s = (int) (elapsedMillis - h * 3600000 - m * 60000) / 1000;
        String time = h + ":" + m + ":" + s;
        EmergencyRequest emergencyRequest = new EmergencyRequest();
        emergencyRequest.setDuration(time);

        emergencyRequest.setLocation(location.getLatitude() + "," + location.getLongitude());
        emergencyRequest.setType(type);
        emergencyRequest.setAddress(emergencyAddress);

        Call<ResponseBody> sendEmergency = RetrofitClient.getRetrofitClient().getAPI().createNewEmergency(
                InitialActivity.studentId,
                InitialActivity.orgId,
                emergencyRequest
        );
        sendEmergency.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    runOnUiThread(() -> {
                        Helpers.success(MainActivity.this,"Thank you for feedback");
                        finish();
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void updateCameraPosition(Point point, float zoom) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(point.latitude(), point.longitude()))
                .zoom(zoom)
                .bearing(180)
                .tilt(0)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }

    public static void initPubNubEmergencyChannel(String emergencyChannel) throws PubNubException {
        String uuid = "escortMeEmergencyUUID";
        PNConfiguration pnConfiguration = new PNConfiguration(uuid);
        pnConfiguration.setPublishKey("pub-c-873fa8f1-23ec-4682-99e5-34e01aee7b99");
        pnConfiguration.setSubscribeKey("sub-c-b1a4fe2e-6d61-4c2e-9450-6190196b4e6c");
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);
        pubnub.subscribe().channels(Collections.singletonList(emergencyChannel)).execute();
    }

    public static void sendEmergencyMessage(String msg,String emergencyChannel) {
        inEmergency = true;
        pubnub.publish()
                .message(msg)
                .channel(emergencyChannel)
                .async((result, status) -> {
                    if (status.isError()) {
                        System.out.println("pub status code: " + status.getErrorData());
                    }
                });
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MainActivity.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"), style -> {
            enableLocationComponent(Objects.requireNonNull(mapboxMap.getStyle()));
            mapboxMap.getUiSettings().setCompassEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            location = locationComponent.getLastKnownLocation();
/*            System.out.println("CURRENT LOCATION " + location.getLongitude() + " " + location.getLatitude());
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(12f)
                    .build());*/
            Point userLocation = Point.fromLngLat(location.getLongitude(),location.getLatitude());
            updateCameraPosition(userLocation,14f);

        });
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(this, "User location is required by the application", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean b) {
        if (b) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(this, "User location permission has not been granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void enableLocationComponent(Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            @SuppressLint("ResourceAsColor") LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .pulseEnabled(true)
                    .pulseColor(Color.RED)
                    .accuracyColor(Color.WHITE)
                    .pulseMaxRadius(100f)
                    .foregroundDrawable(R.drawable.ic_baseline_circle_24)
                    .build();

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
