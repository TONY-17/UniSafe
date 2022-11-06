package com.backend.escort.controller;


import com.backend.escort.helpers.Helper;
import com.backend.escort.model.*;
import com.backend.escort.payload.response.ListVarInfo;
import com.backend.escort.payload.response.StudentsOnSystemInfo;
import com.backend.escort.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/dashboard")
public class DashboardController {

    /*
     * Show how many students are using the system
     * Total drivers
     * Top performing drivers
     * Overall satisfaction
     * Average waiting times requests/emergencies
     * Most recent emergencies
     */

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EmergencyRepository emergencyRepository;

    @Autowired
    TripRepository tripRepository;

    // Based on the organisations the admin is managing indicate how many STUDENTS do we have at each organisation
    @GetMapping("/{adminId}/total-students")
    public ResponseEntity<?> studentsOnSystem(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);

        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }

        /*
         * This will store the organisation and the number of students it has
         * We need to know the number of students within each organisation
         * Example => ["University of Johannesburg", 34], ["University of Cape Town", 14]
         */
        HashMap<String, Integer> orgStudentCount = new HashMap<>();
        // We need to know the total number of within the organisations being managed by the current admin
        int totalNumberOfStudentsAllOrganisations = 0;

        for (Organisation o : organisations) {
            List<Student> students = studentRepository.findByOrganisationId(o.getId());
            // Store the org name and the number of students within the organisation
            orgStudentCount.put(o.getName(), students.size());
            totalNumberOfStudentsAllOrganisations += students.size();
        }

        return new ResponseEntity<>(new StudentsOnSystemInfo(
                orgStudentCount,
                totalNumberOfStudentsAllOrganisations
        ), HttpStatus.OK);

    }


    // Based on the organisations the admin is managing indicate how many DRIVERS do we have at each organisation
    @GetMapping("/{adminId}/total-drivers")
    public ResponseEntity<?> driversOnSystem(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);

        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }

        /*
         * This will store the organisation and the number of students it has
         * We need to know the number of students within each organisation
         * Example => ["University of Johannesburg", 34], ["University of Cape Town", 14]
         */
        HashMap<String, Integer> orgDriverCount = new HashMap<>();
        // We need to know the total number of within the organisations being managed by the current admin
        int totalNumberOfDriversAllOrganisations = 0;

        for (Organisation o : organisations) {
            List<Driver> drivers = driverRepository.findByOrganisationId(o.getId());
            // Store the org name and the number of students within the organisation
            orgDriverCount.put(o.getName(), drivers.size());
            totalNumberOfDriversAllOrganisations += drivers.size();
        }

        return new ResponseEntity<>(new StudentsOnSystemInfo(
                orgDriverCount,
                totalNumberOfDriversAllOrganisations
        ), HttpStatus.OK);

    }


    // Based on driver ratings determine who has the best rating in the whole system
    // -> change based on months
    @GetMapping("/{adminId}/driver-ratings")
    public ResponseEntity<?> driverRatings(@PathVariable(value = "adminId") Long adminId) {
        // All the drivers added by the current logged in admin
        List<Driver> drivers = driverRepository.findByAdminId(adminId);
        // Store the driver name and their rating -> Order from highest to low
        //Map<String, Float> driverRatingCount = new HashMap<>();
        List<ListVarInfo> result = new ArrayList<>();
        for (Driver d : drivers) {
            String fullName = d.getFirstName() + " " + d.getLastName();
            // Retrieve all the trips this driver has been assigned to
            List<Trip> trips = tripRepository.findByDriverId(d.getId());
            // Retrieve all the reviews the current user has received
            List<Review> reviews = reviewRepository.findByDriverId(d.getId());
            if (!reviews.isEmpty()) {
                int totalReviews = 0;
                for (Review review : reviews) {
                    totalReviews += review.getRating();
                }
                float rating = totalReviews / reviews.size();
                //driverRatingCount.put(fullName, rating);
                result.add(new ListVarInfo(
                        fullName,
                        rating,
                        trips.size()
                ));

            } else {
                // The default value if the driver has no reviews
                //driverRatingCount.put(fullName, 0f);
                result.add(new ListVarInfo(
                        fullName,
                        0f,
                        trips.size()
                ));
            }
        }
        //LinkedHashMap<String, Float> sortedRatingList = Helper.sortMap(driverRatingCount);
        // Return a sorted list of driver ratings from highest to lowest
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    /*
     * Returns values meant to be used on a graph
     * The data can be used to cancelled, expired,ongoing, completed trips per organisation
     */

    @GetMapping("/{adminId}/graph-data")
    public ResponseEntity<?> organisationData(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }
        // Stores an organisation and values for each of the trip type
        HashMap<String, OrgDataInfo> orgTripCount = new HashMap<>();
        for (Organisation organisation : organisations) {
            List<Trip> trips = tripRepository.findByOrgId(organisation.getId());

            // Determine which trips have been cancelled, completed, expired, ongoing

            Predicate<Trip> cancelled = trip -> trip.isCancelled();

            Predicate<Trip> completed = trip -> trip.isCompleted();

            Predicate<Trip> ongoing = trip -> trip.isAccepted();


            // All trips that are cancelled
            String cancelledTrips = trips.stream()
                    .filter(cancelled)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));


            // All trips that are currently completed
            String completedTrips = trips.stream()
                    .filter(completed)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));

            // All trips that are currently ongoing
            String ongoingTrips = trips.stream()
                    .filter(ongoing)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));


            long cancelled2 = cancelledTrips.chars().filter(num -> num == ',').count();
            long completed2 = completedTrips.chars().filter(num -> num == ',').count();
            long expired2 = 0;
            long ongoing2 = ongoingTrips.chars().filter(num -> num == ',').count();

            if (cancelled2 > 0) {
                cancelled2 = cancelledTrips.chars().filter(num -> num == ',').count() + 1;
            }

            if (completed2 > 0) {
                completed2 = completedTrips.chars().filter(num -> num == ',').count() + 1;
            }

            if (ongoing2 > 0) {
                ongoing2 = ongoingTrips.chars().filter(num -> num == ',').count() + 1;
            }

            orgTripCount.put(Helper.removeWhiteSpaces(organisation.getName()), new OrgDataInfo(
                    cancelled2,
                    completed2,
                    expired2,
                    ongoing2
            ));
        }

        return new ResponseEntity<>(orgTripCount, HttpStatus.OK);

    }


    @GetMapping("/{adminId}/completed-trips")
    public ResponseEntity<?> completedTrips(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }
        // Stores an organisation and values for each of the trip type
        HashMap<String, Integer> tripsMap = new HashMap<>();
        for (Organisation organisation : organisations) {
            List<Trip> trips = tripRepository.findByOrgId(organisation.getId());
            Predicate<Trip> completed = trip -> trip.isCompleted();
            // All trips that are currently completed
            String completedTrips = trips.stream()
                    .filter(completed)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));
            long completed2 = completedTrips.chars().filter(num -> num == ',').count();
            if (completed2 > 0) {
                completed2 = completedTrips.chars().filter(num -> num == ',').count() + 1;
            }

            tripsMap.put(
                    Helper.removeWhiteSpaces(organisation.getName()),
                    Math.toIntExact(completed2));
        }
        return new ResponseEntity<>(tripsMap, HttpStatus.OK);
    }

    @GetMapping("/{adminId}/cancelled-trips")
    public ResponseEntity<?> cancelledTrips(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }
        // Stores an organisation and values for each of the trip type
        HashMap<String, Integer> tripsMap = new HashMap<>();
        for (Organisation organisation : organisations) {
            List<Trip> trips = tripRepository.findByOrgId(organisation.getId());
            Predicate<Trip> cancelled = trip -> trip.isCancelled();
            // All trips that are currently completed
            String completedTrips = trips.stream()
                    .filter(cancelled)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));
            long completed2 = completedTrips.chars().filter(num -> num == ',').count();
            if (completed2 > 0) {
                completed2 = completedTrips.chars().filter(num -> num == ',').count() + 1;
            }

            tripsMap.put(
                    Helper.removeWhiteSpaces(organisation.getName()),
                    Math.toIntExact(completed2));
        }
        return new ResponseEntity<>(tripsMap, HttpStatus.OK);
    }

    @GetMapping("/{adminId}/ongoing-trips")
    public ResponseEntity<?> ongoingTrips(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }
        // Stores an organisation and values for each of the trip type
        HashMap<String, Integer> tripsMap = new HashMap<>();
        for (Organisation organisation : organisations) {
            List<Trip> trips = tripRepository.findByOrgId(organisation.getId());
            Predicate<Trip> ongoing = trip -> trip.isAccepted();
            // All trips that are currently completed
            String completedTrips = trips.stream()
                    .filter(ongoing)
                    .map(trip -> trip.getOrgId().toString())
                    .collect(Collectors.joining(",", "[", "]"));
            long completed2 = completedTrips.chars().filter(num -> num == ',').count();
            if (completed2 > 0) {
                completed2 = completedTrips.chars().filter(num -> num == ',').count() + 1;
            }

            tripsMap.put(
                    Helper.removeWhiteSpaces(organisation.getName()),
                    Math.toIntExact(completed2));
        }
        return new ResponseEntity<>(tripsMap, HttpStatus.OK);
    }


    @GetMapping("/{adminId}/all-emergencies")
    public ResponseEntity<?> allEmergencies(@PathVariable(value = "adminId") Long Id) {
        List<Organisation> organisations = organisationRepository.findByUserId(Id);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }
        int totalEmergencies = 0;
        Map<String, Integer> orgEmergencies = new HashMap<>();
        for (Organisation org : organisations) {
            List<Emergency> emergencies = emergencyRepository.findByOrgId(org.getId());
            totalEmergencies += emergencies.size();
            orgEmergencies.put(Helper.removeWhiteSpaces(org.getName()), emergencies.size());
        }

        return new ResponseEntity<>(
                orgEmergencies,
                HttpStatus.OK
        );
    }


    /*
     * Calculate average waiting time for each organisation
     * Top Emergencies
     */
    @GetMapping("/{adminId}/org-data")
    public ResponseEntity<?> orgWaiting(@PathVariable(value = "adminId") Long adminId) {
        // Organisations being managed by the current admin
        List<Organisation> organisations = organisationRepository.findByUserId(adminId);
        // The admin doesn't make any organisations
        if (organisations.isEmpty()) {
            return ResponseEntity.ok().body("No organisations found");
        }

        for (Organisation organisation : organisations) {
            List<Emergency> emergencies = emergencyRepository.findByOrgId(organisation.getId());

            Predicate<Emergency> medical = emergency -> emergency.getType() == "Medical";
            Predicate<Emergency> crime = emergency -> emergency.getType() == "Crime";
            Predicate<Emergency> accident = emergency -> emergency.getType() == "Accident";

            // All the medical emergencies
            String medicalFilter = emergencies.stream()
                    .filter(medical)
                    .map(e -> e.getId().toString())
                    .collect(Collectors.joining(",", "[", "]"));

            String crimeFilter = emergencies.stream()
                    .filter(crime)
                    .map(e -> e.getId().toString())
                    .collect(Collectors.joining(",", "[", "]"));

            String accidentFilter = emergencies.stream()
                    .filter(accident)
                    .map(e -> e.getId().toString())
                    .collect(Collectors.joining(",", "[", "]"));

        }
        return null;
    }


}
