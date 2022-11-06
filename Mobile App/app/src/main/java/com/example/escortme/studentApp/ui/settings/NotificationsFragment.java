package com.example.escortme.studentApp.ui.settings;

import android.content.Intent;
import android.graphics.ColorFilter;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.FragmentNotificationsBinding;
import com.example.escortme.network.RetrofitClient;
import com.example.escortme.studentApp.menu.MenuAdapter;
import com.example.escortme.studentApp.menu.menuItem;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    static TextView txtUsername;
    static TextView txtEmail;
    static TextView settingsInitial;

    static ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    static TextView studentRating;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        shimmerFrameLayout = binding.sFL;
        shimmerFrameLayout.startShimmer();
        recyclerView = binding.recyclerView;

        studentRating = binding.studentRating;
        txtUsername = binding.settingsUsername;
        txtEmail = binding.settingsEmail;
        settingsInitial = binding.settingsInitial;

        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white,null));
        setUpMenu();
        retrieveProfileInformation();

        TextView logOut = binding.logOutStudent;
        logOut.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), InitialActivity.class));
            getActivity().finish();
        });


        return binding.getRoot();
    }

    private void setUpMenu() {
        List<menuItem> menuItems = new ArrayList<>();
        menuItem menuItem = new menuItem(
                R.drawable.ic_outline_email_24,
                "Account"
        );
        menuItem menuItem2 = new menuItem(
                R.drawable.ic_24,
                "History"
        );
        menuItems.add(menuItem);
        menuItems.add(menuItem2);

        MenuAdapter adapter = new MenuAdapter(menuItems, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


    }

    public static void retrieveProfileInformation() {
        Call<ResponseBody> studentDetails = RetrofitClient.getRetrofitClient().getAPI().getStudentDetails(InitialActivity.currentUserID);
        studentDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String username = jsonObject.getString("username");
                        String email = jsonObject.getString("email");
                        double rating = jsonObject.getDouble("rating");
                        shimmerFrameLayout.setVisibility(View.GONE);
                        char c = username.charAt(0);
                        settingsInitial.setText(String.valueOf(c));
                        txtUsername.setText(username);
                        txtEmail.setText(email);
                        studentRating.setText(String.format("%.2f", rating));

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}