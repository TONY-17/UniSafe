package com.example.escortme.utils;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.escortme.R;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class MovingDriverWithTrailingLine extends AppCompatActivity {

    private static MapboxMap mapboxMap;
    private MapView mapView;
    private static Context context;
    private static final String DOT_SOURCE_ID = "driver-dot-id";
    private static final String LINE_SOURCE_ID = "driver-line-id";
    private static GeoJsonSource pointGeoJsonSource;
    private static GeoJsonSource lineGeoJsonSource;
    private static List<Point> routeList;
    private static List<Point> markerList = new ArrayList<>();
    private static int routeIndx;
    private Point driverCurrentLocation;
    private Point studentPickUpLocation;
    private static Animator animator;

    public MovingDriverWithTrailingLine(Context context, MapView mapView, MapboxMap mapboxMap,
                                        Point driverCurrentLocation, Point studentPickUpLocation) {
        this.mapView = mapView;
        this.mapboxMap = mapboxMap;
        this.context = context;
        this.driverCurrentLocation = driverCurrentLocation;
        this.studentPickUpLocation = studentPickUpLocation;
    }

    private static class PointEvaluator implements TypeEvaluator<Point> {

        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            return Point.fromLngLat(
                    startValue.longitude() + ((endValue.longitude() - startValue.longitude()) * fraction),
                    startValue.latitude() + ((endValue.latitude() - startValue.latitude()) * fraction)
            );
        }
    }

    public static void initData(Style fullyLoadedStyle, @NonNull FeatureCollection featureCollection) {
        if (featureCollection.features() != null) {
            LineString lineString = ((LineString) featureCollection.features().get(0).geometry());
            if (lineString != null) {
                routeList = lineString.coordinates();
                initSources(fullyLoadedStyle, featureCollection);
                initSymbolLayer(fullyLoadedStyle);
                initDotLinePath(fullyLoadedStyle);
                animation();
            }
        }
    }

    private static void animation() {
        if ((routeList.size() - 1 > routeIndx)) {
            Point indexPoint = routeList.get(routeIndx);
            Point newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude());
            animator = createLatLngAnimator(indexPoint, newPoint);
            animator.start();
            routeIndx++;
        }
    }

    private static Animator createLatLngAnimator(Point currentPosition, Point targetPosition) {
        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new PointEvaluator(), currentPosition, targetPosition);
        latLngAnimator.setDuration((long) TurfMeasurement.distance(currentPosition, targetPosition, "meters"));
        latLngAnimator.setInterpolator(new LinearInterpolator());
        latLngAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation();
            }
        });
        latLngAnimator.addUpdateListener(animation -> {
            Point point = (Point) animation.getAnimatedValue();
            pointGeoJsonSource.setGeoJson(point);
            markerList.add(point);
            lineGeoJsonSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerList)));
        });

        return latLngAnimator;
    }

    public void getRoute() {
        MapboxDirections client = MapboxDirections.builder()
                .origin(driverCurrentLocation)
                .destination(studentPickUpLocation)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());

                // You can get the generic HTTP info about the response
                Timber.d("Response code: %s", response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

                // Get the directions route
                DirectionsRoute currentRoute = response.body().routes().get(0);
                mapboxMap.getStyle(style -> {
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(driverCurrentLocation.latitude(), driverCurrentLocation.longitude())) // Sets the new camera position
                            .zoom(14)
                            .bearing(180) // Rotate the camera
                            .tilt(30) // Set the camera tilt -> 30
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 7000);



                    initData(style, FeatureCollection.fromFeature(
                            Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                });
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: %s", throwable.getMessage());
                /*Toast.makeText(MovingDriverWithTrailingLine.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    private static void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
        loadedMapStyle.addSource(pointGeoJsonSource = new GeoJsonSource(String.valueOf(mapboxMap.getStyle().getSourceAs("source-id")), featureCollection));
        loadedMapStyle.addSource(lineGeoJsonSource = new GeoJsonSource(LINE_SOURCE_ID));
    }


    private static void initSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("moving-red-marker", BitmapFactory.decodeResource(
                context.getResources(), R.drawable.map_default_map_marker));
        loadedMapStyle.addLayer(new SymbolLayer("driver-symbol-layer-id", DOT_SOURCE_ID).withProperties(
                iconImage("moving-red-marker"),
                iconSize(1f),
                iconOffset(new Float[]{5f, 0f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private static void initDotLinePath(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayerBelow(new LineLayer("driver-line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#212638")),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(4f)), "road-label");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
