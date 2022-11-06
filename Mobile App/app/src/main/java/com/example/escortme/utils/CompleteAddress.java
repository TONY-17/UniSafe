package com.example.escortme.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CompleteAddress {

    private static Context context;
    public CompleteAddress(Context context){
        this.context = context;
    }

    public String getCompleteAddressFromLatLong(double latitude, double longitude){
        String ans = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if(addresses != null){
                Address desiredAddress = addresses.get(0);
                StringBuilder stringBuilder = new StringBuilder();
                for(int i =0; i < desiredAddress.getMaxAddressLineIndex();i++){
                    stringBuilder.append(desiredAddress.getAddressLine(i)).append("\n");
                }
                ans = stringBuilder.toString();
                System.out.println("Current location" +  ans);
                Log.d("Current location ", ans);
            }else{
                Log.d("Current location ", "Nothing found");
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.d("Current location","Cannot retrieve location");
        }
        return ans;
    }

    public LatLng getLatLng(String address) throws IOException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList;
        LatLng ans;

        addressList = geocoder.getFromLocationName(address,2);
        if(addressList.isEmpty()){
            return null;
        }

        Address address1 = addressList.get(0);
        ans = new LatLng(address1.getLatitude(),address1.getLongitude());

        return ans;
    }
}
