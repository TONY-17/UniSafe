package com.example.escortme.utils;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.escortme.R;
import com.example.escortme.network.RetrofitClient;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.CannotAddSourceException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.turf.TurfMeasurement;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HeatMap {

    private static final String EMERGENCY_LINE_SOURCE_ID = "emergency-line-source-id";

    private static final String EMERGENCY_LINE_LAYER_ID = "emergency-line-layer-id";
    static JSONObject featureCollection = new JSONObject();
    private static final String LINE_COLOR = "#F13B6E";
    private static final float LINE_WIDTH = 2;


    private static Target target;
    private static boolean notificationExists;
    private static NotificationManager manager;
    private static NotificationCompat.Builder builder;
    private static int searchRadius = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1002;
    /*
     * This method will be used to retrieve all the emergency coordinates and store the result in a list
     */

    public static void retrieveEmergencyPoints(Long orgId) {
        // This stores all the coordinates in the form of lat and long
        List<EmergencyLatLng> emergencyLatLngList = new ArrayList<>();
        Call<ResponseBody> emergencies = RetrofitClient.getRetrofitClient().getAPI().getAllOrgEmergencies(orgId);
        emergencies.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String data = response.body().string();
                        System.out.println("This organisation has no emergency points" + data);
                        JSONArray jsonArray = new JSONArray(data);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            // "location":"-26.2599443,28.0393033"
                            String location = jsonObject.getString("location");
                            String locationArr[] = location.split(",");
                            double latitude = Double.parseDouble(locationArr[0]);
                            double longitude = Double.parseDouble(locationArr[1]);
                            // Store the response in the list we created in line 30
                            emergencyLatLngList.add(new EmergencyLatLng(String.valueOf(latitude), String.valueOf(longitude)));
                        }
                        convertJsonToGeoJson(emergencyLatLngList);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("This organisation has no emergency points");
                    Log.d("Emergencies", "This organisation has no emergency points");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Emergencies Error", "Can't retrieve emergency points");
            }
        });
    }


    /*
     * This method converts the JSON response received from the backend to a Geo-JSON
     * The Geo-Json will be used to render the heatmap
     */

    public static void convertJsonToGeoJson(List<EmergencyLatLng> emergencyLatLngList) throws JSONException {
        System.out.println("Geo-Json 3 " + emergencyLatLngList.toString());
        featureCollection.put("type", "FeatureCollection");

        JSONObject properties = new JSONObject();
        properties.put("name", "ESPG:4326");

        JSONObject crs = new JSONObject();
        crs.put("type", "name");
        crs.put("properties", properties);
        featureCollection.put("crs", crs);

        JSONArray features = new JSONArray();

        // We need to convert all the data in the list
        for (int i = 0; i < emergencyLatLngList.size(); i++) {
            if (emergencyLatLngList.get(i).getLat() != null) {
                JSONObject geometry = new JSONObject();
                JSONArray coordinates = new JSONArray();
                coordinates.put(0, emergencyLatLngList.get(i).getLat());
                coordinates.put(1, emergencyLatLngList.get(i).getLon());
                geometry.put("type", "Point");
                geometry.put("coordinates", coordinates);
                features.put(i, geometry);
                featureCollection.put("features", features);
            }
        }
    }

    /*
     * This method will be used to add the hotspots on the map
     * A List of coordinates was already retrieved from the backend
     */
    public static void queryEmergencyHotspots(Location location,
                                              MapboxMap mapboxMap,
                                              MapView mapView,
                                              Context context,
                                              Activity activity) {
        mapboxMap.getStyle(style -> {
            if(!featureCollection.isNull("features")){
                try {

                    JSONArray features = (JSONArray) featureCollection.get("features");
                    System.out.println("JSONArray " + features);


                    GeoJsonSource emergencyLineSource = style.getSourceAs(EMERGENCY_LINE_SOURCE_ID);
                    //GeoJsonSource emergencyCircles = style.getSourceAs(EMERGENCY_CIRCLE_LAYER_SOURCE_ID);


                    LineLayer nearby = style.getLayerAs(EMERGENCY_LINE_LAYER_ID);

                    if (features.length() < 0) {
                        Toast.makeText(context,
                                "No Emergency hotspots",
                                Toast.LENGTH_SHORT).show();
                        if (nearby != null) {
                            if (VISIBLE.equals(nearby.getVisibility().getValue())) {
                                nearby.setProperties(visibility(NONE));
                            }
                        }
                    } else {
                        if (nearby != null) {
                            if (NONE.equals(nearby.getVisibility().getValue())) {
                                nearby.setProperties(visibility(VISIBLE));
                            }
                            if (emergencyLineSource != null) {
                            // drawlines
                            }

                            /*
                             * Draw lines from the current user location to the hotspots
                             */
                            drawLines(features,
                                    emergencyLineSource,
                                    location,
                                    mapView,
                                    mapboxMap,
                                    style,
                                    activity);
                        }



/*                    generateAPIImage(center,
                            features.length(),
                            "Test",
                            mapboxMap,
                            activity
                    );*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /*
     * This method draws a line from the users current location to a hotspot
     * Draws circles on the map to denote the locations where the emergencies occurred
     */

    private static void drawLines(JSONArray features,
                                  GeoJsonSource emergencyLineSource,
                                  Location location,
                                  MapView mapView,
                                  MapboxMap mapboxMap,
                                  Style style,
                                  Activity activity) throws JSONException {

        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        List<Double> distances = new ArrayList<>();

        CircleManager circleManager = new CircleManager(mapView, mapboxMap, style);
        int d = 0;
        for (int i = 0; i < features.length(); i++) {
            JSONObject jsonObject = features.getJSONObject(i);
            JSONArray coordinates = jsonObject.getJSONArray("coordinates");
            double lat = Double.parseDouble(String.valueOf(coordinates.get(0)));
            double lng = Double.parseDouble(String.valueOf(coordinates.get(1)));

            List<Point> pointList = new ArrayList<>();

            // Single point for an emergency
            Point emergencyPoint = Point.fromLngLat(lng, lat);
            // Current user location
            Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

            pointList.add(emergencyPoint);
            pointList.add(currentPoint);

            // Create a line from where the emergency occurred to the students location

            // For each emergency create a red circle on the map


            CircleOptions biggerCircle = new CircleOptions()
                    .withLatLng(new LatLng(lat, lng))
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.WHITE))
                    .withCircleRadius(8f)
                    .withDraggable(false);

            CircleOptions innerCircle = new CircleOptions()
                    .withLatLng(new LatLng(lat, lng))
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.RED))
                    .withCircleRadius(6f)
                    .withDraggable(false);

            // Show the circles on the map

            try{
                circleManager.create(biggerCircle);
                circleManager.create(innerCircle);
            }catch (CannotAddSourceException e){

            }

            // Measure the distance between the users currentLocation and the emergency points


            // Calculate the distance between the user and the emergency point in KMs
            // This uses the Haversine formula to account for global curvature.
            double distance = TurfMeasurement.distance(emergencyPoint, currentPoint);
            System.out.println("DISTANCE " + d + " " + distance);
            d++;
            // Add points that are 0.1KM close to the user
            if (distance <= 0.5) {
                //lineFeatureList.add(Feature.fromGeometry(lineString));
                distances.add(distance);
            }
        }

        // If theres any locations within the users range generate the images
        if(distances.size() > 0){
            // Uncomment this to show notifications -> disabled for now to save res
/*            generateAPIImage(center,
                    distances.size(),
                    String.valueOf(Collections.min(distances)),
                    mapboxMap,
                    activity);*/


        }



    }

    public static void initEmergencyLineLayer(MapboxMap mapboxMap) {
        mapboxMap.getStyle(style -> {
            try{
                style.addSource(new GeoJsonSource(EMERGENCY_LINE_SOURCE_ID));
                LineLayer lineLayer = new LineLayer(EMERGENCY_LINE_LAYER_ID,
                        EMERGENCY_LINE_SOURCE_ID).withProperties(
                        lineColor(Color.parseColor(LINE_COLOR)),
                        lineWidth(LINE_WIDTH)
                );
                if (style.getLayer("emergency-hotspot-locations") != null) {
                    style.addLayerBelow(lineLayer, "emergency-hotspot-locations");
                } else {
                    style.addLayer(lineLayer);
                }
            }catch (CannotAddSourceException e){
/*                for (Layer layer : mapboxMap.getStyle().getLayers()) {
                    mapboxMap.getStyle().removeLayer(layer);
                }*/
            }

        });
    }


    private static void generateAPIImage(LatLng center,
                                         Integer nearby,
                                         String closest,
                                         MapboxMap mapboxMap,
                                         Activity activity) {

        mapboxMap.getStyle(style -> {
            target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (!notificationExists) {
                        newNotification(bitmap, nearby, closest, activity);
                    } else {
                        updateNotication(bitmap, nearby, closest);
                    }

                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            if(center != null){
                Picasso.get().load(genImageURL(center, activity)).into(target);
            }
        });
    }

    private static String genImageURL(LatLng center, Activity activity) {
        return MapboxStaticMap.builder()
                .accessToken(activity.getString(R.string.mapbox_access_token))
                // .user("appsatmapboxcom")
                .styleId(StaticMapCriteria.STREET_STYLE)
                .cameraPoint(Point.fromLngLat(center.getLongitude(), center.getLatitude()))
                .cameraZoom(16.5)
                .width(400)
                .height(400)
                .retina(true)
                .build()
                .url()
                .toString();
    }

    private static void updateNotication(Bitmap bitmap, Integer nearby, String closest) {
        builder.setLargeIcon(bitmap);
        builder.setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .bigLargeIcon(null));
        builder.setContentTitle(String.format("%1$d EMERGENCY HOTSPOTS WITHIN %2$d KM.",
                nearby, searchRadius));
        builder.setContentText("Students had issues around your location");
        manager.notify(NOTIFICATION_ID, builder.build());

    }

    private static void newNotification(Bitmap bitmap, Integer nearby, String closest, Activity activity) {

        if (manager == null) {
            manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null) {
                NotificationChannel channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            "channel_name", NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("channel_description");
                    manager.createNotificationChannel(channel);
                }
            }
        }

        /*
         * Based on the students current location determine the incidents that have occurred there
         * This will assist the student to be more cautious/ more alert
         * Create and join groups
         * Shake phone
         * suggest what times are best for certain things
         * Fall detection
         * Based on the submitted alerts use Topic Analysis to determine what a region is notorious for
         * https://monkeylearn.com/topic-analysis/
         * https://www.toptal.com/algorithms/how-to-build-a-natural-language-processing-app
         */

        /*
            Convert emergency points -> Addresses
            Store in

         */


        /*
        * Group requests
        * Based on the locations
         */

        /*
         * Suggest areas where drivers should patrol
         * Areas where there has been the most incidents
         *
         *
         *
         * Calculate the distance between the org and the distance location if it is greater than 3 miles decline the request
         * Make sure the driver is able to call the student
         */






        Intent intent = new Intent(activity.getApplicationContext(), activity.getClass())
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder = new NotificationCompat.Builder(activity.getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setContentTitle(String.format("%1$d EMERGENCY HOTSPOTS WITHIN %2$d KM.",
                        nearby, searchRadius))
                .setSmallIcon(R.drawable.mapbox_logo_icon)
                .setContentText("Students had issues around your location")
                .setContentIntent(PendingIntent.getActivity(activity.getApplicationContext(),
                        0, intent, 0))
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null));
        Notification notification = builder.build();
        manager.notify(NOTIFICATION_ID, notification);
        notificationExists = true;

    }

}
