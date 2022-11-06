package com.example.escortme.studentApp.history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.ui.driverHome.trips.Trip;
import com.example.escortme.driverApp.ui.driverHome.trips.TripAdapter;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.menu.MenuAdapter;

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

public class TripFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TripFragment() {
        // Required empty public constructor
    }


    ImageView noRecordsImg;
    TextView noRecordsTxt;
    RecyclerView recyclerView;
    Long userId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        userId = InitialActivity.currentUserID;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip, container, false);

        noRecordsImg = view.findViewById(R.id.noRecordsImg);
        noRecordsTxt = view.findViewById(R.id.noRecordsTxt);

        recyclerView = view.findViewById(R.id.tripHistory);


        if(MenuAdapter.isDriver){
            retriveDriverHistoryRecords();
        }else{
            retrieveStudentHistoryRecords();
        }

        return view ;
    }

    private void retriveDriverHistoryRecords() {
        Call<ResponseBody> driverTrips = RetrofitClient.getRetrofitClient().getAPI().getAllDriverTrips(InitialActivity.driverId);
        driverTrips.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    System.out.println("Have trips ");
                    try {
                        assert response.body() != null;
                        String data = response.body().string();
                        JSONArray jsonArray = new JSONArray(data);
                        List<Trip> tripList = new ArrayList<>();
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
                                    false,
                                    dateCreated,
                                    student));
                        }
                        if(!tripList.isEmpty()){removeViews();}
                        Collections.reverse(tripList);
                        TripAdapter tripAdapter = new TripAdapter(tripList,true,false,getContext());
                        recyclerView.setAdapter(tripAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        tripAdapter.notifyDataSetChanged();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void retrieveStudentHistoryRecords(){
        Call<ResponseBody> getAllTrips = RetrofitClient.getRetrofitClient().getAPI().getAllStudentTrips(userId);
        getAllTrips.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {
                    System.out.println("Have trips ");
                    try {
                        assert response.body() != null;

                        String data = response.body().string();
                        JSONArray jsonArray = new JSONArray(data);
                        List<Trip> tripList = new ArrayList<>();
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
                                    false,
                                    dateCreated,
                                    student));
                        }
                        if(!tripList.isEmpty()){removeViews();}
                        // Show the latest trips on top of the list
                        Collections.reverse(tripList);
                        TripAdapter tripAdapter = new TripAdapter(tripList,true,false,getContext());
                        recyclerView.setAdapter(tripAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        tripAdapter.notifyDataSetChanged();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    private void removeViews(){
        noRecordsTxt.setVisibility(View.GONE);
        noRecordsImg.setVisibility(View.GONE);
    }
}