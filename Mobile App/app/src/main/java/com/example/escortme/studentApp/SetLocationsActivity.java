package com.example.escortme.studentApp;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.escortme.InitialActivity;
import com.example.escortme.R;
import com.example.escortme.databinding.ActivitySetLocationsBinding;
import com.example.escortme.studentApp.ui.home.HomeFragment;
import com.example.escortme.utils.BActivityResult;
import com.example.escortme.utils.CompleteAddress;
import com.example.escortme.utils.SearchResultsAdapter;
import com.example.escortme.utils.SearchSuggestions;
import com.google.android.material.textfield.TextInputEditText;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class  SetLocationsActivity extends AppCompatActivity {
    private ActivitySetLocationsBinding binding;
    protected final BActivityResult<Intent, ActivityResult> activityResultBActivityResult = BActivityResult.registerActivityForResult(this);
    private TextInputEditText TxtUserDestination;
    // Enables geocoding
    private SearchEngine searchEngine;
    private SearchRequestTask searchRequestTask;
    Location currentLocation;
    // private static final Logger logger = LoggerFactory.getLogger()
    View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetLocationsBinding.inflate(getLayoutInflater());
       view = binding.getRoot();
        setContentView(view);

        getWindow().setStatusBarColor(Color.WHITE);
        // Gets the current user location from the MainActivity
        currentLocation = HomeFragment.userLocation;
        // Location picker
        binding.materialCardView16.setOnClickListener(v -> {
            assert Mapbox.getAccessToken() != null;
            @SuppressLint("ResourceType")
            Intent intent = new PlacePicker.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(
                            PlacePickerOptions.builder()
                                    .includeDeviceLocationButton(true)
                                    .includeReverseGeocode(true)
                                    .toolbarColor(Color.parseColor(getString(R.color.primary)))
                                    .statingCameraPosition(
                                            new CameraPosition.Builder()
                                                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                                                    .zoom(16)
                                                    .build())
                                    .build())
                    .build(SetLocationsActivity.this);
            activityResultBActivityResult.launch(intent, result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    CarmenFeature carmenFeature = PlacePicker.getPlace(data);
                    if (carmenFeature != null) {
                        System.out.println("Camera Feature " + carmenFeature.toJson());
                        TxtUserDestination.setText("");
                    }

                }
            });
        });

        // Pick up locations list
        binding.userPickUpLocation.setAdapter(pickUpLocations());

        // Mapbox geocoder
        searchEngine = MapboxSearchSdk.getSearchEngine();
        final SearchOptions searchOptions = new SearchOptions.Builder().limit(5).build();


        // suggestions

        TxtUserDestination = findViewById(R.id.userDestinationLocation);
        TxtUserDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(TxtUserDestination.getText().toString())) {
                    System.out.println("AfterTextChanged" + s);
                    searchRequestTask = searchEngine.search(s.toString(), searchOptions, searchSelectionCallback);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ImageView backButton = binding.backButton;
        backButton.setOnClickListener(v -> SetLocationsActivity.super.onBackPressed());


    }

    private ArrayAdapter<String>  pickUpLocations(){
        List<String> locations = InitialActivity.pickUpPoints;
        return new ArrayAdapter<>(SetLocationsActivity.this, R.layout.list_item, locations);
    }

    private final SearchSelectionCallback searchSelectionCallback = new SearchSelectionCallback() {
        @Override
        public void onResult(@NonNull SearchSuggestion searchSuggestion, @NonNull SearchResult searchResult, @NonNull ResponseInfo responseInfo) {
            Timber.tag("Search destinations").i("Result found %s", searchResult);

        }

        @Override
        public void onCategoryResult(@NonNull SearchSuggestion searchSuggestion, @NonNull List<? extends SearchResult> list, @NonNull ResponseInfo responseInfo) {
            Log.i("Search destinations", "Search results (catergory): " + list);
        }

        @Override
        public void onSuggestions(@NonNull List<? extends SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {
            if (list.isEmpty()) {
                Log.i("Search destinations", "No suggestions found");
            } else {

                System.out.println("list.get(0)" + list.get(0));
                ArrayList<SearchSuggestions> suggestionsList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if(list.get(i).getAddress().getPlace() != null){
                        suggestionsList.add(new SearchSuggestions(list.get(i).getAddress().getPlace(), list.get(i).getAddress().formattedAddress()));
                    }
                }
                // Get coordinates from address
                CompleteAddress coordinates = new CompleteAddress(SetLocationsActivity.this);

                SearchResultsAdapter searchResultsAdapter = new SearchResultsAdapter(suggestionsList);
               /* searchResultsRV.addItemDecoration(new DividerItemDecoration(SetLocationsActivity.this,
                        DividerItemDecoration.VERTICAL));*/
                binding.searchResultsRV.setAdapter(searchResultsAdapter);
                binding.searchResultsRV.setHasFixedSize(true);
                binding.searchResultsRV.setLayoutManager(new LinearLayoutManager(SetLocationsActivity.this));
                searchResultsAdapter.setListener((item, position) -> {

                    String name = suggestionsList.get(position).getAddress();
                    System.out.println("LOCATION NAME" + name);

                    LatLng userDestination = coordinates.getLatLng(name);
                    //LatLng [latitude=-26.204102799999998, longitude=28.0473051, altitude=0.0]
                    System.out.println("userDestination " + userDestination.toString());
                    System.out.println("Coordinates" + coordinates.getLatLng(name));
                    Toast.makeText(SetLocationsActivity.this, "Click " + name, Toast.LENGTH_SHORT).show();
                    String pickUpLocation = binding.userPickUpLocation.getText().toString();

                    if (!TextUtils.isEmpty(pickUpLocation)) {
                        Intent i = new Intent(SetLocationsActivity.this, OrderActivity.class);
                        i.putExtra("Pick Up point", binding.userPickUpLocation.getText().toString().trim());
                        i.putExtra("Destination point", name);
                        i.putExtra("User location", currentLocation);
                        i.putExtra("User destination", userDestination);
                        startActivity(i);
                        finish();
                    }
                });
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("Search destinations", "Search error: ", e);
        }
    };

}
