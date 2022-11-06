package com.example.escortme.utils;

import android.animation.Animator;

import com.example.escortme.R;
import com.example.escortme.driverApp.DriverMainActivity;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
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

import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.animation.LinearInterpolator;
import com.mapbox.turf.TurfMeasurement;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// This class will assist in showing the driver location updates on the map
public class DriverLocationTracking {
    private static final String DOT_SOURCE_ID = "dot-source-id-route";
    private static final String LINE_SOURCE_ID = "line-source-id-route";
    private GeoJsonSource pointSource;
    private GeoJsonSource lineSource;
    private List<Point> routeCoordinateList;
    private List<Point> markerLinePointList = new ArrayList<>();
    private int routeIndex;

    // Should Inject the driver locations
    public DirectionsRoute directionsRoute;

    public void setDirectionsRoute(DirectionsRoute route){
        this.directionsRoute = route;
    }

    public DirectionsRoute getDirectionsRoute() {
        return directionsRoute;
    }

    public Animator currentAnimator;


    private MapboxMap mapboxMap;
    private Context context;
    public DriverLocationTracking(Context context, MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;
        this.context = context;
    }
    // Add data to the map once the GeoJSON is loaded
    public void initializeData(Style fullyLoadedStyle, @NonNull FeatureCollection featureCollection){
        if (featureCollection.features() != null) {
            LineString lineString = ((LineString) featureCollection.features().get(0).geometry());
            if (lineString != null) {
                routeCoordinateList = lineString.coordinates();
                initializeSources(fullyLoadedStyle, featureCollection);
                initializeSymbolLayer(fullyLoadedStyle);
                initializeDotLinePath(fullyLoadedStyle);
                animateIcon();
            }
        }
    }
    private void initializeDotLinePath(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayerBelow(new LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#212638")),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(4f)), "road-label");
    }
    // Add marker icon on a SymbolLayer
    private void initializeSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage("moving-red-marker", BitmapFactory.decodeResource(
                context.getResources(), R.drawable.map_default_map_marker_shadow));
        loadedMapStyle.addLayer(new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID).withProperties(
                iconImage("moving-red-marker"),
                iconSize(1f),
                iconOffset(new Float[] {5f, 0f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private void initializeSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
        loadedMapStyle.addSource(pointSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection));
        loadedMapStyle.addSource(lineSource = new GeoJsonSource(LINE_SOURCE_ID));
    }
    // Moving icon along the route
    private void animateIcon() {
        if ((routeCoordinateList.size() - 1 > routeIndex)) {
            Point indexPoint = routeCoordinateList.get(routeIndex);
            Point newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude());
            currentAnimator = createLatLngAnimator(indexPoint, newPoint);
            currentAnimator.start();
            routeIndex++;
        }
    }
    private Animator createLatLngAnimator(Point currentPosition, Point targetPosition) {
        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new PointEvaluator(), currentPosition, targetPosition);
        latLngAnimator.setDuration((long) TurfMeasurement.distance(currentPosition, targetPosition, "meters"));
        latLngAnimator.setInterpolator(new LinearInterpolator());
        latLngAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animateIcon();
            }
        });
        latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                pointSource.setGeoJson(point);
                markerLinePointList.add(point);
                lineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerLinePointList)));
            }
        });

        return latLngAnimator;
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
    public void removeLineFromMap(final int statusValue){
        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer("line-layer-id");
            if (layer != null) {
                layer.setProperties(PropertyFactory.visibility(
                        statusValue == 0 ? Property.NONE : Property.VISIBLE
                ));

            }

            System.out.println("mapbox style " + layer);
        });
    }
    // Make a request to the Mapbox directions Api
    // If successful pass the route to the route layer
    // The method requires the origin as well as the destination coordinates
    public void getRouteFromOriginToDestination(final Point originPoint, final Point destinationPoint){
        MapboxDirections client = MapboxDirections.builder()
                .origin(originPoint)
                .destination(destinationPoint)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());
                Timber.d("Response code: %s", response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, check access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

                // The route of the directions
                DirectionsRoute currentRoute  = response.body().routes().get(0);
                // setDirectionsRoute(currentRoute);
                DriverMainActivity.driverDriverRoute = response.body().routes().get(0);

                mapboxMap.getStyle(style -> {
                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                            new LatLngBounds.Builder()
                                    .include(new LatLng(originPoint.latitude(), originPoint.longitude()))
                                    .include(new LatLng(destinationPoint.latitude(), destinationPoint.longitude()))
                                    // used 400 before
                                    .build(), 200), 2000);

                    initializeData(style, FeatureCollection.fromFeature(
                            Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                });


            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Timber.e("Error: %s", t.getMessage());
                System.out.println("Directions request has failed");
            }
        });


    }


    public static DirectionsRoute getFromTrackingMain(DirectionsRoute route){
        return route;
    }


}
