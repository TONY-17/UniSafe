package com.example.escortme.studentApp;

import static com.mapbox.core.constants.Constants.PRECISION_5;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.escortme.R;
import com.example.escortme.utils.Helpers;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripDetails extends AppCompatActivity {

    MapboxDirections mapboxDirections;
    ImageView tripMap;
    DirectionsRoute route;
    String polyline;
    Point o;
    Point d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        getWindow().setStatusBarColor(Color.WHITE);

        tripMap = findViewById(R.id.tripDetailsMap);

        // -122.46589,37.77343
        o = Point.fromLngLat(-122.46589, 37.77343);
        d = Point.fromLngLat(-122.42816, 37.75965);
        getDirectionRoute(o, d);

    }


    private void getDirectionRoute(Point origin, Point destination) {
        mapboxDirections = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        mapboxDirections.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                route = response.body().routes().get(0);

                List<LatLng> points = new ArrayList<>();
                List<Point> coords = LineString.fromPolyline(route.geometry(), PRECISION_5).coordinates();

                for (Point point : coords) {
                    points.add(new LatLng(point.latitude(), point.longitude()));
                    //previous = (int) point.latitude();
                }

                // String polyline = "%7DrpeFxbnjVsFwdAvr@cHgFor@jEmAlFmEMwM_FuItCkOi@wc@bg@wBSgM";
                String polyline = encoder(points,PRECISION_5);

                String mapBoxUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s-a+212638(" + o.longitude() + "," + o.latitude() + ")" +
                        ",pin-s-b+212638(" + d.longitude() + "," + d.latitude() + ")," +
                        "path-5+212638(" + " " + ")" +
                        "/auto/500x300?access_token=pk.eyJ1IjoidHktMTciLCJhIjoiY2txNGE3MWVuMTBsbjJvcGZjNjhnZGpxYSJ9.nt6K5vqc_ZMxJQXQp6bLYQ";
                imageResponse(mapBoxUrl);

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

            }
        });

    }


    private void imageResponse(String mapBoxUrl) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //String mapBoxUrl = "https://api.mapbox.com/styles/v1/{username}/{style_id}/static/{overlay}/{lon},{lat},{zoom},{bearing},{pitch}|{bbox}|{auto}/{width}x{height}{@2x}";

        // "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/0,10,3,20/600x600?access_token=pk.eyJ1IjoidHktMTciLCJhIjoiY2txNGE3MWVuMTBsbjJvcGZjNjhnZGpxYSJ9.nt6K5vqc_ZMxJQXQp6bLYQ"

        // Send request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mapBoxUrl,
                response ->
                        Picasso.get().load(mapBoxUrl).into(tripMap),
                error ->
                        Helpers.failure(TripDetails.this, "Failed to retrieve snapshot"));

        requestQueue.add(stringRequest);
    }


    /*
     * The following code does not belong to Team 46
     * It was adapted from:
     * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Google polyline algorithm</a>
     */
    private static String encoder(List<LatLng> points, int precision) {
        StringBuilder encoded = new StringBuilder();
        int previous_longitude = 0;
        int previous_latitude = 0;
        for (LatLng single : points) {
            int latitude = (int) (single.latitude/precision);
            int longitude = (int) (single.longitude / precision);

            encoded.append(encodedSignedNumber(latitude - previous_latitude));
            encoded.append(encodedSignedNumber(longitude - previous_longitude));
            previous_latitude = latitude;
            previous_longitude = longitude;
        }
        return encoded.toString();
    }

    private static StringBuffer encodedSignedNumber(int number){
        int signed_number = number << 1;
        if (number < 0) {
            signed_number = ~(signed_number);
        }
        return(encodeNumber(signed_number));
    }

    private static StringBuffer encodeNumber(int num) {
        StringBuffer encodeString = new StringBuffer();
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            encodeString.append((char)(nextValue));
            num >>= 5;
        }
        num += 63;
        encodeString.append((char)(num));
        return encodeString;
    }

}