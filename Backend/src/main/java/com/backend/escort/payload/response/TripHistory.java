package com.backend.escort.payload.response;

import com.backend.escort.model.Trip;

import java.util.List;

public class TripHistory {
    List<Trip> trips;
    String driver;
    String duration;
    String rating;
    String ratingWords;
    String driverImage;
}
