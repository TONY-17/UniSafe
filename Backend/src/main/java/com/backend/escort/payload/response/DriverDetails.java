package com.backend.escort.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverDetails {
    private int totalNumberOfTrips;
    private HashMap<String, Integer> driverTrips;
    private HashMap<String, Integer> orgTrips;
    private double rating;
    private int ranking;
}

