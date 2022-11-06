package com.example.escortme.studentApp.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.FragmentDashboardBinding;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.Compose;
import com.example.escortme.studentApp.news.Alert;
import com.example.escortme.studentApp.news.AlertAdapter;
import com.example.escortme.utils.Helpers;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    RecyclerView  newsFeed;
    TextView textView;
    ImageView imageView15;
    //CircularProgressIndicator spinKitView;
    SwipeRefreshLayout swipeLayout;
    ShimmerFrameLayout shimmerFrameLayout;
    @SuppressLint({"NotifyDataSetChanged", "ResourceAsColor"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        newsFeed = binding.newsfeed;
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.customStatusBar,null));

        shimmerFrameLayout = binding.newsShimmerFrame;
        imageView15 = binding.justImg;
        textView = binding.justText;
        //spinKitView = binding.spinKit;
        retrieveAllAlerts();
        turnOffDrawables(View.GONE);

        MaterialCardView filter = binding.filterNews;
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ThemedToggleButtonGroup themedButtonGroup = binding.themedToggleButtonGroup;
        ThemedButton questionsFilter = themedButtonGroup.findViewById(R.id.btn1);
        ThemedButton safetyFilter = themedButtonGroup.findViewById(R.id.btn2);
        ThemedButton alertFilter = themedButtonGroup.findViewById(R.id.btn3);

        themedButtonGroup.setOnSelectListener((ThemedButton btn) -> {
            if(questionsFilter.isSelected() == true){
                retrieveQuestionsAlerts("Question");
            }
            if(safetyFilter.isSelected() == true){
                retrieveQuestionsAlerts("Safety");
            }
            if(alertFilter.isSelected() == true){
                retrieveQuestionsAlerts("Alert");
            }
            return kotlin.Unit.INSTANCE;
        });

        swipeLayout = binding.swipeContainer;
        swipeLayout.setOnRefreshListener(() -> {
            AlertAdapter.alerts.clear();
            retrieveAllAlerts();
        });
        swipeLayout.setOnRefreshListener(() -> retrieveAllAlerts());
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright,null),
                getResources().getColor(android.R.color.holo_green_light,null),
                getResources().getColor(android.R.color.holo_orange_light,null),
                getResources().getColor(android.R.color.holo_red_light,null)
        );


        final ExtendedFloatingActionButton extendedFloatingActionButton = binding.extFloatingActionButton;
        extendedFloatingActionButton.shrink();
        extendedFloatingActionButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Compose.class));
        });

        newsFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Scrolling up
                if(dy > 0 && extendedFloatingActionButton.isExtended() ){
                    extendedFloatingActionButton.shrink();
                }
                if(dy < 0 && !extendedFloatingActionButton.isExtended()){
                    extendedFloatingActionButton.extend();
                }

            }
        });

        MaterialCardView searchText = binding.time;
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageView orgLogo = binding.orgLogo;
        String orgDomain = StringUtils.substringAfterLast(InitialActivity.email, "@");
        System.out.println("ORG-DOMAIN " + orgDomain);
        String logoAPI = "https://logo.clearbit.com/" + orgDomain;
        Picasso.get().load(logoAPI).into(orgLogo);

        return binding.getRoot();
    }

    private void turnOffDrawables(int visibility){
        textView.setVisibility(visibility);
        imageView15.setVisibility(visibility);
    }
    public void retrieveAllAlerts(){
        List<Alert> list = new ArrayList<>();
        Call<ResponseBody> alertsCall = RetrofitClient.getRetrofitClient().getAPI().getAlerts(InitialActivity.orgId);
        alertsCall.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    try {
                        String data = response.body().string();
                        JSONArray jsonArray = new JSONArray(data);
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Long id = jsonObject.getLong("id");
                            String body = jsonObject.getString("body");
                            String time = jsonObject.getString("timePosted");
                            String username = jsonObject.getString("user");

                            List<String> URLS = new ArrayList<>();
                            JSONArray imageList = jsonObject.getJSONArray("imageList");
                            for(int k = 0; k < imageList.length(); k++){
                                URLS.add(imageList.getString(k));
                            }
                            System.out.println("URLS" + URLS.toString());
                            Integer comments = jsonObject.getInt("numberOfComments");
                            list.add(new Alert(id,username,time,body,String.valueOf(comments),URLS));
                        }
                        if(!list.isEmpty()){
                            //spinKitView.setVisibility(View.GONE);
                            shimmerFrameLayout.setVisibility(View.GONE);
                            swipeLayout.setRefreshing(false);
                            turnOffDrawables(View.GONE);
                        }
                        Collections.reverse(list);
                        AlertAdapter alertAdapter = new AlertAdapter(list,getActivity());
                        alertAdapter.notifyDataSetChanged();
                        newsFeed.setAdapter(alertAdapter);
                        newsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
                        newsFeed.setHasFixedSize(true);
                        swipeLayout.setRefreshing(false);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    //spinKitView.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.GONE);
                    turnOffDrawables(View.VISIBLE);


                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //spinKitView.setVisibility(View.GONE);
                shimmerFrameLayout.setVisibility(View.GONE);
                Helpers.failure(getActivity(),"Failed to retrieve alerts");
            }
        });
    }

    public void retrieveQuestionsAlerts(String tag){
       // spinKitView.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        List<Alert> list = new ArrayList<>();
        Call<ResponseBody> alertsCall = RetrofitClient.getRetrofitClient().getAPI().getAlertsByTag(InitialActivity.orgId,tag);
        alertsCall.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    //assert response.body() != null;
                    try {
                        //spinKitView.setVisibility(View.GONE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        String data = response.body().string();
                        JSONArray jsonArray = new JSONArray(data);
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Long id = jsonObject.getLong("id");
                            String body = jsonObject.getString("body");
                            String time = jsonObject.getString("timePosted");
                            String username = jsonObject.getString("user");
                            Integer comments = jsonObject.getInt("numberOfComments");
                            List<String> URLS = new ArrayList<>();
                            JSONArray imageList = jsonObject.getJSONArray("imageList");
                            for(int k = 0; k < imageList.length(); k++){
                                URLS.add(imageList.getString(k));
                            }

                            list.add(new Alert(id,username,time,body,String.valueOf(comments),URLS));
                        }
                        if(!list.isEmpty()){
                            turnOffDrawables(View.GONE);
                        }
                        Collections.reverse(list);
                        AlertAdapter alertAdapter = new AlertAdapter(list,getActivity());
                        alertAdapter.notifyDataSetChanged();
                        newsFeed.setAdapter(alertAdapter);
                        newsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
                        newsFeed.setHasFixedSize(true);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    //spinKitView.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.GONE);
                    turnOffDrawables(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(getActivity(),"Failed to retrieve alerts");
                turnOffDrawables(View.VISIBLE);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
