package com.example.escortme.studentApp.ui.home;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;
import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.FragmentHomeBinding;
import com.example.escortme.studentApp.Compose;
import com.example.escortme.studentApp.MainActivity;
import com.example.escortme.studentApp.SetLocationsActivity;
import com.example.escortme.studentApp.nearby.Nearby;
import com.example.escortme.studentApp.nearby.NearbyAPI;
import com.example.escortme.utils.Helpers;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.pubnub.api.PubNubException;
import com.squareup.seismic.ShakeDetector;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends NearbyAPI implements OnMapReadyCallback,
        PermissionsListener, OnCameraTrackingChangedListener, OnLocationClickListener, MapboxMap.OnMapClickListener, ShakeDetector.Listener {

    private static final String CHANNEL_ID = "EMERGENCY_NOTIFICATION";
    private final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private FragmentHomeBinding binding;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    MapView mapView;
    private LocationComponent locationComponent;

    public static Location userLocation;
    ThemedButton nearbyShare;
    TextView currentUsername;
    private final HomeFragmentLocationCallback callback = new HomeFragmentLocationCallback(this);
    public static Long studentIdLong;

    SensorManager sensorManager;
    ShakeDetector shakeDetector;
    // Flag for when the bottomsheet is opened
    public static boolean isOpen = false;
    // Nearby API attributes
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String SERVICE_ID =
            "com.example.escortme.studentApp.ui.home.SERVICE_ID";
    private String mName = InitialActivity.username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Using mapbox api key
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        createNotificationChannel();
        // MapView Setup
        mapView = binding.homeMap;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white, null));

        // Send to request page
        MaterialCardView requestCV = binding.requestCardView;
        requestCV.setOnClickListener(v -> startActivity(new Intent(getContext(), SetLocationsActivity.class)));

        // Obtain Student ID from login page
        studentIdLong = InitialActivity.studentId;
        // Fetch username from API and set on home page
        currentUsername = binding.currentUsername;
        HomeViewModel.retrieveProfileInformation(currentUsername);


        // Press and hold emergency button
        MaterialCardView holdButton = binding.hold;
        ProgressBar progressIndicator = binding.progressBar;
        Vibrator mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        TextView textView = binding.textView;
        holdButton.setOnLongClickListener(v -> {
            new CountDownTimer(2000, 1) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (!holdButton.isPressed()) {
                        textView.setTextColor(getResources().getColor(R.color.primary));
                        progressIndicator.setProgress(0);
                        mVibrator.cancel();
                        cancel();
                    } else {
                        textView.setTextColor(Color.WHITE);
                        progressIndicator.setProgress(1 + progressIndicator.getProgress());
                        mVibrator.vibrate(millisUntilFinished);
                    }
                }

                @Override
                public void onFinish() {
                    getActivity().runOnUiThread(() -> {
                        progressIndicator.setProgress(0);
                        startActivity(new Intent(getContext(), MainActivity.class));
                        //getActivity().finish();
                    });
                }
            }.start();
            return true;
        });


        // Activate advertise mode -> Other devices can find you when they are in need of help
        ThemedToggleButtonGroup themedButtonGroup = binding.toggleGroup;
        nearbyShare = themedButtonGroup.findViewById(R.id.btnShareInt);
        themedButtonGroup.setOnSelectListener((ThemedButton btn) -> {

            if (nearbyShare.isSelected() == true) {
                stopDiscovering();
                enableAdvertisingMode();
            } else {
                disableAdvertisingMode();
            }

            return kotlin.Unit.INSTANCE;
        });


        Set<Endpoint> DiscoveredEndpoints = getDiscoveredEndpoints();
        System.out.println("DiscoveredEndpoints " + DiscoveredEndpoints.toString());


        // When we have detected shaking events go on emergency alert mode
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this::hearShake);
       // shakeDetector.start(sensorManager);

        shakeDetector.start(sensorManager, SensorManager.SENSOR_DELAY_GAME);


        MaterialCardView compose = binding.alertMode;
        compose.setOnClickListener(view -> startActivity(new Intent(getContext(), Compose.class)));


        // Check if the user has not completed any trips
        SharedPreferences sh = getActivity().getSharedPreferences("MyRequests", MODE_PRIVATE);
        String CHANNEL = sh.getString("CHANNEL", "");
        String DEST = sh.getString("DEST", "");
        MaterialCardView popUp = binding.storedPopUp;
        if (!(CHANNEL.length() <= 0)) {
            ConstraintLayout parentLayout = binding.parentLayout;
            popUp.setVisibility(View.VISIBLE);
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.primary, null));
            requestCV.setOnClickListener(view -> Snackbar.make(parentLayout, "You have booked already", Snackbar.LENGTH_LONG).show());
            TextView popUpDest = binding.popUpDest;
            popUpDest.setText(DEST);
            popUp.setOnClickListener(view -> getActivity().onBackPressed());

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(parentLayout);
            constraintSet.connect(
                    R.id.currentUsername,
                    ConstraintSet.TOP,
                    R.id.storedPopUp,
                    ConstraintSet.BOTTOM
            );
            constraintSet.applyTo(parentLayout);


        }


        return root;
    }




    @Override
    protected void onDiscoveryStarted() {
        Toast.makeText(getContext(), "Discovery started", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDiscoveryFailed() {
        Toast.makeText(getContext(), "Discovery failed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        if (isAskingForConnection()) {
            connectToDevice(endpoint);
        }
    }

    @Override
    protected void onAcceptRequestFailed() {

    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) throws PubNubException {
        // We are receiving a name, location
        if (payload.getType() == Payload.Type.BYTES) {
            //Base64.getEncoder().encodeToString(bytes);
            //String result = new String(payload.asBytes(), StandardCharsets.UTF_8);
            byte[] receivedBytes = payload.asBytes();
            String result = new String(receivedBytes);
            MainActivity.initPubNubEmergencyChannel("escortMeEmergencies");
            // The result will be in the form String,Lat,Lng
            String[] resultArr = result.split(",");
            System.out.println("resultArr " + result);
            String strLocation = resultArr[1] + "," + resultArr[2];
            MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .query(Point.fromLngLat(Double.valueOf(resultArr[2]), Double.valueOf(resultArr[1])))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    if (response.isSuccessful()) {
                        String data = response.body().toString();
                        List<CarmenFeature> results = response.body().features();
                        //Tony : 101 Carter Road, Johannesburg, Johannesburg, Gauteng 2190, South Africa : -26.2599879,28.0392694
                        String message = resultArr[0] + " : "
                                + results.get(0).placeName() + " : " +
                                strLocation;
                        System.out.println("ADDRESS OBTAINED " + message);
                        // Send the emergency to drivers
                        MainActivity.sendEmergencyMessage(message, "escortMeEmergencies");
                        // Notify the student that their device was used to help a student
                        sendNotification(resultArr[0]);
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    Helpers.failure(getActivity(), "Failed to retrieve address");
                }
            });
        }
    }



    private void sendNotification(String result){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_notifications_active_24)
                .setContentTitle("Emergency alert")
                .setContentText(result + " just used your device to seek help. ")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(result + " just used your device to seek help. "))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = CHANNEL_ID;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onEndpointDisconnected(Endpoint e) {
        Helpers.failure(getActivity(),
                "Disconnected");
        if (getConnectedEndpoints().isEmpty()) {

        }
    }

    @Override
    protected void onConnectionFailed(Endpoint remove) {
        if (!getDiscoveredEndpoints().isEmpty()) {
            connectToDevice(randomElem(getDiscoveredEndpoints()));
        }
    }


    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        allowIncomingRequest(endpoint);
    }


    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Helpers.success(
                getActivity(),
                "Connected to " + endpoint.getId()
        );
    }

    @Override
    protected void onAdvertisingStarted() {

    }

    @Override
    protected void onAdvertisingFailed() {

    }


    @Override
    public void hearShake() {
        if (nearbyShare.isSelected() == true) {
            Toast.makeText(getContext(), "Disable advertise mode", Toast.LENGTH_LONG).show();
        }


        if (nearbyShare.isSelected() != true && isOpen != true) {
            isOpen = true;
            FragmentManager fragmentManager = getChildFragmentManager();
            Nearby nearby = new Nearby();
            nearby.show(fragmentManager, nearby.getTag());
            shakeDetector.stop();
            //startDeviceSearch();
        }
        shakeDetector.stop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        // Mapbox requires a style in order to work
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11")
                , style -> {
                    // removing the compass on the map
                    mapboxMap.getUiSettings().setCompassEnabled(false);
                    mapboxMap.setMinZoomPreference(16.0);
                    enableLocationComponent(style);
                });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        return true;
    }


    private class HomeFragmentLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<HomeFragment> homeFragmentWeakReference;

        private HomeFragmentLocationCallback(HomeFragment homeFragment) {
            this.homeFragmentWeakReference = new WeakReference<>(homeFragment);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            HomeFragment homeFragment = homeFragmentWeakReference.get();
            if (homeFragment != null) {

                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                userLocation = result.getLastLocation();
                if (mapboxMap != null && result.getLastLocation() != null) {
                    mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }

            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

    private void enableLocationComponent(@NonNull Style mapStyle) {
        // Check if the permissions are enabled
        // If the permissions are not enabled, request them

        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            // Helps us to customize the location component
            LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(requireContext()).elevation(5)
                    .accuracyAlpha(.6f)
                    .accuracyColor(Color.WHITE)
                    .foregroundDrawable(R.drawable.ic_baseline_circle)
                    .build();
            // Get an instance of the location component from mapBox
            locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), mapStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.addOnLocationClickListener(this);
            locationComponent.addOnCameraTrackingChangedListener(this);
            initLocationEngine();
            System.out.println("USER LOCATION " + locationComponent.getLastKnownLocation());
            //enableMarkerView(Objects.requireNonNull(locationComponent.getLastKnownLocation()));
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }


    }

    private void initLocationEngine() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext());

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), "Location required", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCameraTrackingDismissed() {

    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @SuppressWarnings("unchecked")
    private static <T> T randomElem(Collection<T> collection) {
        return (T) collection.toArray()[new Random().nextInt(collection.size())];
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationComponentClick() {

    }
}