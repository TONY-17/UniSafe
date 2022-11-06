package com.example.escortme.driverApp.ui.driverHome;

import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.DriverCallingActivity;
import com.example.escortme.driverApp.DriverHome;
import com.example.escortme.driverApp.Emergency;
import com.example.escortme.driverApp.rating.Rating;
import com.example.escortme.driverApp.ui.driverHome.trips.Trip;
import com.example.escortme.driverApp.ui.driverHome.trips.TripAdapter;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.OrderActivity;
import com.example.escortme.utils.DriverLocationTracking;
import com.example.escortme.utils.HeatMap;
import com.example.escortme.utils.Helpers;
import com.example.escortme.utils.MapUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
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
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.CannotAddSourceException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.ncorti.slidetoact.SlideToActView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHomeFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    RecyclerView recyclerView;
    static MapView mapView;
    private static MapboxMap mapboxMap;
    private final LocationCallback callback = new LocationCallback(this);
    // used to draw the route from the origin to destination
    // private DriverLocationTracking driverLocationTracking;
    static Location location;
    static String channel;
    public static boolean openRatingPage = false;
    String studentPickUp;
    String studentDestination;
    String student;

    View requestsBottomSheet;
    static ViewGroup parentCL;
    View messageBS;
    private static PubNub pubnub;

    TextView studentInDanger;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearProgressIndicator linearProgressIndicator;
    CircularProgressIndicator circularProgressIndicator;
    //TextView tripStatusTxt;
    TextView studentInProgressName;
    TextView currentStudentInit;
    MaterialCardView tripStatusCV;
    MaterialCardView completeTrip;
    SlideToActView slideToCompleteTrip;
    MaterialCardView doneHelpingStudent;
    // flags when a driver needs to stop broadcasting their location
    boolean stopBroadcast = false;

    // Illustration to show when there is no data
    ImageView illustration;
    TextView iText1, iText2;

    private static final int PERMISSION_REQ_ID = 22;
    // Global channel for emergencies
    private String emergencyChannel = "escortMeEmergencies";
    Vibrator mVibrator;
    // This is the location of the emergency
    private String EmergencyLocation;
    private String EmergencyUser;
    private String EmergencyAddress;

    MaterialCardView cancelOngoingTrip;
    MaterialCardView unavailableStudent;
    TextView studentTripDestination;

    // Fields required to use Agora audio call
    private String appId = "d6bcbcf83d1d40c5815ecd46141620f3";
    private String token = "c2d814b41ef445a38d41a36450c2b058";
    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
    };

    TextView sheetInitialLocation;
    TextView sheetDestinationLocation;
    List<Trip> tripList;

    @SuppressLint({"NotifyDataSetChanged", "ResourceAsColor"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.driver_home_fragment, container, false);

        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        /*
         * Illustration to show when there is no data
         */
        sheetInitialLocation = view.findViewById(R.id.studentPickupLocation);
        sheetDestinationLocation = view.findViewById(R.id.textView67);
        illustration = view.findViewById(R.id.imageView8);
        iText1 = view.findViewById(R.id.textView4);
        iText2 = view.findViewById(R.id.iText2);

        studentInDanger = view.findViewById(R.id.studentInEmergency);
        // ** ENDS HERE ** //
        completeTrip = view.findViewById(R.id.completeTrip);
        slideToCompleteTrip = view.findViewById(R.id.slideToComplete);
        doneHelpingStudent = view.findViewById(R.id.doneHelpingStudent);
        //tripStatusTxt = view.findViewById(R.id.tripStatusTxt);
        studentInProgressName = view.findViewById(R.id.studentInProgressName);
        currentStudentInit = view.findViewById(R.id.currentStudentInit);
        //studentTripDestination = view.findViewById(R.id.studentDestination);
        tripStatusCV = view.findViewById(R.id.tripStatusCV);
        tripStatusCV.setVisibility(View.VISIBLE);

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator);
        recyclerView = view.findViewById(R.id.requestList);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        getActivity().getWindow().setStatusBarColor(Color.WHITE);


        requestsBottomSheet = view.findViewById(R.id.requestsBS);
        parentCL = view.findViewById(R.id.parentCL);

        /*
         * Emergencies variables from Emergency.java
         */
        EmergencyLocation = requireActivity().getIntent().getStringExtra("emergencyLocation");
        EmergencyAddress = requireActivity().getIntent().getStringExtra("emergencyAddress");
        EmergencyUser = requireActivity().getIntent().getStringExtra("emergencyUser");


        channel = requireActivity().getIntent().getStringExtra("Channel");
        studentPickUp = requireActivity().getIntent().getStringExtra("initial");
        studentDestination = requireActivity().getIntent().getStringExtra("final");
        student = requireActivity().getIntent().getStringExtra("student");

        sheetInitialLocation.setText(studentPickUp);

        sheetDestinationLocation.setText(studentDestination);

        String uuid = "escortMeUUID";
        PNConfiguration pnConfiguration = null;
        try {
            pnConfiguration = new PNConfiguration(uuid);
        } catch (PubNubException e) {
            e.printStackTrace();
        }
        pnConfiguration.setPublishKey(getString(R.string.publish_key));
        pnConfiguration.setSubscribeKey(getString(R.string.subscribe_key));
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);
        subscribeToEmergencyChannel();


        linearProgressIndicator = view.findViewById(R.id.linearProgressIndicator2);
        shimmerFrameLayout = view.findViewById(R.id.sFL);
        shimmerFrameLayout.startShimmer();

        if (TripAdapter.clicked) {
            getAllTrips();
            //circularProgressIndicator.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.postDelayed(() -> circularProgressIndicator.setVisibility(View.GONE), 4000);
        }

        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.custom_complete_animated); // Insert your AnimatedVectorDrawable resource identifier
        ImageView animatedIV = view.findViewById(R.id.animatedIMGV);
        slideToCompleteTrip.setBumpVibration(50);
        slideToCompleteTrip.setOnSlideCompleteListener(view14 -> {
            animatedIV.setVisibility(View.VISIBLE);
            animatedIV.setImageDrawable(d);
            d.start();
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                stopBroadcast = true;
                acceptTrip();
                sendMessage("Trip completed", channel);
                pubnub.disconnect();
                // Allow drivers to rate students

            }, 500);

        });

        doneHelpingStudent.setOnClickListener(v -> {
            getActivity().runOnUiThread(() -> {
                startActivity(new Intent(getContext(), DriverHome.class));
                getActivity().finish();
            });
        });


        Chronometer chronometer = view.findViewById(R.id.chronometer23);
        chronometer.start();

        MaterialCardView locationsContainer = view.findViewById(R.id.locationsContainer);
        ImageButton closeLocations = view.findViewById(R.id.closeLocations);
        ImageButton openLocations = view.findViewById(R.id.openLocations);


        closeLocations.setOnClickListener(view1 -> {
            locationsContainer.setVisibility(View.GONE);
            closeLocations.setVisibility(View.GONE);
            openLocations.setVisibility(View.VISIBLE);
        });


        openLocations.setOnClickListener(view1 -> {
            locationsContainer.setVisibility(View.VISIBLE);
            closeLocations.setVisibility(View.VISIBLE);
            openLocations.setVisibility(View.GONE);
        });
        unavailableStudent = view.findViewById(R.id.unavailableStudent);

        cancelOngoingTrip = view.findViewById(R.id.cancelOngoingTrip);
        cancelOngoingTrip.setOnClickListener(view12 -> {
            sendMessage("Trip Cancelled", channel);
            pubnub.disconnect();
            Long tripID = TripAdapter.trip;
            Call<ResponseBody> cancelTripRequest = RetrofitClient.getRetrofitClient().getAPI().cancelTrip(tripID);
            cancelTripRequest.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Helpers.success(getActivity(), "Trip cancelled");
                        startActivity(new Intent(getContext(), DriverHome.class));
                        getActivity().finish();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Helpers.failure(getActivity(), "Failed to cancel");
                    startActivity(new Intent(getContext(), DriverHome.class));
                    getActivity().finish();
                }
            });


        });


        // Allow drivers to call students
        MaterialCardView callStudent = view.findViewById(R.id.callCurrentUser);
        callStudent.setOnClickListener(view13 -> {
            if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)) {
                // Join the audio call channel
                initAndJoin(channel);
                // Alert the student that theres an incoming call
                sendMessage("Incoming Call", channel);
                Intent i = new Intent(getContext(), DriverCallingActivity.class);
                i.putExtra("studentName", student);
                startActivity(i);
            }
        });


        return view;
    }


    private void subscribeToEmergencyChannel() {

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final String msg = pnMessageResult.getMessage().toString().replace("\"", "");

                String[] msgReceived = msg.split(":");

                mVibrator.vibrate(4000l);
                if (getActivity() == null) {
                    Helpers.Alert(getActivity(), "Alert", msg);
                    Intent i = new Intent(getContext(), Emergency.class);

                    String student = msgReceived[0];
                    String address = msgReceived[1];
                    String location = msgReceived[2];

                    i.putExtra("user", student);
                    i.putExtra("address", address);
                    i.putExtra("slocation", location);
                    startActivity(i);
                    stopVibration();
                    return;
                } else {
                    //Toast.makeText(getContext(),msg,Toast.LENGTH_LONG).show();
                    getActivity().runOnUiThread(() -> {
                        Intent i = new Intent(getContext(), Emergency.class);
                        String student = msgReceived[0];
                        String address = msgReceived[1];
                        String location = msgReceived[2];
                        i.putExtra("user", student);
                        i.putExtra("address", address);
                        i.putExtra("slocation", location);
                        startActivity(i);
                        Helpers.Alert(getActivity(), "Alert", msg);
                        stopVibration();
                    });
                }
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
        pubnub.subscribe().channels(Collections.singletonList(emergencyChannel)).execute();
    }

    private void stopVibration() {
        Handler handler = new Handler();
        handler.postDelayed(() -> mVibrator.cancel(), 4000);
    }

    private void acceptTrip() {
        Long tripID = TripAdapter.trip;
        System.out.println("TRIP ID " + tripID);
        Call<ResponseBody> acceptTrip = RetrofitClient.getRetrofitClient().getAPI().acceptTrip(tripID);
        acceptTrip.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Helpers.success(getActivity(), "Completed");
                    // Take drivers to the rating page when the trip is done
                    openRatingPage = true;
                    Intent i = new Intent(getContext(), Rating.class);
                    startActivity(i);

                } else {
                    Helpers.failure(getActivity(), "Failed to update trip");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void IllustrationsVisibility(int visibility) {
        illustration.setVisibility(visibility);
        iText1.setVisibility(visibility);
        iText2.setVisibility(visibility);
    }

    private void getAllTrips() {
        Long driverId = InitialActivity.driverId;
        Call<ResponseBody> retrieveTripsCall = RetrofitClient.getRetrofitClient().getAPI().getAllRequests(driverId);
        retrieveTripsCall.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response != null) {
                        try {
                            assert response.body() != null;
                            String data = response.body().string();
                            System.out.println("TRIP INFO " + data);
                            JSONArray jsonArray = new JSONArray(data);
                            tripList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Long id = jsonObject.getLong("id");
                                String pickUp = jsonObject.getString("pickUp");
                                String destination = jsonObject.getString("destination");
                                String pickUpPoint = jsonObject.getString("pickUpPoint");
                                String destinationPoint = jsonObject.getString("destinationPoint");
                                String channel = jsonObject.getString("channel");
                                String dateCreated = jsonObject.getString("dateCreated");
                                String student = jsonObject.getString("student");

                                tripList.add(new Trip(
                                        id,
                                        pickUp,
                                        destination,
                                        pickUpPoint,
                                        destinationPoint,
                                        channel,
                                        true,
                                        dateCreated,
                                        student));
                            }
                            if (tripList.isEmpty()) {
                                IllustrationsVisibility(View.VISIBLE);
                            }

                            shimmerFrameLayout.stopShimmer();
                            linearProgressIndicator.setVisibility(View.GONE);
                            shimmerFrameLayout.setVisibility(View.GONE);

                            // Add this back to show trips
                            TripAdapter tripAdapter = new TripAdapter(tripList, false, false, getContext());

                            recyclerView.setAdapter(tripAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            tripAdapter.notifyDataSetChanged();


                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Helpers.failure(getActivity(), "Failed to retrieve requests");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(getActivity(), "Failed to retrieve requests");
            }
        });


    }

    private static void sendMessage(String msg, String channel) {
        pubnub.publish()
                .message(msg)
                .channel(channel)
                .async((result, status) -> {
                    // status.isError() to see if error happened and print status code if error
                    if (status.isError()) {
                        System.out.println("pub status code: " + status.getErrorData());
                    }
                });
    }

    private static void pubNubIncomingUpdates() {
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final String msg = pnMessageResult.getMessage().toString().replace("\"", "");

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
    }

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;

    @Override
    public void onResume() {
        // As soon as the activity becomes visible
        handler.postDelayed(runnable = () -> {
/*
            if(!tripList.isEmpty())
                IllustrationsVisibility(View.GONE);
*/

            getAllTrips();
            handler.postDelayed(runnable, delay);
        }, delay);

        super.onResume();
        mapView.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // mapView.onDestroy();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(requireContext(), "User location required", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_LONG).show();

        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        DriverHomeFragment.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            enableLocationComponent(style);
            mapboxMap.getUiSettings().setCompassEnabled(false);
            try {
                MapUtils.initStudentEmergencyIcon(style, getContext());
                HeatMap.initEmergencyLineLayer(mapboxMap);
                //HeatMap.initEmergencyCircleLayer(mapboxMap);
                HeatMap.retrieveEmergencyPoints(InitialActivity.orgIdDriver);
                HeatMap.queryEmergencyHotspots(location,
                        mapboxMap,
                        mapView,
                        getContext(),
                        getActivity()
                );

            } catch (CannotAddSourceException e) {

                for (Layer layer : mapboxMap.getStyle().getLayers()) {

                    mapboxMap.getStyle().removeLayer(layer);
                }

            }

            //driverLocationTracking = new DriverLocationTracking(getContext(), mapboxMap);

            /*
             * When a driver starts a trip a channel will be sent here
             */
            if (channel != null) {

                getActivity().getWindow().setStatusBarColor(Color.WHITE);
                System.out.println("PICK UP LOCATION " + location.getLongitude() + " , " + location.getLatitude());
                //animateToUserLocation(studentPickUp);
                //driverLocationTracking.getRouteFromOriginToDestination(currentLocation,destinationLocation);

                // Hide the bottom navigation view
                // and trip bottom sheet
                showBottomSheet(false, requestsBottomSheet, Gravity.BOTTOM);
                DriverHome.showBottomNavigationView(false);
                // showBottomSheet(true,messageBS,Gravity.BOTTOM);
                //showBottomSheet(false, messageBS, Gravity.BOTTOM);
                tripStatusCV.setVisibility(View.VISIBLE);
                /*tripStatusTxt.setText("Escorting " + student + " to " + studentDestination);*/
                currentStudentInit.setText(String.valueOf(student.charAt(0)));
                studentInProgressName.setText(student);
                //studentTripDestination.setText(studentDestination);
            }
            /*
             * This checks if we received an emergency
             */
            if (EmergencyAddress != null) {
                //sendMessage("Help on the way",emergencyChannel);
                showBottomSheet(false, requestsBottomSheet, Gravity.BOTTOM);
                DriverHome.showBottomNavigationView(false);
                //showBottomSheet(false, messageBS, Gravity.BOTTOM);
                tripStatusCV.setVisibility(View.GONE);
                tripStatusCV.setRadius(8f);

                // hide the button to complete a trip
                completeTrip.setVisibility(View.GONE);


                slideToCompleteTrip.setVisibility(View.GONE);
                // show the button to indicate a student has been assisted
                doneHelpingStudent.setVisibility(View.VISIBLE);
                //tripStatusCV.setBackgroundColor(Color.RED);

                //tripStatusTxt.setText("ASSISTING " + EmergencyUser + " AT " + EmergencyAddress);
                studentInDanger.setText("I have assisted " + EmergencyUser);
                studentInProgressName.setText(EmergencyUser);

                String[] receivedLocation = EmergencyLocation.split(",");
                double rLat = Double.parseDouble(receivedLocation[0]);
                double rLng = Double.parseDouble(receivedLocation[1]);

                LatLng position = new LatLng(rLat, rLng);
                updateMarkerPosition(position);

                cancelOngoingTrip.setVisibility(View.GONE);
                unavailableStudent.setVisibility(View.VISIBLE);
                unavailableStudent.setOnClickListener(view -> {
                    startActivity(new Intent(getContext(), DriverHome.class));
                    getActivity().finish();
                });

            }
        });

    }

    public static void showBottomSheet(boolean show, View target, int gravity) {
        // Bottom sheet transitions
        Transition transitionBS = new Slide(gravity);
        transitionBS.setDuration(600);
        transitionBS.addTarget(target);

        TransitionManager.beginDelayedTransition(parentCL, transitionBS);
        target.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    private void updateMarkerPosition(LatLng position) {
        // Update marker position when receiving updates
        if (mapboxMap.getStyle() != null) {
            GeoJsonSource studentLocationSource = mapboxMap.getStyle().getSourceAs("student-source-id");
            if (studentLocationSource != null) {
                studentLocationSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }

        // Animate the camera to the new position that we are receiving
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }


    public void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle).build());

            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.GPS);
            initLocationEngine();

        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    private void initLocationEngine() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext());

        long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
        long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());

        locationEngine.getLastLocation(callback);
    }


    private static class LocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<DriverHomeFragment> activityWeakReference;

        private LocationCallback(DriverHomeFragment mainActivity) {
            this.activityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            DriverHomeFragment activity = activityWeakReference.get();
            if (activity != null) {
                location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                animateToUserLocation(location, 0);
                System.out.println("My current location " + location);

                // HeatMap.retrieveEmergencyPoints(InitialActivity.orgIdDriver);
                HeatMap.queryEmergencyHotspots(location,
                        mapboxMap,
                        mapView,
                        activity.getContext(),
                        activity.getActivity()
                );
                /*
                 *  If we received a channel from the other activities then we should location updates to the room-channel
                 */

                if (channel != null) {
                    String strLocation = location.getLongitude() + "," + location.getLatitude();

                    sendMessage(strLocation, channel);

                    pubNubIncomingUpdates();
                    animateToUserLocation(location, 0);
                }

/*
                if (mapboxMap != null && result.getLastLocation() != null) {
                    mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }*/
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

    public static void animateToUserLocation(Location loc, double tilt) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(loc.getLatitude(), loc.getLongitude())) // Sets the new camera position
                .zoom(18)
                .bearing(180)
                .tilt(tilt)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 1000);

    }

    // Allow drivers to join the call channel
    private void initAndJoin(String channelName) {
        try {
            mRtcEngine = RtcEngine.create(getContext(), appId, mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Check the error");
        }
        mRtcEngine.joinChannel(token, channelName, "", 0);
    }


    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

}