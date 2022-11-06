package com.example.escortme.utils;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import android.content.Context;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.example.escortme.R;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

public class MapUtils {


    /*
    * This method will initialize the icon to show the students location during an emergency
     */
    public static void initStudentEmergencyIcon(@NonNull Style style, Context context) {
        style.addImage("student-icon-id",
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.map_default_map_marker));

        style.addSource(new GeoJsonSource("student-source-id"));

        style.addLayer(new SymbolLayer("layer-id", "student-source-id").withProperties(
                iconImage("student-icon-id"),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(.7f)
        ));
    }
}
