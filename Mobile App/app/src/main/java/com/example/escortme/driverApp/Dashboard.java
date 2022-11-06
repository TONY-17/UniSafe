package com.example.escortme.driverApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.History;
import com.example.escortme.utils.Helpers;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {

    CombinedChart chart;
    private final int count = 7;

    HashMap<String,Integer> driverTrips;
    HashMap<String,Integer> orgTrips;
    TextView tripsCount;
    XAxis xAxis;


    PieChart pieChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getWindow().setStatusBarColor(Color.WHITE);

        pieChart = findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);

        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        //pieChart.setCenterTextTypeface(tfLight);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        //chart.setOnChartValueSelectedListener(this);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        //chart.animateY(1400, Easing.EaseInOutQuad);

        Legend l = pieChart.getLegend();
        l.setEnabled(false);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChart.setEntryLabelTextSize(12f);


        setData(4,4f);





        chart = findViewById(R.id.chart1);
        chart.animateY(1400, Easing.EaseInOutQuad);
        tripsCount = findViewById(R.id.textView5);
        driverTrips = new HashMap<>();
        orgTrips = new HashMap<>();

        String[] days = {
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT",
                "SUN"
        };

        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(getResources().getColor(R.color.white,null));
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });
        Legend pieChartLegend = chart.getLegend();
        pieChartLegend.setEnabled(false);

        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);

        ValueFormatter v = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return days[(int) value % days.length];
            }
        };
        xAxis.setValueFormatter(v);

        getDashboardData();

        TextView current = findViewById(R.id.currentDateTime);
        current.setText(Helpers.currentTime());
    }

    private void getDashboardData(){
        Call<ResponseBody> dashboardData = RetrofitClient.getRetrofitClient().getAPI().allDriverTips(
                InitialActivity.driverId
        );
        dashboardData.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String resData = response.body().string();
                        JSONObject jsonObject = new JSONObject(resData);
                        int totalNumberOfTrips = jsonObject.getInt("totalNumberOfTrips");
                        JSONObject trips = jsonObject.getJSONObject("driverTrips");
                        tripsCount.setText(totalNumberOfTrips + " trips");
                        driverTrips.put("MON",trips.getInt("MON"));
                        driverTrips.put("TUE", trips.getInt("TUE"));
                        driverTrips.put("WED",trips.getInt("WED"));
                        driverTrips.put("THUR",trips.getInt("THUR"));
                        driverTrips.put("FRI",trips.getInt("FRI"));
                        driverTrips.put("SAT",trips.getInt("SAT"));
                        driverTrips.put("SUN",trips.getInt("SUN"));

                        JSONObject orgs = jsonObject.getJSONObject("driverTrips");
                        orgTrips.put("MON",orgs.getInt("MON"));
                        orgTrips.put("TUE", orgs.getInt("TUE"));
                        orgTrips.put("WED",orgs.getInt("WED"));
                        orgTrips.put("THUR",orgs.getInt("THUR"));
                        orgTrips.put("FRI",orgs.getInt("FRI"));
                        orgTrips.put("SAT",orgs.getInt("SAT"));
                        orgTrips.put("SUN",orgs.getInt("SUN"));


                        CombinedData data = new CombinedData();
                        data.setData(populateGraph(driverTrips,orgTrips));
                        xAxis.setAxisMaximum(data.getXMax() + 0.25f);
                        chart.setData(data);
                        chart.invalidate();


                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Helpers.failure(Dashboard.this,"Failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Helpers.failure(Dashboard.this,"Failed: " + t.getMessage().toString());
            }
        });


    }
    private LineData populateGraph(HashMap<String,Integer> map,HashMap<String,Integer> org) {
        LineData data = new LineData();

        ArrayList<Entry> entries = new ArrayList<>();

        entries.add(new Entry(0,org.get("MON")));
        entries.add(new Entry(1, org.get("TUE")));
        entries.add(new Entry(2, org.get("WED")));
        entries.add(new Entry(3, org.get("THUR")));
        entries.add(new Entry(4, org.get("FRI")));
        entries.add(new Entry(5, org.get("SAT")));
        entries.add(new Entry(6, org.get("SUN")));

        LineDataSet set = new LineDataSet(entries, "Driver");
        set.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.custom_gradient));
        set.setDrawFilled(true);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setColor(Color.rgb(64, 89, 128));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(64, 89, 128));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(64, 89, 128));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(set);



        ArrayList<Entry> entries1 = new ArrayList<>();

        entries1.add(new Entry(0,map.get("MON")));
        entries1.add(new Entry(1, map.get("TUE")));
        entries1.add(new Entry(2, map.get("WED")));
        entries1.add(new Entry(3, map.get("THUR")));
        entries1.add(new Entry(4, map.get("FRI")));
        entries1.add(new Entry(5, map.get("SAT")));
        entries1.add(new Entry(6, map.get("SUN")));

        LineDataSet set1 = new LineDataSet(entries1, "Organisation");
        set1.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.custom_gradient));
        set1.setDrawFilled(true);
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setColor(Color.rgb(149, 165, 124));
        set1.setLineWidth(2.5f);
        set1.setCircleColor(Color.rgb(149, 165, 124));
        set1.setCircleRadius(5f);
        set1.setFillColor(Color.rgb(149, 165, 124));
        set1.setDrawValues(false);
        set1.setValueTextSize(10f);
        set1.setValueTextColor(Color.rgb(240, 238, 70));

        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        data.addDataSet(set1);


        return data;
    }

    private void setData(int count, float range) {
        String[] parties= new String[]{"GATE 1","GATE 2","GATE 3","GATE 1","GATE 2","GATE 3"};
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < count ; i++) {

            entries.add(new PieEntry((float) ((Math.random() * range) + range / 5),
                    parties[i % parties.length],
                    getResources().getDrawable(R.drawable.ic_circle)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Location Traffic");
        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c :  CustomThemeColors.JOYFUL_COLORS)
            colors.add(c);

        for (int c : CustomThemeColors.JOYFUL_COLORS)
            colors.add(c);

        for (int c : CustomThemeColors.JOYFUL_COLORS)
            colors.add(c);

        for (int c : CustomThemeColors.JOYFUL_COLORS)
            colors.add(c);

        for (int c : CustomThemeColors.JOYFUL_COLORS)
            colors.add(c);

        colors.add(CustomThemeColors.getPrimaryColor());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueTypeface();
        pieChart.setData(data);
        // undo all highlights
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }


    public static class CustomThemeColors{

        public static final int[] JOYFUL_COLORS = {
                Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134), Color.rgb(179, 48, 80)
        };

        public static int getPrimaryColor() {
            return Color.rgb(254, 229, 185);
        }

    }

}

