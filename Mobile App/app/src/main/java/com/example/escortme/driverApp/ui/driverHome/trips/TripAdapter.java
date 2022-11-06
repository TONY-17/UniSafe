package com.example.escortme.driverApp.ui.driverHome.trips;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.escortme.R;
import com.example.escortme.driverApp.DriverHome;
import com.example.escortme.studentApp.ui.home.HomeFragment;
import com.example.escortme.utils.Helpers;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {


    private String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;
    List<Trip> tripList;

    public static Long trip;
    public static boolean clicked = false;
    boolean changeView;
    boolean emergencyPage;


    public TripAdapter(List<Trip> tripList, boolean changeView, boolean emergencyPage, Context context) {
        this.tripList = tripList;
        this.changeView = changeView;
        this.emergencyPage = emergencyPage;

        requestQueue = Volley.newRequestQueue(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        /*
         * Render a different view based on the boolean value
         * This is indicates if we are in the driver home page (where he/she can see the assigned trips)
         * Or we are in the history page for trips and emergencies
         */
        if (changeView) {
            /*
             * if True then render the History pages
             * This is the view used in the History pages (Trips and Emergencies)
             */
            view = inflater.inflate(R.layout.single_trip2, parent, false);
        } else {
            /*
             * If false then we are on the driver home page
             */
            view = inflater.inflate(R.layout.single_trip, parent, false);
        }
        return new TripAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView pickup = holder.pickup;
        pickup.setText(tripList.get(position).getPickUp());

        TextView destination = holder.destination;
        destination.setText(tripList.get(position).getDestination());

        TextView pointA = holder.pointA;
        TextView pointB = holder.pointB;
        // Show in emergencies pages
        if (!tripList.get(position).isShowStart()) {
            pointA.setText(tripList.get(position).getPickUp());
            pointB.setText(tripList.get(position).getDestination());
        }


        MaterialCardView start = holder.startTrip;

        TextView dateCreated = holder.date;
        // 29 July 2022 09:24:08
        DateFormat df = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss");
        DateFormat outputformat = new SimpleDateFormat("MM dd yyyy HH:mm aa");
        Date date;
        String output = null;

        try {
            date = df.parse(tripList.get(position).getDateCreated());
            output = outputformat.format(date);

            System.out.println(output);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        String[] firstPortion = tripList.get(position).getDateCreated().split(" ");
        String[] received = output.split(" ");

        if (tripList.get(position).isShowStart()) {
            dateCreated.setText(received[3] + " " + received[4]);
            start.setVisibility(View.VISIBLE);
            start.setOnClickListener(v -> {
                clicked = true;

                trip = tripList.get(position).getId();
                Long tripId = tripList.get(position).getId();
                String student = tripList.get(position).getStudent();
                String channelName = tripList.get(position).getChannel();
                String initialLocation = tripList.get(position).getPickUp();
                String finalLocation = tripList.get(position).getDestination();

                //
                sendNotification(student);
                Intent intent = new Intent(v.getContext(), DriverHome.class);
                intent.putExtra("tripId", tripId);
                intent.putExtra("student", student);
                intent.putExtra("Channel", channelName);
                intent.putExtra("initial", initialLocation);
                intent.putExtra("final", finalLocation);


                v.getContext().startActivity(intent);

            });
        }

        ImageView mapView = holder.mapViewImg;

        ConstraintLayout extDetails = holder.extendedDetails;
        ImageButton extend = holder.downArrow;
        ImageButton hide = holder.upArrow;

        if (!tripList.get(position).isShowStart()) {
            dateCreated.setText(firstPortion[0] + " " + firstPortion[1] + " " + firstPortion[2] + " " + received[3] + " " + received[4]);
            extend.setOnClickListener(view -> {
                extDetails.setVisibility(View.VISIBLE);
                hide.setVisibility(View.VISIBLE);
                extend.setVisibility(View.GONE);
                //mapView.setVisibility(View.VISIBLE);
            });

            hide.setOnClickListener(view -> {
                extDetails.setVisibility(View.GONE);
                hide.setVisibility(View.GONE);
                extend.setVisibility(View.VISIBLE);
                //mapView.setVisibility(View.GONE);
            });
        }

        // If we are in the emergency page hide the ability to extend details pages
        if (emergencyPage) {
            extend.setVisibility(View.GONE);
            hide.setVisibility(View.GONE);
        }

        /*
        https://api.mapbox.com/styles/v1/mapbox/light-v10/static/pin-s-l+000(-87.0186,32.4055)/-87.0186,32.4055,14/500x300?access_token=pk.eyJ1IjoidHktMTciLCJhIjoiY2txNGE3MWVuMTBsbjJvcGZjNjhnZGpxYSJ9.nt6K5vqc_ZMxJQXQp6bLYQ



         */

        String _pointB = tripList.get(position).getDestinationPoint();

        String mapBoxUrl = null;
        // If the current user is not a driver
        if (changeView) {
            if (emergencyPage) {
                mapBoxUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s-l+000(-87.0186,32.4055)/-87.0186,32.4055,14/500x300?access_token=pk.eyJ1IjoidHktMTciLCJhIjoiY2txNGE3MWVuMTBsbjJvcGZjNjhnZGpxYSJ9.nt6K5vqc_ZMxJQXQp6bLYQ";
            } else {
                mapBoxUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s-l+000(" + HomeFragment.userLocation.getLatitude() + "," + HomeFragment.userLocation.getLongitude() + ")" +
                        "/" + HomeFragment.userLocation.getLatitude() + "," + HomeFragment.userLocation.getLongitude() +
                        "/500x300?access_token=pk.eyJ1IjoidHktMTciLCJhIjoiY2txNGE3MWVuMTBsbjJvcGZjNjhnZGpxYSJ9.nt6K5vqc_ZMxJQXQp6bLYQ";

            }
        }
        if (!tripList.get(position).isShowStart()) {
            // Show the map for each history
            Picasso.get().load(mapBoxUrl).into(mapView);
        }

    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pickup;
        TextView destination;
        MaterialCardView startTrip;
        TextView date;
        ImageButton upArrow;
        ImageButton downArrow;
        ConstraintLayout extendedDetails;
        ImageView mapViewImg;


        TextView pointA, pointB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickup = itemView.findViewById(R.id.pickUpLocation);
            destination = itemView.findViewById(R.id.destinationLocation);
            startTrip = itemView.findViewById(R.id.tripInit);
            date = itemView.findViewById(R.id.dateCreated);
            upArrow = itemView.findViewById(R.id.upFacingArrow);
            downArrow = itemView.findViewById(R.id.downFacingArrow);
            extendedDetails = itemView.findViewById(R.id.extendedTripDetails);
            mapViewImg = itemView.findViewById(R.id.mapViewImg);


            pointA = itemView.findViewById(R.id.textView48);
            pointB = itemView.findViewById(R.id.textView50);
        }
    }


    /*
     * Alerts the student that the driver has accepted their trip
     */
    private void sendNotification(String username){



        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/" + username);
            JSONObject notification = new JSONObject();
            notification.put("title", "any title");
            notification.put("body", "any body");
            jsonObject.put("notification", notification);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAtwdXx-E:APA91bHPOxvitg2HXDoirOMDANmLkS52yHK0b6P3cMz5uuDs5ZCnk4-i6slx4UEQKPH9DY3S96TkwJ10SDQKMHWrUdF0L9iSG1xLJTEecW_c8rzjcMJkM69RJ0QkTfIjllbHI4O5kSQa");
                    return header;
                }
            };


            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
