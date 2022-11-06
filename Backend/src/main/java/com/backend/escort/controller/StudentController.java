package com.backend.escort.controller;

import com.backend.escort.helpers.Helper;
import com.backend.escort.helpers.NotFoundException;
import com.backend.escort.model.*;
import com.backend.escort.payload.request.EmergencyRequest;
import com.backend.escort.payload.request.InfoUpdate;
import com.backend.escort.payload.request.ReviewRequest;
import com.backend.escort.payload.request.TripRequest;
import com.backend.escort.payload.response.*;
import com.backend.escort.repository.*;
import com.backend.escort.security.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * This controller contains the student mobile app functionality
 *
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/student")

public class StudentController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    DriverImageRepository driverImageRepository;
    @Autowired
    AlertRepository alertRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ImagesRepository imagesRepository;
    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    EmergencyRepository emergencyRepository;
    @Autowired
    TripRepository tripRepository;

    @Autowired
    ImageStorageService imageStorageService;

    @Autowired
    ReviewRepository reviewRepository;


    // Send a trip request
    @PostMapping("/{orgId}/{studentId}/request-trip")
    public ResponseEntity<?> requestTrip(@PathVariable(value = "studentId") Long studentId,
                                         @PathVariable(value = "orgId") Long orgId,
                                         @Valid @RequestBody TripRequest trip) {

        if (!organisationRepository.existsById(orgId)) {
            throw new NotFoundException(String.format(Helper.ORG_NOT_FOUND, orgId));
        }
        // Retrieve a list of all the drivers under an organisation
        List<Driver> allDrivers = driverRepository.findByOrganisationId(orgId);

        if (allDrivers.isEmpty()) {
            throw new NotFoundException("Organisation does not have drivers" +
                    "cannot take trip requests");
        }

        Driver randomDriver = Helper.getRandomElement(allDrivers);

        Student student = studentRepository.findByUserId(studentId);

        Trip trip1 = userRepository.findById(studentId).map(user -> {

            Trip request = new Trip(
                    Helper.getCurrentTimeDate(),
                    trip.getPickUp(),
                    trip.getDestination(),
                    trip.getPickUpPoint(),
                    trip.getDestinationPoint(),
                    trip.isCompleted(),
                    trip.isCancelled(),
                    trip.isAccepted(),
                    randomDriver.getId(),
                    orgId,
                    trip.getChannel(),
                    student.getUsername());

            request.setUser(user);

            return tripRepository.save(request);
        }).orElseThrow(() -> new NotFoundException(String.format(Helper.USER_NOT_FOUND, studentId)));
        // Fetch the random drivers Profile image
        // Will throw a NullPointerException if theres no driver image
        DriverImages driverImage = driverImageRepository.findByDriverId(randomDriver.getId());
        /*List<Trip> trips = tripRepository.findByDriverId(randomDriver.getId());*/
        List<Review> reviews = reviewRepository.findByDriverId(randomDriver.getId());
        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        double totalRating = 0.0;
        if (reviews.size() > 0) {
            totalRating = (double) total / reviews.size();
        }

        return new ResponseEntity<>(new TripInfo(
                trip1, randomDriver,
                student.getUsername(), driverImage.getUrl(),
                totalRating, reviews.size(),
                trip.getPickUp(),
                trip.getDestination()), HttpStatus.OK);
    }

    // Get all trips
    @GetMapping("/{userId}/student-trips")
    public ResponseEntity<?> getAllTrips(@PathVariable(value = "userId") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.ok().body(String.format(Helper.USER_NOT_FOUND, id));
        }
        List<Trip> trips = tripRepository.findByUserId(id);
        if (trips.isEmpty()) {
            return ResponseEntity.ok().body("Student has not requested yet");
        } else {
            return new ResponseEntity<>(trips, HttpStatus.OK);
        }
    }

    @GetMapping("/{userId}/student-id")
    public ResponseEntity<Long> getStudentId(@PathVariable(value = "userId") Long userId) {
        Student student = studentRepository.findByUserId(userId);
        return new ResponseEntity<>(student.getId(), HttpStatus.OK);
    }


    @PutMapping("/{studentId}/{userId}/update-student")
    public ResponseEntity<?> updateStudentInfo(@PathVariable(value = "studentId") Long studentId,
                                               @PathVariable(value = "userId") Long userId,
                                               @RequestBody InfoUpdate infoUpdate) {
        // Check if the supplied id exists
        if (!userRepository.existsById(userId) && !studentRepository.existsById(studentId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // Check if the supplied domain exists
        String domain = Helper.extractDomain(infoUpdate.getEmail());

        if (!organisationRepository.existsByDomain(domain)) {
            return ResponseEntity.ok("The organisation does not exist");
        }

        Optional<User> user = userRepository.findById(userId);
        Optional<Student> student = studentRepository.findById(studentId);

        if (user.isPresent() && student.isPresent()) {
            User _user = user.get();
            // Check if the user is under the same organisation
            String oldEmailDomain = Helper.extractDomain(_user.getEmail());
            System.out.println("OLD EMAIL DOMAIN " + oldEmailDomain);
            System.out.println("NEW EMAIL DOMAIN " + domain);

            // Flag if the old email domain is not the same as the new one
            if (!oldEmailDomain.equals(domain)) {
                return ResponseEntity.ok("Switching organisations not allowed");
            }
            // Update the stored records
            _user.setEmail(infoUpdate.getEmail());
            // Saving the content
            userRepository.save(_user);

            Student _student = student.get();
            _student.setUsername(infoUpdate.getUsername());
            studentRepository.save(_student);

            return new ResponseEntity<>(student, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    // Allow a student to send an alert to a community with media support
    @PostMapping("/{studentId}/new-media-alert")
    public ResponseEntity<?> createMediaAlert(@PathVariable(value = "studentId") Long id,
                                              @RequestPart("tag") String tag,
                                              @RequestPart("body") String body,
                                              @RequestPart("files") List<MultipartFile> files) {

        Student s = studentRepository.getById(id);
        Alert alert1 = studentRepository.findById(id).map(student -> {
            // Generate a unique id to be able to retrieve images for each alert
            String uniqueAlertId = Helper.generatePassword(20);
            Alert newAlert = new Alert(
                    tag,
                    body,
                    Helper.getCurrentTimeDate(),
                    false,
                    s.getOrganisation().getId(),
                    uniqueAlertId
            );
            uploadFiles(files, uniqueAlertId);
            // Map the student who created the alert to the post
            newAlert.setStudent(student);
            return alertRepository.save(newAlert);
        }).orElseThrow(() -> new NotFoundException(String.format("Student id %s does not exist", id)));
        return ResponseEntity.ok().body(String.format(Helper.ALERT_CREATED, alert1.toString()));
    }


    public void uploadFiles(List<MultipartFile> files, String uniqueAlertId) {
        for (MultipartFile file : files) {
            try {
                imageStorageService.save(file, uniqueAlertId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Allow a student to send an alert to a community
    @PostMapping("/{studentId}/new-alert")
    public ResponseEntity<?> createAlert(@PathVariable(value = "studentId") Long id,
                                         @RequestBody Alert alert) {

        Student s = studentRepository.getById(id);
        Alert alert1 = studentRepository.findById(id).map(student -> {
            // Generate a unique id to be able to retrieve images for each alert
            String uniqueAlertId = Helper.generatePassword(20);
            Alert newAlert = new Alert(
                    alert.getTag(),
                    alert.getBody(),
                    Helper.getCurrentTimeDate(),
                    false,
                    s.getOrganisation().getId(),
                    uniqueAlertId
            );
            // Map the student who created the alert to the post
            newAlert.setStudent(student);
            return alertRepository.save(newAlert);
        }).orElseThrow(() -> new NotFoundException(String.format("Student id %s does not exist", id)));
        return ResponseEntity.ok().body(String.format(Helper.ALERT_CREATED, alert1.toString()));
    }


    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Resource file = imageStorageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @GetMapping("/{orgId}/alerts")
    public ResponseEntity<?> getAllAlerts(@PathVariable(value = "orgId") Long orgId) {
        List<AlertInfo> alerts = new ArrayList<>();
        for (Alert alert : alertRepository.findByOrgId(orgId)) {
            // Get the username of each person who posted the
            Student student = alert.getStudent();
            // Get all the comments with this ID so we can get the comments count
            List<Comment> comments = commentRepository.findByAlertId(alert.getId());
            List<Image> imageList = imagesRepository.findByUniqueAlertId(alert.getUniqueAlertId());
            List<String> URLs = new ArrayList<>();
            if (!imageList.isEmpty()) {
                for (Image i : imageList) {
                    URLs.add(i.getUrl());
                }
            }
            alerts.add(new AlertInfo(
                    alert.getId(),
                    student.getUsername(),
                    alert.getTag(),
                    alert.getBody(),
                    alert.getTimePosted(),
                    comments.size(),
                    URLs
            ));

        }
        if (alerts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }

    @PostMapping("/delete-alerts")
    public String deleteAlerts() {
        alertRepository.deleteAll();
        imagesRepository.deleteAll();
        return "DONE";
    }

    // Retrieve all the alerts that are not reported
    @GetMapping("/{orgId}/alerts/{tag}")
    public ResponseEntity<?> getAllAlertsByCategory(@PathVariable(value = "orgId") Long orgId,
                                                    @PathVariable(value = "tag") String tag) {

        List<Alert> alerts = new ArrayList<>();
        for (Alert alert : alertRepository.findByOrgId(orgId)) {
            if (alert.getTag().equals(tag)) {
                alerts.add(alert);
            }
        }
        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }


    //Get all comments of an alert
    @GetMapping("/alerts/{alertId}/comments")
    public ResponseEntity<?> getAllCommentsByAlertId(
            @PathVariable(value = "alertId") Long id) {

        if (!alertRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<CommentInfo> comments = new ArrayList<>();

        for (Comment comment : commentRepository.findByAlertId(id)) {

            // Student student = comment.getAlert().getStudent();
            Student student = studentRepository.getById(comment.getStudentId());
            comments.add(new CommentInfo(
                    comment.getId(),
                    student.getUsername(),
                    comment
            ));
        }
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }


    @GetMapping("/{orgId}/org-driver-size")
    public ResponseEntity<Integer> getOrganisationSize(@PathVariable(value = "orgId") Long id) {
        List<Driver> drivers = driverRepository.findByOrganisationId(id);
        if (drivers.isEmpty()) {
            return null;
        } else {
            return new ResponseEntity<>(drivers.size(), HttpStatus.OK);
        }
    }

    // Allow a user to be able to comment on an alert
    @PostMapping("/alerts/{alertId}/{studentId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable(value = "alertId") Long id,
            @PathVariable(value = "studentId") Long studentId,
            @Valid @RequestBody Comment comment
    ) {

        Student student = studentRepository.findById(studentId).orElseThrow(() ->
                new NotFoundException(String.format("Student Id %s not found ", studentId)));

        Comment comment1 = alertRepository.findById(id).map(
                alert -> {
                    comment.setDateCreated(Helper.getCurrentTimeDate());
                    comment.setAlert(alert);
                    comment.setStudentId(studentId);
                    return commentRepository.save(comment);
                }
        ).orElseThrow(() -> new NotFoundException(
                "Alert with id " + id + " not found"
        ));
        return ResponseEntity.ok().body(new CommentInfo(comment1.getId(), student.getUsername(), comment1));
    }

    @GetMapping("/{userId}/student-details")
    public ResponseEntity<?> studentDetails(@PathVariable(value = "userId") Long userId) {
        User user = userRepository.getById(userId);
        Student student = studentRepository.findByUserId(user.getId());
        List<Review> reviews = reviewRepository.findByStudentId(student.getId());
        int total = 0;
        for (Review r : reviews) {
            total += r.getRating();
        }
        double computedRating = (double) total / reviews.size();

        return new ResponseEntity<>(new StudentDetails(student.getUsername(), user.getEmail(), computedRating), HttpStatus.OK);
    }

    /*
     * Allow the student to rate a driver
     */

    @PostMapping("/{studentId}/{driverId}/new-review")
    public ResponseEntity<Review> createNewDriverReview(@PathVariable(value = "studentId") Long studentId,
                                                        @PathVariable(value = "driverId") Long driverId,
                                                        @RequestBody ReviewRequest review) {
        Driver driver = driverRepository.getById(driverId);

        Review driverReview = new Review(
                review.getRating(),
                review.getComment(),
                studentId,
                driverId,
                driver.getOrganisation().getId()
        );
        return new ResponseEntity<>(reviewRepository.save(driverReview), HttpStatus.OK);
    }


    @PostMapping("/{studentId}/{orgId}/new-emergency")
    public ResponseEntity<Emergency> createNewEmergency(@PathVariable(value = "studentId") Long studentId,
                                                        @PathVariable(value = "orgId") Long orgId,
                                                        @RequestBody EmergencyRequest emergencyRequest) {
        Emergency emergency = new Emergency(
                emergencyRequest.getDuration(),
                Helper.getCurrentTimeDate(),
                studentId,
                orgId,
                emergencyRequest.getLocation(),
                emergencyRequest.getType(),
                emergencyRequest.getAddress()
        );
        return new ResponseEntity<>(emergencyRepository.save(emergency), HttpStatus.OK);
    }

    @GetMapping("/{studentId}/all-emergencies")
    public ResponseEntity<List<Emergency>> getAllStudentEmergencies(@PathVariable(value = "studentId") Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        List<Emergency> emergencies = emergencyRepository.findByStudentId(studentId);
        if (emergencies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(emergencies, HttpStatus.OK);
    }


    /*
     * Get coordinates of each emergency from the specified organisation ID
     */

    @GetMapping("/{orgId}/emergency-points")
    public ResponseEntity<?> getAllEmergencyPoints(@PathVariable(value = "orgId") Long orgId) {
        if (!organisationRepository.existsById(orgId)) {
            return ResponseEntity.ok(String.format(Helper.ORG_NOT_FOUND, orgId));
        }

        List<Emergency> emergencies = emergencyRepository.findByOrgId(orgId);
        // Check if the organisation has any emergencies
        if (emergencies.isEmpty()) {
            return ResponseEntity.ok("No emergencies");
        }

        List<EmergencyResponse> responses = new ArrayList<>();

        for (Emergency e : emergencies) {
            String[] formattedLocation = e.getLocation().split(",");
            double latitude = Double.parseDouble(formattedLocation[0]);
            double longitude = Double.parseDouble(formattedLocation[1]);
            responses.add(new EmergencyResponse(latitude, longitude));
        }

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }


    @PostMapping("/delete-all-emergencies")
    public ResponseEntity<String> deleteEmergencies() {
        emergencyRepository.deleteAll();
        alertRepository.deleteAll();
        return new ResponseEntity<>("DONE DELETING", HttpStatus.OK);
    }


}
