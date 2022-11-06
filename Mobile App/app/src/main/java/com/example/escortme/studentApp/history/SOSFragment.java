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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SOSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SOSFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SOSFragment() {
        // Required empty public constructor
    }

    ImageView noRecordsImg;
    TextView noRecordsTxt;
    RecyclerView recyclerView;

    // TODO: Rename and change types and number of parameters
    public static SOSFragment newInstance(String param1, String param2) {
        SOSFragment fragment = new SOSFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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

        View view = inflater.inflate(R.layout.fragment_s_o_s, container, false);

        noRecordsImg = view.findViewById(R.id.noRecordsImg);
        noRecordsTxt = view.findViewById(R.id.noRecordsTxt);

        recyclerView = view.findViewById(R.id.emergencyHistory);

        if (MenuAdapter.isDriver) {
            retriveDriverHistoryRecords();
        } else {
            retriveStudentHistoryRecords();
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void retriveStudentHistoryRecords() {
        Call<ResponseBody> emergencies = RetrofitClient.getRetrofitClient().getAPI().getAllStudentEmergencies(InitialActivity.studentId);
        emergencies.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Have trips ");
                    try {

                        String data = response.body().string();
                        System.out.println("DATA DATA " + data);
                        JSONArray jsonArray = new JSONArray(data);
                        List<Trip> tripList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Long id = jsonObject.getLong("id");
                            String duration = jsonObject.getString("duration");
                            String dateCreated = jsonObject.getString("dateCreated");
                            String location = jsonObject.getString("location");
                            String type = jsonObject.getString("type");
                            Long studentId = jsonObject.getLong("studentId");

                            switch (type) {
                                case "Medical":
                                    tripList.add(new Trip(
                                            id,
                                            "You required medical assistance",
                                            duration,
                                            null,
                                            null,
                                            null,
                                            false,
                                            dateCreated,
                                            null));
                                    break;

                                case "Crime":
                                    tripList.add(new Trip(
                                            id,
                                            "You signalled for a crime alert",
                                            duration,
                                            null,
                                            null,
                                            null,
                                            false,
                                            dateCreated,
                                            null));
                                    break;

                                case "Accident":
                                    tripList.add(new Trip(
                                            id,
                                            "You signalled an accident",
                                            duration,
                                            null,
                                            null,
                                            null,
                                            false,
                                            dateCreated,
                                            null));
                                    break;


                                default:
                                    tripList.add(new Trip(
                                            id,
                                            type,
                                            duration,
                                            null,
                                            null,
                                            null,
                                            false,
                                            dateCreated,
                                            null));
                                    break;


                            }

                        }

                        if (!tripList.isEmpty()) {
                            removeViews();
                        }

                        Collections.reverse(tripList);
                        TripAdapter tripAdapter = new TripAdapter(tripList, true,true,getContext());
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

    private void retriveDriverHistoryRecords() {
        Call<ResponseBody> emergencies = RetrofitClient.getRetrofitClient().getAPI().getAllOrgEmergencies(InitialActivity.orgIdDriver);
        emergencies.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Have trips ");
                    try {

                        String data = response.body().string();
                        System.out.println("DATA DATA " + data);
                        JSONArray jsonArray = new JSONArray(data);
                        List<Trip> tripList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Long id = jsonObject.getLong("id");
                            String duration = jsonObject.getString("duration");
                            String dateCreated = jsonObject.getString("dateCreated");
                            String location = jsonObject.getString("location");
                            String type = jsonObject.getString("type");
                            Long studentId = jsonObject.getLong("studentId");

                            tripList.add(new Trip(
                                    id,
                                    type,
                                    duration,
                                    null,
                                    null,
                                    null,
                                    false,
                                    dateCreated,
                                    null));

                        }

                        if (!tripList.isEmpty()) {
                            removeViews();
                        }

                        Collections.reverse(tripList);
                        TripAdapter tripAdapter = new TripAdapter(tripList, true,true,getContext());
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

    private void removeViews() {
        noRecordsTxt.setVisibility(View.GONE);
        noRecordsImg.setVisibility(View.GONE);
    }
}