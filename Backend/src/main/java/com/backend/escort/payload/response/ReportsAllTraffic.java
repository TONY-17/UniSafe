package com.backend.escort.payload.response;

import lombok.*;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ReportsAllTraffic {
    HashMap<String,Integer> emergenciesCategories;
    HashMap<String,Integer> emergenciesPlaces;
    HashMap<String,Integer> tripsPickUp;
    HashMap<String,Integer> tripsDestination;
    int avgNumberOfRequests;
    String avgResponseTimes;

}
