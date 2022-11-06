package com.backend.escort.controller;

/*
 * This controller contains the driver mobile app functionality
 */

import com.backend.escort.helpers.Helper;
import com.backend.escort.helpers.NotFoundException;
import com.backend.escort.model.*;
import com.backend.escort.payload.request.ReviewRequest;
import com.backend.escort.payload.response.DriverInfo;
import com.backend.escort.repository.*;
import com.backend.escort.security.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/driver")

public class DriverController {

    // Show all available trips *DONE*
    // Accept a trip , send a deviceID to the requester
    // Change default password *DONE*
    // upload a picture
    // performance -> amounts of trips taken / number of requests per day
    // get data for the heatmap

    @Autowired
    ImageStorageService imageStorageService;

    @Autowired
    DriverImageRepository driverImageRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TripRepository tripRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    EmergencyRepository emergencyRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    StudentRepository studentRepository;



    // Get all trips that are assigned to driver
    @GetMapping("/{driverId}/assigned-requests")
    public ResponseEntity<?> getAllDriverRequests(@PathVariable(value = "driverId") Long id){
        if(!driverRepository.existsById(id)){
            return ResponseEntity.badRequest().body(String.format(Helper.DRIVER_ID_NOT_FOUND,id));
        }
        List<Trip> trips = tripRepository.findByDriverId(id);
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    @GetMapping("/{userId}/driver-id")
    public ResponseEntity<Long> getDriverId(@PathVariable(value = "userId")Long userId){
        Driver driver = driverRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(String.format(Helper.USER_NOT_FOUND,userId)));
        return new ResponseEntity<>(driver.getId(),HttpStatus.OK);
    }


    // Retrieve the list of trips that the driver has not or completed accepted yet
    // A list of trips with a complete status of false
    @GetMapping("/{driverId}/available-requests")
    public ResponseEntity<?> getAllRequests(@PathVariable(value = "driverId") Long id){
        if(!driverRepository.existsById(id)){
            return ResponseEntity.badRequest().body(String.format(Helper.DRIVER_ID_NOT_FOUND,id));
        }
        List<Trip> allTrips = tripRepository.findByDriverId(id);
        List<Trip> trips = new ArrayList<>();
        for(Trip t : allTrips){
            if(!t.isAccepted())
                trips.add(t);
        }

        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    /*
     * Allow the DRIVER to rate a STUDENT
     */

    @PostMapping("/{studentId}/{driverId}/student-review")
    public ResponseEntity<Review> createNewStudentReview(@PathVariable(value = "studentId") Long studentId,
                                                        @PathVariable(value = "driverId") Long driverId,
                                                        @RequestBody ReviewRequest review) {

        Student student = studentRepository.getById(studentId);
        Review studentReview = new Review(
                review.getRating(),
                review.getComment(),
                studentId,
                driverId,
                student.getOrganisation().getId()
        );
        return new ResponseEntity<>(reviewRepository.save(studentReview), HttpStatus.OK);
    }


    // Get all trips by driver Id
    @GetMapping("/{driverId}/trips")
    public ResponseEntity<List<Trip>> getAllDriverTrips(@PathVariable(value = "driverId") Long driverId){
        List<Trip> allTrips = tripRepository.findByDriverId(driverId);
        List<Trip> trips = new ArrayList<>();
        for(Trip t : allTrips){
            if(t.isAccepted())
                trips.add(t);
        }
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    @GetMapping("/{orgId}/notifications")
    public ResponseEntity<?> getOrgNotifications(@PathVariable(value = "orgId") Long orgId){
        List<Notification> notifications = notificationRepository.findByOrgId(orgId);
        if(notifications.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(notifications,HttpStatus.OK);
    }

    // Make trips expire after a certain period
    /*
     * Number of trips accepted , assigned, cancelled, completed
     * Expired trips
     * Hotspot regions to consider
     * Response rate -> compare the time a trip was assigned and when I accepted
     * Distance travelling
     */

    // Update driver password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable(value = "userId")Long id, @Valid
                                            @RequestBody String password){
        return ResponseEntity.ok().body("New password " + password);
    }

    @PutMapping("/{tripId}/accept-trip")
    public ResponseEntity<?> updateTrip(@PathVariable(value = "tripId")Long id){
        Trip trip = tripRepository.findById(id).orElseThrow(()-> new NotFoundException(
                String.format("Trip with id %s not found",id)));
        trip.setAccepted(true);
        trip.setCompleted(true);
        tripRepository.save(trip);
        return new ResponseEntity<>(trip,HttpStatus.OK);
    }

    @PutMapping("/{tripId}/cancel-trip")
    public ResponseEntity<?> cancelTrip(@PathVariable (value = "tripId") Long tripId){
        Trip trip = tripRepository.getById(tripId);
        trip.setAccepted(false);
        trip.setCompleted(false);
        trip.setCancelled(false);
        return new ResponseEntity<>(
                trip,
                HttpStatus.OK
        );
    }



    @GetMapping("/{driverId}/driver-info")
    public ResponseEntity<?> getDriverInfo(@PathVariable(value = "driverId")Long driverId){
        Driver driver = driverRepository.getById(driverId);

        User user = driver.getUser();
        List<Review> reviews = reviewRepository.findByDriverId(driverId);
        int total = 0;
        for(Review review : reviews){
            total += review.getRating();
        }

        double totalRating = 0.0;
        if(reviews.size() > 0){
            totalRating = (double)total/ reviews.size();
        }

        DriverImages driverImages = driverImageRepository.findByDriverId(driverId);

        List<Trip> trips = tripRepository.findByDriverId(driverId);

        DriverInfo driverInfo = new DriverInfo(
                driver.getId(),
                driver.getTitle(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getGender(),
                user.getEmail(),
                driverImages.getUrl(),
                driver.isVerified(),
                driver.getDateCreated(),
                null,
                String.format("%.2f", totalRating),
                trips.size(),
                reviews.size()
        );

        return new ResponseEntity<>(driverInfo,HttpStatus.OK);
    }


    /*
    Allow driver to change their profile photo

     */

    @GetMapping("/{orgId}/org-emergencies")
    public ResponseEntity<List<Emergency>> getAllOrgEmergencies(@PathVariable(value = "orgId") Long orgId){
        List<Emergency> emergencies = emergencyRepository.findByOrgId(orgId);
        if(emergencies.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(emergencies,HttpStatus.OK);
    }


    @PostMapping("/{driverId}/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(@PathVariable (value = "driverId") Long driverId,
                                                @RequestParam("file") MultipartFile file){
        if(!driverRepository.existsById(driverId))
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        try {
            imageStorageService.save2(file, driverId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Profile uploaded",HttpStatus.OK);
    }





















































































































































































































































































































    // Accept a trip
    @PutMapping("/request/{tripId}")
    public ResponseEntity<Trip> acceptTrip(@PathVariable(value = "tripId")Long id){
            Trip trip = tripRepository.findById(id).orElseThrow(()-> new NotFoundException(
                String.format(Helper.ACCOUNT_INFO)
        ));
        trip.setAccepted(true);
        return new ResponseEntity<>(trip,HttpStatus.OK);
    }
}

