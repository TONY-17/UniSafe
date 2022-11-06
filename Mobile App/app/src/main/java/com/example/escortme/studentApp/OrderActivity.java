package com.example.escortme.studentApp;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.IRtcEngineEventHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.escortme.SplashScreen;
import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.ActivityOrderBinding;
import com.example.escortme.driverApp.rating.Rating;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.network.model.TripRequest;
import com.example.escortme.studentApp.ui.home.HomeFragment;
import com.example.escortme.utils.DriverLocationTracking;
import com.example.escortme.utils.Helpers;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity implements OnMapReadyCallback {

    // used to draw the route from the origin to destination
    private DriverLocationTracking driverLocationTracking;

    // should be type Location
    private Location currentLocation;
    private LatLng userDestination;

    // Current mapBox map style
    Style mapStyle;
    // Holds the map layout
    private MapView mapView;
    // Instance of the map
    private MapboxMap mapboxMap;

    private PubNub pubnub;
    TextView driverStatus;
    String userChannel;
    String pickupAddress;
    String destinationAddress;
    Long studentIdLong = InitialActivity.currentUserID;
    LatLng destinationCord;

    MarkerView originView;
    MarkerViewManager originViewManager;
    MarkerView destinationView;
    MarkerViewManager destinationViewManager;
    LinearProgressIndicator linearProgressIndicator;

    CircularProgressIndicator circularProgressIndicator;
    LatLng driverLocation;

    boolean firstTime = true;
    Vibrator mVibrator;
    TextView driverCount;
    private static final int PERMISSION_REQ_ID = 22;
    MaterialCardView incomingCallBanner;
    public static Long driverId;

    // Fields required to use Agora audio call
    private String appId = "d6bcbcf83d1d40c5815ecd46141620f3";
    private String token = "c2d814b41ef445a38d41a36450c2b058";
    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
    };
    TripRequest tripRequest;
    ShimmerFrameLayout shimmerFrameLayout;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOrderBinding binding = ActivityOrderBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Make full screen
        getWindow().setStatusBarColor(Color.WHITE);

        incomingCallBanner = findViewById(R.id.incomingCallBanner);
        // Modal -> Ride details
        driverCount = findViewById(R.id.universityDriverCount);
        universityDriverCount();


        TextView university = findViewById(R.id.universityName);
        university.setText(InitialActivity.organisation);


        pickupAddress = getIntent().getStringExtra("Pick Up point");
        destinationAddress = getIntent().getStringExtra("Destination point");
        destinationCord = getIntent().getParcelableExtra("User destination");


        tripRequest = new TripRequest();
        setUpRequest(tripRequest);

        // MapView layout
        mapView = binding.mapViewOrder;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        circularProgressIndicator = findViewById(R.id.circularProgressIndicator);
        //getStudentIdFromUserId(this);
        // Shows the progress until a driver has been allocated
        linearProgressIndicator = findViewById(R.id.linearProgressIndicator);
        // Indicates the status of the driver
        driverStatus = findViewById(R.id.textView3);
        // Instance of the shimmer effect
        // Run the effect until a driver has been found
        shimmerFrameLayout = findViewById(R.id.sFL);
        // Holds the driver details bottom sheet, the initial is set to hidden
        CoordinatorLayout container = binding.containerCL;


        MaterialCardView rideDetailsContainer = findViewById(R.id.rideDetailsContainer);

        // Bottom sheet to display  information about the assigned driver details
        MaterialCardView driverDetailsBottomSheet = findViewById(R.id.materialCardView8);


        // Button to go back to the main map activity
        // Store the current trip in Shared preferences so they can be able to return to the trip
        MaterialCardView backButton = binding.backToLocations;
        backButton.setOnClickListener(view12 -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyRequests", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("CHANNEL", tripRequest.getChannel());
            editor.putString("DEST", tripRequest.getDestination());
            editor.apply();

            startActivity(new Intent(this,Home.class));
            //OrderActivity.super.onBackPressed();
        });


        // Parent constraint layout
        @SuppressLint("CutPasteId") ConstraintLayout bottomSheet = findViewById(R.id.ride_details);
        // Behaviour of the bottom sheet that holds the driver details
        BottomSheetBehavior<ConstraintLayout> bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet);
        // Initial state of the bottom sheet
        bottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                System.out.println("Bottom sheet state " + newState);
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                        backButton.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        backButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        MaterialButton cancel = findViewById(R.id.studentCancelTrip);
        cancel.setOnClickListener(view13 -> {

            SharedPreferences sharedPreferences = getSharedPreferences("MyRequests", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            finish();

        });
        // Book button click
        // Allocate a driver to a student
        MaterialButton book = binding.orderNow;
        book.setOnClickListener(v -> {
            // Remove markers and line

            circularProgressIndicator.setVisibility(View.VISIBLE);
            clearMap(originViewManager, originView, destinationView, destinationViewManager);

            // The current (Ride details) bottom sheet is disabled
            rideDetailsContainer.setVisibility(View.GONE);
            // Driver bottom sheet will be visible
            bottomSheet.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
            // start shimmer effect
            shimmerFrameLayout.startShimmer();
            // Animate to drivers current position
            Point point = Point.fromLngLat(destinationCord.getLongitude(), destinationCord.getLatitude());
            updateCameraPosition(point, 8);

            System.out.println("TRIP REQUEST " + tripRequest);

            Long organisationId = InitialActivity.orgId;
            //Long organisationId = 24L;
            System.out.println("STUDENT LONG ID" + studentIdLong + " " + organisationId);
            Call<ResponseBody> request = RetrofitClient.getRetrofitClient().getAPI().requestEscort(organisationId, studentIdLong, tripRequest);
            request.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            System.out.println("REQUEST RESPONSE " + data);
                            Helpers.success(OrderActivity.this, "You have successfully booked.");
                            Point point1 = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
                            updateCameraPosition(point1, 14);
                            //updateCameraPosition(currentLocation,17);
                            shimmerFrameLayout.stopShimmer();
                            driverDetailsBottomSheet.setVisibility(View.VISIBLE);
                            updateDriverDetailsBottomSheet(data);
                            shimmerFrameLayout.setVisibility(View.GONE);
                            //linearProgressIndicator.setVisibility(View.GONE);
                            driverStatus.setText("Driver found");
                            Handler handler = new Handler();
                            handler.postDelayed(() -> driverStatus.setText("Waiting for driver..."), 2000);
                            initPubNub();

                        } catch (IOException | PubNubException | JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            Helpers.failure(OrderActivity.this, "Failed to request escort: " + response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Helpers.failure(OrderActivity.this, "Server error" + t.getMessage());
                }
            });
        });
        Chronometer callDuration = findViewById(R.id.callDuration);
        final boolean[] answered = {false};
        MaterialCardView declineCall = findViewById(R.id.declineIncomingCall);
        declineCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomingCallBanner.setVisibility(View.GONE);
                if (answered[0]) {
                    callDuration.stop();
                    mRtcEngine.leaveChannel();
                    mRtcEngine.destroy();
                }

            }
        });
        MaterialCardView answerCall = findViewById(R.id.answerIncomingCall);
        TextView callStatus = findViewById(R.id.textView57);
        Chronometer chronometer = findViewById(R.id.callDuration);

        answerCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDuration.start();
                answered[0] = true;
                initAndJoin(userChannel);
                callStatus.setText("Ongoing call ");
                chronometer.setVisibility(View.VISIBLE);
                chronometer.start();
            }
        });


        MaterialCardView shareWithContacts = findViewById(R.id.shareCurrentLocation);
        shareWithContacts.setOnClickListener(view1 -> {
            String uri = "https://www.google.com/maps/?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, uri);
            startActivity(Intent.createChooser(sharingIntent, "Share in..."));
        });
    }

    private void universityDriverCount() {
        Call<ResponseBody> orgSize = RetrofitClient.getRetrofitClient().getAPI().getOrgSize(InitialActivity.orgId);
        orgSize.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        System.out.println("UNIV COUNT" + data);
                        //JSONObject jsonObject = new JSONObject(data);
                        try{
                            int size = Integer.valueOf(data);
                            driverCount.setText(size + " Drivers Available");

                        }catch (NumberFormatException e){
                            Helpers.Alert(
                                    OrderActivity.this,
                                    "No drivers",
                                    "Feature not yet available"
                            );
                            finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


    }

    private void updateDriverDetailsBottomSheet(String data) throws JSONException {
        shimmerFrameLayout.setVisibility(View.GONE);
        System.out.println("DRIVER DETAILS PAGE " + data);
        JSONObject jsonObject = new JSONObject(data);

        JSONObject tripDetails = jsonObject.getJSONObject("trip");

        JSONObject driverDetails = jsonObject.getJSONObject("driver");
        driverId = driverDetails.getLong("id");
        String firstName = driverDetails.getString("firstName");
        String lastName = driverDetails.getString("lastName");
        String tempRating = jsonObject.getString("rating");
        String tempTotalReviews = jsonObject.getString("totalReviews");
        TextView driverName = findViewById(R.id.driverName);
        driverName.setText(firstName.toUpperCase() + " " + lastName.toUpperCase());


        ImageView driverProfile = findViewById(R.id.driverImgBS);
        String tempProfile = jsonObject.getString("driverImage");
        Picasso.get().load(tempProfile).into(driverProfile);

        TextView estimatedTime = findViewById(R.id.estimatedTimeBS);
        Location destination =  new Location("");
        destination.setLatitude(userDestination.getLatitude());
        destination.setLongitude(userDestination.getLongitude());
        estimatedTime.setText(Helpers.estimatedTime(currentLocation, destination));
        TextView initLocation = findViewById(R.id.driverInitLoc);
        initLocation.setText(tripDetails.getString("pickUp"));
        TextView destLocation = findViewById(R.id.driverDestLoc);
        destLocation.setText(tripDetails.getString("destination"));
        TextView driverRating = findViewById(R.id.driverDetailsBS);
        driverRating.setText(tempRating + " Rating • "+
                tempTotalReviews + " Reviews");
        TextView passengers = findViewById(R.id.passengers);
        passengers.setText("You might share this trip with other students going your way.");

        TextView driverNameInitial = findViewById(R.id.driverNameInitial);
        driverNameInitial.setText(String.valueOf(firstName.charAt(0)));

    }


    private void setUpRequest(TripRequest tripRequest) {
        tripRequest.setPickUp(pickupAddress);
        tripRequest.setDestination(destinationAddress);
        tripRequest.setPickUpPoint(pickupAddress);
        tripRequest.setDestinationPoint(destinationAddress);
        tripRequest.setDateCreated("string");
        tripRequest.setCompleted(false);
        tripRequest.setAccepted(false);
        tripRequest.setCancelled(false);
        userChannel = "ch" + Helpers.generatePassword();
        tripRequest.setChannel(userChannel.toLowerCase());
    }


    // BOTTOM-SHEET UPDATE


    @Override
    public void onBackPressed() {
        super.onBackPressed();
/*        SharedPreferences sharedPreferences = getSharedPreferences("MyRequests", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("CHANNEL", tripRequest.getChannel());
        editor.putString("DEST", tripRequest.getDestination());
        editor.apply();
        startActivity(new Intent(this,Home.class));*/

    }

    private void initPubNub() throws PubNubException {
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
                //sendMessage(InitialActivity.username);
                final String msg = pnMessageResult.getMessage().toString().replace("\"", "");

                System.out.println("EVERY MSG " + msg);
                if (msg.equals("Trip completed")) {
                    runOnUiThread(() -> {
                        pubnub.disconnect();
                        mVibrator.vibrate(2000l);
                        //notifyUser();
                        startActivity(new Intent(OrderActivity.this, Rating.class));
                        finish();
                    });
                } else if (msg.equals("Incoming Call")) {
                    runOnUiThread(() -> {
                        mVibrator.vibrate(2000l);
                        //notifyUser();
                        incomingCallBanner.setVisibility(View.VISIBLE);
                        // Allow driver and student to be able to talk

                    });
                } else if (msg.equals("Trip Cancelled")) {
                    runOnUiThread(() -> {
                        pubnub.disconnect();
                        mVibrator.vibrate(2000l);
                        //notifyUser();
                        startActivity(new Intent(OrderActivity.this, Home.class));
                        finish();
                        Helpers.Alert(OrderActivity.this,"Trip", "Driver cancelled trip");
                    });
                } else {  /*
                 * Will receive a string location in the form of 23.00,-22.00
                 * This line splits the string into a lat and lng
                 */
                    String[] msgArr = msg.split(",");
                    double longitude = Double.parseDouble(msgArr[0]);
                    double latitude = Double.parseDouble(msgArr[1]);
                    driverLocation = new LatLng(latitude, longitude);
                }
                // Point driver = Point.fromLngLat(driverLocation.getLongitude(), driverLocation.getLatitude());
                runOnUiThread(() -> {
                    if (firstTime) {
                        firstTime = false;
                        driverStatus.setText("Trip is in progress");
                        circularProgressIndicator.setVisibility(View.GONE);
                        linearProgressIndicator.setVisibility(View.GONE);

                        Point driver = Point.fromLngLat(driverLocation.getLongitude(), driverLocation.getLatitude());
                        Point student = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
                        // Generate the route
                        //driverLocationTracking.getRouteFromOriginToDestination(student, driver);
                        // Add the markerViews
                        //markerViewsSetup(driver,student,"Your Location",null);
                    }
                    // updateCameraPosition(driver, 8);
                    updateMarkerPosition(driverLocation);
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

        pubnub.subscribe().channels(Collections.singletonList(userChannel.toLowerCase())).execute();


    }

    // Removes the markers and route on the map
    private void clearMap(MarkerViewManager origin, MarkerView o, MarkerView d, MarkerViewManager destination) {
        origin.removeMarker(o);
        destination.removeMarker(d);
        driverLocationTracking.removeLineFromMap(0);
    }

    // Given the coordinates and zoom level animate to that location
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        OrderActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                style -> {
                    mapStyle = style;

                    // Disable the compass icon
                    mapboxMap.getUiSettings().setCompassEnabled(false);

                    currentLocation = HomeFragment.userLocation;
                    userDestination = getIntent().getParcelableExtra("User destination");

                    System.out.println("userDestination" + userDestination.toString());
                    driverLocationTracking = new DriverLocationTracking(OrderActivity.this, mapboxMap);


                    Point initial = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
                    Point destination = Point.fromLngLat(userDestination.getLongitude(), userDestination.getLatitude());


                    driverLocationTracking.getRouteFromOriginToDestination(initial, destination);

                    // Adds the custom marker layout to show the places
                    markerViewsSetup(
                            initial,
                            destination,
                            pickupAddress,
                            destinationAddress);

                    // If we have received a driver location from pubNup add the marker on the map

                    initDriverIcon(style);

                });
    }


    private void markerViewsSetup(Point currentLocation,
                                  Point destination,
                                  String currentName,
                                  String destinationName) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(OrderActivity.this).inflate(R.layout.marker_view_origin, null, false);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // Set the layouts content
        TextView title = view.findViewById(R.id.marker_window_title);
        title.setText(currentName);


        originView = new MarkerView(new LatLng(currentLocation.latitude(), currentLocation.longitude()), view);
        originViewManager = new MarkerViewManager(mapView, mapboxMap);
        originViewManager.addMarker(originView);
        originViewManager.onCameraDidChange(true);


        @SuppressLint("InflateParams") View viewDestination = LayoutInflater.from(OrderActivity.this).inflate(R.layout.marker_view_destination, null, false);
        viewDestination.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView marker_window_title = viewDestination.findViewById(R.id.marker_window_title);
        marker_window_title.setText(destinationName);

        destinationView = new MarkerView(new LatLng(destination.latitude(), destination.longitude()), viewDestination);
        destinationViewManager = new MarkerViewManager(mapView, mapboxMap);
        destinationViewManager.addMarker(destinationView);
        destinationViewManager.onCameraDidChange(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    // Add a marker (denotes the driver position) to the map
    private void initDriverIcon(@NonNull Style style) {

        /* style.addImage("driver-icon-id",
                BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.icondrive));
                        */

        Drawable driverIcon = AppCompatResources.getDrawable(this, R.drawable.driver_icon_layer);
        style.addImage("driver-icon-id", driverIcon);

        style.addSource(new GeoJsonSource("source-id"));

        style.addLayer(new SymbolLayer("layer-id", "source-id").withProperties(
                iconImage("driver-icon-id"),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(.7f)
        ));


    }


    private void updateMarkerPosition(LatLng position) {
        // Update marker position when receiving updates
        if (mapboxMap.getStyle() != null) {
            GeoJsonSource driverLocationSource = mapboxMap.getStyle().getSourceAs("source-id");
            if (driverLocationSource != null) {
                driverLocationSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }
        // Animate the camera to the new position that we are receiving
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }


    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }


    private void initAndJoin(String channelName) {
        try {
            mRtcEngine = RtcEngine.create(this, appId, mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Check the error");
        }
        mRtcEngine.joinChannel(token, channelName, "", 0);
    }


    // Bottom sheet update UI
    /*
    -> Show predicted arrival time
    -> Allow students to share the trip
    -> Add safety tips
    -> Update location puck with custom locations

     */


}