package com.backend.escort.payload.response;

import lombok.*;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ReportsResponse {
    int totalOrganisations;
    int totalStudents;
    double companyRating;
    HashMap<String, Integer> weeklyTrips;
    HashMap<String, Integer> weeklyEmergencies;
    HashMap<String, Integer> monthlyTrips;
    HashMap<String, Integer> monthlyEmergencies;

}
