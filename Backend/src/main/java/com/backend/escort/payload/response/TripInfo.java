package com.backend.escort.payload.response;

import com.backend.escort.model.Driver;
import com.backend.escort.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TripInfo {
    private Trip trip;
    private Driver driver;
    private String user;
    private String driverImage;
    private double rating;
    private int totalReviews;
    private String pickUp;
    private String destination;
}
