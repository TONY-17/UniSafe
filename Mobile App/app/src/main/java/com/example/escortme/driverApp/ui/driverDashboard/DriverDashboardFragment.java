package com.example.escortme.driverApp.ui.driverDashboard;

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

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.menu.MenuAdapter;
import com.example.escortme.studentApp.menu.MenuAdapter2;
import com.example.escortme.studentApp.menu.menuItem;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverDashboardFragment extends Fragment {

    View view;
    static ShimmerFrameLayout shimmerFrameLayout;
    MaterialCardView profileCV;
    static TextView txtUsername;
    static TextView txtEmail;
    static TextView driverRating;
    static ImageView driverImage;


    static TextView txtTotalRides;
    static TextView txtTotalDistance;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.driver_dashboard_layout, container, false);
        txtUsername = view.findViewById(R.id.driverUsernameDash);
        txtEmail = view.findViewById(R.id.driverEmailDash);

        txtTotalDistance = view.findViewById(R.id.driverTotalDistance);
        txtTotalRides = view.findViewById(R.id.driverTotalRides);
        driverRating = view.findViewById(R.id.studentRating);
        setUpMenu();
        setUpMenu2();
        shimmerFrameLayout = view.findViewById(R.id.sFL);
        profileCV = view.findViewById(R.id.materialCardView20);
        shimmerFrameLayout.startShimmer();
        getDriverProfileInfo();

        TextView logOutDriver = view.findViewById(R.id.logOutDriver);
        logOutDriver.setOnClickListener(v -> {
            startActivity(new Intent(getContext(),InitialActivity.class));
            getActivity().finish();
        });

        driverImage = view.findViewById(R.id.driverProfilePicture);


        return view;
    }

    protected static float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    public static void getDriverProfileInfo() {
        Long driverId = InitialActivity.driverId;
        Call<ResponseBody> getDriverInfo = RetrofitClient.getRetrofitClient().getAPI().getDriverInfo(driverId);
        getDriverInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        String loggedInUser = firstName + " " + lastName;
                        String picture = jsonObject.getString("picture");
                        System.out.println("DRIVER PICTURE " + picture);
                        String rating = jsonObject.getString("rating");

                        String totalRides = jsonObject.getString("totalRides");
                        String totalRatings = jsonObject.getString("totalRatings");

                        txtTotalRides.setText(totalRides);

                        txtTotalDistance.setText( String.format("%.2f", getRandom(.05f, 0f)) + " KM");

                        Picasso.get().load(picture).into(driverImage);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);

                        txtUsername.setText(loggedInUser);
                        txtEmail.setText(email);

                        driverRating.setText("Rating " + rating + " \u2022 "+ "Total " + totalRatings);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Toasts.failure(getActivity(),"Server retrieval error");
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setUpMenu() {
        RecyclerView menu1 = view.findViewById(R.id.menu1);
        // First section menu icons
        List<menuItem> menuItems = new ArrayList<>();
        menuItem menuItem2 = new menuItem(
                R.drawable.ic_outline_email_24,
                "Account"
        );
        menuItems.add(menuItem2);


        MenuAdapter adapter = new MenuAdapter(menuItems, true);
        menu1.setAdapter(adapter);
        menu1.setHasFixedSize(true);
        menu1.setLayoutManager(new LinearLayoutManager(getContext()));

    }
    private void setUpMenu2() {
        List<menuItem> menuItems2 = new ArrayList<>();
           menuItem menuItem = new menuItem(
                R.drawable.ic_dashboard_black_24dp,
                "Dashboard"
        );
        menuItem menuItem3 = new menuItem(
                R.drawable.ic_outline_notifications_active_24,
                "Notifications"
        );
        menuItems2.add(menuItem);
        menuItems2.add(menuItem3);
        RecyclerView menu2 = view.findViewById(R.id.menu2);
        MenuAdapter2 adapter2 = new MenuAdapter2(menuItems2);
        menu2.setAdapter(adapter2);
        menu2.setHasFixedSize(true);
        menu2.setLayoutManager(new LinearLayoutManager(getContext()));
    }





}