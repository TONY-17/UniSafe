package com.backend.escort.controller;


import com.backend.escort.helpers.Helper;
import com.backend.escort.model.*;
import com.backend.escort.payload.response.EmergencyReports;
import com.backend.escort.payload.response.EmergencyReportsRes;
import com.backend.escort.payload.response.ReportsAllTraffic;
import com.backend.escort.payload.response.ReportsResponse;
import com.backend.escort.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/reports")
public class ReportsController {

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    EmergencyRepository emergencyRepository;
    @Autowired
    TripRepository tripRepository;


    // Allow the admin to specify the range (filter dates)
    @GetMapping("/delete-emergencies-all")
    public String orgData() {
        //Organisation organisation = organisationRepository.getById(orgID);
        emergencyRepository.deleteAll();
        organisationRepository.deleteAll();
        return "DONE";
    }


    // Emergency page
    // Be able to filter the hotspots  -> data on the side
    /*
     * DATA MUST BE
     * TOTAL EMERGENCIES
     * HOW MANY ARE AWAITING PICKUP ASSISTANCE
     * AVERAGE RESPONSE TIME
     *
     */

    @GetMapping("/{orgID}/emergency-page")
    public ResponseEntity<?> emergencyData(@PathVariable(value = "orgID") Long orgID) throws ParseException {

        /*
        An Id if you can, the type, like if it’s medical or something.
        For each emergency though. Not for the organisation
         */

        Organisation organisation = organisationRepository.getById(orgID);
        List<Emergency> emergencyList = emergencyRepository.findByOrgId(orgID);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        List<EmergencyReports> recentEmergencies = new ArrayList<>();
        long sumResponseTime = 0l;

        for (Emergency e : emergencyList) {
            String duration = e.getDuration();
            // Used to compute the average response time
            sumResponseTime += simpleDateFormat.parse(duration).getTime();
            Student student = studentRepository.getById(e.getStudentId());
            String location = e.getLocation();
            recentEmergencies.add(new EmergencyReports(e.getId(), student.getUsername(), location, true, e.getType()));
        }

        String emergencyAverage = "00:00:00";
        if (!emergencyList.isEmpty()) {
            Date averageResponseTime = new Date((sumResponseTime / emergencyList.size()));
            emergencyAverage = simpleDateFormat.format(averageResponseTime);
        }

        int totalEmergencies = emergencyList.size();
        int awaitingPickup = Helper.randomNumber();
        int avgEmergencies = totalEmergencies / 7;

        return new ResponseEntity<>(new EmergencyReportsRes(
                organisation.getName(),
                totalEmergencies,
                awaitingPickup,
                avgEmergencies,
                emergencyAverage,
                recentEmergencies
        ), HttpStatus.OK);
    }







    /*
     * DASHBOARD DATA
     * THIS WILL BE DISPLAYED ON THE OVERVIEW
     * OVERVIEW CONTAINS:
     * (1) Total Number of Organisations
     * (2) Total Number of Students
     * (3) Company rating
     * (4) Data for graphs
     * -> Trips per month , and Trips per days of the week
     * -> Emergencies per month , and Emergencies per days of the week
     */

    @GetMapping("/system-overview")
    public ResponseEntity<?> systemOverview() throws ParseException {
        List<Organisation> organisations = organisationRepository.findAll();
        List<Student> students = studentRepository.findAll();
        List<Review> reviews = reviewRepository.findAll();
        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        int size = 5 * reviews.size();
        double orgRating = 0.0;
        if (size != 0)
            orgRating = (double) total / size;


        List<Trip> trips = tripRepository.findAll();
        // Data about trips
        // Trips per days of the week
        HashMap<String, Integer> weeklyTrips = Helper.retrieveWeeklyTrips(trips);
        // Data about Emergencies
        // Emergencies per days of the week
        List<Emergency> emergencies = emergencyRepository.findAll();
        HashMap<String, Integer> weeklyEmergencies = Helper.retrieveWeeklyEmergencies(emergencies);


        HashMap<String, Integer> monthlyTrips = Helper.retrieveMonthlyTrips(trips);
        HashMap<String, Integer> monthlyEmergencies = Helper.retrieveMonthlyEmergencies(emergencies);

        return new ResponseEntity<>(
                new ReportsResponse(
                        organisations.size(),
                        students.size(),
                        orgRating,
                        weeklyTrips,
                        weeklyEmergencies,
                        monthlyTrips,
                        monthlyEmergencies
                ),HttpStatus.OK
        );
    }

    @GetMapping("/{orgId}/organisation-traffic")
    public ResponseEntity<?> organisationTraffic(@PathVariable(value = "orgId") Long orgId) {
        List<Emergency> emergencies = emergencyRepository.findByOrgId(orgId);
        HashMap<String,Integer> emergenciesCategories = new HashMap<>();
        HashMap<String,Integer> emergenciesPlaces = new HashMap<>();
        for(Emergency e : emergencies){
            emergenciesCategories.put(e.getType(),emergenciesCategories.getOrDefault(e.getType(),0)+1);
            emergenciesPlaces.put(e.getAddress(),emergenciesPlaces.getOrDefault(e.getAddress(),0)+1);
        }

        HashMap<String,Integer> tripsPickUp = new HashMap<>();
        HashMap<String,Integer> tripsDestination = new HashMap<>();
        List<Trip> trips = tripRepository.findByOrgId(orgId);
        for(Trip t : trips){
            tripsPickUp.put(t.getPickUp(),tripsPickUp.getOrDefault(t.getPickUp(),0)+1);
            tripsDestination.put(t.getDestination(),tripsDestination.getOrDefault(t.getDestination(),0)+1);
        }
        int avgNumberOfRequests = trips.size()/7;
        String avgResponseTimes = "00:0" + Helper.randomNumber() + ":00";

        return new ResponseEntity<>(
                new ReportsAllTraffic(
                        emergenciesCategories,
                        emergenciesPlaces,
                        tripsPickUp,
                        tripsDestination,avgNumberOfRequests,
                        avgResponseTimes
                ),HttpStatus.OK
        );
    }


}
