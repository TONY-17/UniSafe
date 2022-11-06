package com.example.escortme.studentApp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.driverApp.customCalender.AdapterCalendar;
import com.example.escortme.driverApp.customCalender.DataCalendar;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.news.Alert;
import com.example.escortme.studentApp.news.AlertAdapter;
import com.example.escortme.studentApp.news.AlertAdapterNotifications;
import com.google.common.base.CharMatcher;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notifications extends AppCompatActivity  implements AdapterCalendar.ICalendar {

    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    AdapterCalendar adapterCalendar = new AdapterCalendar(new ArrayList<>(), (AdapterCalendar.ICalendar) this);
    List<DataCalendar> calendarList = new ArrayList<>();
    RecyclerView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        getWindow().setStatusBarColor(Color.WHITE);
        recyclerView = findViewById(R.id.notificationsList);
        mSwipeRefreshLayout = findViewById(R.id.refreshNotificationList);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        calendarView = findViewById(R.id.calendarView);
        //retrieveOrgNotifications();

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            retrieveOrgNotifications();
            mSwipeRefreshLayout.setRefreshing(false);
        });
        initializeCalendar();
        fetchDates();

    }


    private void initializeCalendar() {
        calendarView.setHasFixedSize(true);
        calendarView.setAdapter(adapterCalendar);
    }

    private void fetchDates(){
        ArrayList<DataCalendar> dateList = new ArrayList<>();
        ArrayList<Date> dates = new ArrayList<>();

        Calendar monthCalendar = (Calendar) calendar.clone();
        int maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);


        DateFormat dateFormat = new SimpleDateFormat("d");
        Date date = new Date();
        int todayDate = Integer.parseInt(dateFormat.format(date));

        while (dates.size() < maxDaysInMonth){

            int date2 = monthCalendar.getTime().getDate();
            System.out.println("DATE " +  Calendar.DAY_OF_MONTH);
            dates.add(monthCalendar.getTime());
            dateList.add(new DataCalendar(monthCalendar.getTime(),false));
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);
        }
        calendarList.clear();
        calendarList.addAll(dateList);
        adapterCalendar.updateList(dateList);
    }

    private void retrieveOrgNotifications(){
        List<Alert> list = new ArrayList<>();
        Call<ResponseBody> notifications = RetrofitClient.getRetrofitClient().getAPI().getOrgNotifications(InitialActivity.orgIdDriver);
        notifications.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    try {
                        String data = response.body().string();
                        System.out.println("NOTIFICATIONS " + data);
                        // {\"newMessage\":\"Hi Tony on the other side\"}

                        JSONArray jsonArray = new JSONArray(data);
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Long id = jsonObject.getLong("id");
                            String content = jsonObject.getString("content");
                        /*    String removeChars = "{\"\\\"}";
                            String updatedContent = CharMatcher.anyOf(removeChars).removeFrom(content);*/
                            String dateCreated = jsonObject.getString("dateCreated");
                            String username = "Management";
                            list.add(new Alert(id,username,dateCreated,content,null,null));
                        }
                        if(!list.isEmpty()){
/*                            spinKitView.setVisibility(View.GONE);
                            turnOffDrawables(View.GONE);*/
                        }
                        Collections.reverse(list);
                        AlertAdapterNotifications alertAdapter = new AlertAdapterNotifications(list);
                        alertAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(alertAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Notifications.this));
                        recyclerView.setHasFixedSize(true);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    /*spinKitView.setVisibility(View.GONE);
                    turnOffDrawables(View.VISIBLE);*/
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });



    }

    @Override
    public void onSelect(@NonNull DataCalendar data, int position) {

    }
}