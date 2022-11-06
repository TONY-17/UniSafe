package com.backend.escort.controller;

import com.backend.escort.helpers.Helper;
import com.backend.escort.helpers.NotFoundException;
import com.backend.escort.model.*;
import com.backend.escort.payload.request.DriverRequest;
import com.backend.escort.payload.response.DriverDetails;
import com.backend.escort.payload.response.DriverInfo;
import com.backend.escort.payload.response.OrgInfoResponse;
import com.backend.escort.payload.response.TripInfo;
import com.backend.escort.repository.*;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static java.time.DayOfWeek.*;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/admin")
public class AdminController {

    @Autowired
    Helper helper = new Helper();
    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    AlertRepository alertRepository;
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Autowired
    DriverImageRepository driverImageRepository;

    // Allow admins to create new organisations
    @PostMapping("/{adminId}/new-org")
    public ResponseEntity<?> createOrganisation(@PathVariable(value = "adminId") Long id, @RequestBody Organisation org) {
        Organisation organisation = userRepository.findById(id).map(admin -> {

            Organisation temp = new Organisation(
                    org.getName(),
                    org.getDomain().toLowerCase(),
                    org.isActive(),
                    0.0,
                    null,
                    org.getAddress(),
                    org.getCoordinates(),
                    org.getPickUpLocation()
            );

            temp.setUser(admin);
            return organisationRepository.save(temp);
        }).orElseThrow(() -> new NotFoundException(String.format(Helper.USER_NOT_FOUND, id)));
        return new ResponseEntity<>(organisation, HttpStatus.CREATED);
    }

    @PutMapping("/orgs/{orgId}")
    public ResponseEntity<?> updateOrganisation(@PathVariable(value = "orgId") Long orgId,
                                                @RequestBody Organisation updated) {
        if (organisationRepository.existsById(orgId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Organisation organisation = organisationRepository.getById(orgId);
        organisation.setActive(updated.isActive());
        organisation.setDomain(updated.getDomain());
        organisation.setAddress(updated.getAddress());
        organisation.setName(updated.getName());

        return new ResponseEntity<>(organisation, HttpStatus.CREATED);
    }


    // Retrieve all the organisations created by an admin
    @GetMapping("/orgs/{adminId}")
    public ResponseEntity<List<Organisation>> getAllOrganisationsByAdminId(@PathVariable(value = "adminId") Long id) {
        if (!userRepository.existsById(id)) {
            // TODO: Check if the user is an admin
            // User with the prev id doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Organisation> organisations = organisationRepository.findByUserId(id);

        String rating = null;
        for (Organisation o : organisations) {
            List<Review> reviewList = reviewRepository.findByOrgId(o.getId());
            int total = 0;
            for (Review review : reviewList) {
                total += review.getRating();
            }

            int size = 5 * reviewList.size();
            double orgRating = 0.0;
            if (size != 0)
                orgRating = (double) total / size;

            if (orgRating < 2.0) {
                rating = "BAD";
            }
            if (orgRating >= 2.0 || orgRating < 3.0) {
                rating = "MID";
            }
            if (orgRating >= 3.0 || orgRating <= 5.0) {
                rating = "HIGH";
            }
            o.setScore(orgRating);
            o.setRating(rating);
            organisationRepository.save(o);
        }

        return new ResponseEntity<>(organisations,
                HttpStatus.OK);
    }


    // Delete an organisation
    @DeleteMapping("/org/{id}")
    public ResponseEntity<HttpStatus> deleteOrganisation(@PathVariable(value = "id") Long id) {
        organisationRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    // Create a new driver that belongs to a certain organisation
    @PostMapping("/{adminId}/{orgId}/new-driver")
    public ResponseEntity<?> createDriver(@PathVariable(value = "orgId") Long id,
                                          @PathVariable(value = "adminId") Long adminId,
                                          @RequestBody DriverRequest newDriver) {

        if (userRepository.existsByEmail(newDriver.getEmail())) {
            return ResponseEntity.badRequest().body(String.format(Helper.REGISTRATION_EMAIL_EXISTS, newDriver.getEmail()));
        }

        // The driver will be given a random password
        String password = Helper.generatePassword(10);
        System.out.println("DRIVER PASSWORD " + password);
        // Store driver details in a user object
        User driver = new User(
                newDriver.getEmail(),
                passwordEncoder.encode(password),
                Role.DRIVER
        );
        Driver _driver = organisationRepository.findById(id).map(organisation -> {
            // Map driver to an organisation
            newDriver.setOrganisation(organisation);
            // Save a new user with the driver details
            userRepository.save(driver);
            newDriver.setUser(driver);
            newDriver.setAdminId(adminId);
            newDriver.setDateCreated(Helper.getCurrentTimeDate());

            helper.sendMail(newDriver.getFirstName(),
                    organisation.getName(),
                    newDriver.getEmail(),
                    password
            );
            return driverRepository.save(Helper.generateDriver(newDriver));
        }).orElseThrow(() -> new NotFoundException(String.format(Helper.ORG_NOT_FOUND, id)));


        return new ResponseEntity<>(_driver, HttpStatus.CREATED);
    }


    @PostMapping("/{orgId}/new-notification")
    public ResponseEntity<?> createNewNotification(@PathVariable(value = "orgId") Long orgId,
                                                   @RequestBody String content) {
        Organisation organisation = organisationRepository.getById(orgId);
        Notification notification = new Notification(
                Helper.getCurrentTimeDate(),
                content,
                organisation.getId()
        );
        notificationRepository.save(notification);

        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }


    // Remove driver from an organisation
    @DeleteMapping("/driver/{id}")
    public ResponseEntity<HttpStatus> deleteDriver(@PathVariable(value = "id") Long id) {
        Driver driver = driverRepository.getById(id);
        driverRepository.deleteById(id);
        User user = driver.getUser();
        userRepository.delete(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Retrieve a list of all the drivers under an organisation
    @GetMapping("/org/{orgId}/drivers")
    public ResponseEntity<?> getAllDrivers(@PathVariable(value = "orgId") Long id) {
        if (!organisationRepository.existsById(id)) {
            throw new NotFoundException(String.format(Helper.ORG_NOT_FOUND, id));
        }
        List<DriverInfo> allDrivers = new ArrayList<>();

        for (Driver driver : driverRepository.findByOrganisationId(id)) {
            List<Review> reviews = reviewRepository.findByDriverId(driver.getId());
            int total = 0;
            for (Review review : reviews) {
                total += review.getRating();
            }

            double totalRating = 0.0;

            if (reviews.size() > 0) {
                totalRating = (double) total / reviews.size();
            }

            String profileURL = "";
            DriverImages driverImage = driverImageRepository.findByDriverId(driver.getId());
            if (driverImage != null) {
                profileURL = driverImage.getUrl();
            }
            //List<Review> reviewList = reviewRepository.findByDriverId(driver.getId());
            User user = driver.getUser();
            allDrivers.add(new DriverInfo(
                    driver.getId(),
                    driver.getTitle(),
                    driver.getFirstName(),
                    driver.getLastName(),
                    driver.getGender(),
                    user.getEmail(),
                    profileURL,
                    driver.isVerified(),
                    driver.getDateCreated(),
                    null,
                    String.format("%.2f", totalRating),
                    0,
                    0
            ));
        }
        return new ResponseEntity<>(allDrivers, HttpStatus.OK);
    }

    // Get all the drivers registered by an admin from all the organisations
    @GetMapping("/{adminId}/drivers")
    public ResponseEntity<?> getAllDriversRegisteredByAdmin(@PathVariable(value = "adminId") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.ok().body(String.format(Helper.USER_NOT_FOUND, id));
        }
        List<DriverInfo> allDrivers = new ArrayList<>();
        for (Driver driver : driverRepository.findByAdminId(id)) {
            List<Review> reviews = reviewRepository.findByDriverId(driver.getId());
            int total = 0;
            for (Review review : reviews) {
                total += review.getRating();
            }

            double totalRating = 0.0;

            if (reviews.size() > 0) {
                totalRating = (double) total / reviews.size();
            }
            String profileURL = "";
            DriverImages driverImage = driverImageRepository.findByDriverId(driver.getId());
            if (driverImage != null) {
                profileURL = driverImage.getUrl();
            }
            User user = driver.getUser();
            allDrivers.add(new DriverInfo(
                    driver.getId(),
                    driver.getTitle(),
                    driver.getFirstName(),
                    driver.getLastName(),
                    driver.getGender(),
                    user.getEmail(),
                    profileURL,
                    driver.isVerified(),
                    driver.getDateCreated(),
                    null,
                    String.format("%.2f", totalRating),
                    0,0
            ));
        }
        return new ResponseEntity<>(allDrivers, HttpStatus.OK);
    }


    @GetMapping("/{adminId}/company-details")
    public ResponseEntity<?> companyDetails(@PathVariable(value = "adminId") Long id) {
        Admin admin = adminRepository.findByUserId(id);
        return new ResponseEntity<>(
                admin,
                HttpStatus.OK
        );
    }

    @PutMapping("/{driverId}/suspend-driver")
    public ResponseEntity<?> suspendDriver(@PathVariable(value = "driverId") Long driverId) {
        Driver driver = driverRepository.getById(driverId);
        driver.setSuspended(true);
        driverRepository.save(driver);
        return new ResponseEntity<>(
                driver,
                HttpStatus.OK
        );
    }


    @PutMapping("/{adminId}/edit-company")
    public ResponseEntity<?> updateCompany(@PathVariable(value = "adminId") Long id,
                                           @RequestBody Admin body) {
        Admin admin = adminRepository.findByUserId(id);
        admin.setCompany(body.getCompany());
        admin.setFirstName(body.getFirstName());
        admin.setLastName(body.getLastName());
        admin.setPhoneNumber(body.getPhoneNumber());

        // save new changes
        adminRepository.save(admin);
        return new ResponseEntity<>(
                admin,
                HttpStatus.OK
        );
    }


    // Get all the trips
    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getAllTrips() {
        // Get all by admin Id or trips from organisations managed by an admin
        List<Trip> trips = new ArrayList<>();
        tripRepository.findAll().forEach(trips::add);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }


/*
    @GetMapping("/trips/{orgId}")
    public ResponseEntity<?> getAllTripsByOrgId(@PathVariable(value = "orgId") Long id) {
        List<Trip> allTrips = tripRepository.findByOrgId(id);
        if (allTrips.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // We need to know how many trips have expired, cancelled, ongoing , completed
        String totalExpired, totalCancelled, totalOngoing, totalCompleted;
        Predicate<Trip> cancelled = trip -> trip.isCancelled();
        Predicate<Trip> completed = trip -> trip.isCompleted();
        Predicate<Trip> ongoing = trip -> trip.isAccepted();

        // All trips that are cancelled
        totalCancelled = allTrips.stream()
                .filter(cancelled)
                .map(trip -> trip.getOrgId().toString())
                .collect(Collectors.joining(",", "[", "]"));

        // All trips that are currently completed
        totalCompleted = allTrips.stream()
                .filter(completed)
                .map(trip -> trip.getOrgId().toString())
                .collect(Collectors.joining(",", "[", "]"));

        // All trips that are currently ongoing
        totalOngoing = allTrips.stream()
                .filter(ongoing)
                .map(trip -> trip.getOrgId().toString())
                .collect(Collectors.joining(",", "[", "]"));

        // on the next update, make sure the driver is able to filter the dates
        // Display the current day rates

        long cancelledSum = totalCancelled.chars().filter(num -> num == ',').count() + 1;
        long ongoingSum = totalOngoing.chars().filter(num -> num == ',').count() + 1;
        long completedSum = totalCompleted.chars().filter(num -> num == ',').count() + 1;
        int expiredSum;

        System.out.println("TRIP SIZE " + cancelledSum);
        System.out.println("TRIP SIZE " + allTrips.size());
        double cancelledPercent = 100.0 * ((double) cancelledSum / allTrips.size());
        double ongoingPercent = 100.0 * ((double) ongoingSum / allTrips.size());
        double completedPercent = 100.0 * ((double) completedSum / allTrips.size());
        //double  expiredPercent = ((totalExpired.chars().filter(num -> num == ',').count() + 1)/allTrips.size()) * 100;

        return new ResponseEntity<>(new TripInfoRes(
                allTrips,
                0l,
                cancelledSum,
                ongoingSum,
                completedSum,
                cancelledPercent,
                ongoingPercent,
                completedPercent,
                0f
        ), HttpStatus.OK);
    }
*/

    @GetMapping("/trips/{orgId}")
    public ResponseEntity<?> getAllTripsByOrgId(@PathVariable(value = "orgId") Long id) {
        List<Trip> trips = tripRepository.findByOrgId(id);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    // Get all the trips
    @GetMapping("/trips-updated")
    public ResponseEntity<List<TripInfo>> getAllTripsUpdated() {
        List<TripInfo> trips = new ArrayList<>();
        // Needs to be updated -> retrieve all trips by adminID
        for (Trip t : tripRepository.findAll()) {
            User user = t.getUser();
            Driver driver = driverRepository.findByUserId(t.getDriverId()).orElseThrow(() -> new NotFoundException("Resource not found"));
            Student student = studentRepository.findByUserId(user.getId());
            trips.add(new TripInfo(t, driver, student.getUsername(),"",0,0,"",""));

        }

        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    @GetMapping("/trips-updated/{orgId}")
    public ResponseEntity<List<TripInfo>> getAllTripsByOrgIdUpdated(@PathVariable(value = "orgId") Long id) {
        List<Trip> allTrips = tripRepository.findByOrgId(id);
        if (allTrips.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<TripInfo> trips = new ArrayList<>();
        for (Trip t : allTrips) {
            User user = t.getUser();
            Driver driver = driverRepository.findByUserId(t.getDriverId()).orElseThrow(() -> new NotFoundException("Resource not found"));
            Student student = studentRepository.findByUserId(user.getId());
            trips.add(new TripInfo(t, driver, student.getUsername(),"",0,0,"",""));
        }
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }


    // url example https:localhost:8080/api/admin/search?query=tony
    @GetMapping("/search/drivers")
    public ResponseEntity<List<Driver>> searchDrivers(@RequestParam("query") String query) {
        List<Driver> drivers = driverRepository.searchDrivers(query);
        if (drivers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @GetMapping("/search/organisations")
    public ResponseEntity<List<Organisation>> searchOrganisations(@RequestParam("query") String query) {
        List<Organisation> organisations = organisationRepository.searchByName(query);
        if (organisations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(organisations, HttpStatus.OK);
    }

    @GetMapping("/search/trips")
    public ResponseEntity<List<Trip>> searchTrips(@RequestParam("query") String query) {
        List<Trip> trips = tripRepository.searchTrips(query);
        if (trips.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(trips, HttpStatus.OK);
    }

    // Wipe database
    @PostMapping("/delete-all-trips")
    public ResponseEntity<?> deleteAll() {
        //userRepository.deleteAll();
        alertRepository.deleteAll();
        driverRepository.deleteAll();
        //organisationRepository.deleteAll();
        //studentRepository.deleteAll();
        //userRepository.deleteAll();
        //tripRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/{orgId}/org-info")
    public ResponseEntity<?> getOrgInfo(@PathVariable(value = "orgId") Long orgId) {
        Organisation organisation = organisationRepository.getById(orgId);
        List<Driver> drivers = driverRepository.findByOrganisationId(orgId);
        List<Notification> notifications = notificationRepository.findByOrgId(orgId);
        // NAME , MESSAGES
        OrgInfoResponse orgInfoResponse = new OrgInfoResponse(
                organisation.getName(),
                notifications
        );
        return new ResponseEntity<>(orgInfoResponse, HttpStatus.OK);
    }


    /*
     * Given an ID return all the information about a driver
     */
    @GetMapping("/{driverId}/driver-info")
    public ResponseEntity<?> getEditableDriverInfo(@PathVariable(value = "driverId") Long driverId) {
        Driver driver = driverRepository.getById(driverId);
        User user = driver.getUser();
        List<Review> reviewList = reviewRepository.findByDriverId(driverId);
        List<Review> reviews = reviewRepository.findByDriverId(driver.getId());
        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }

        double totalRating = 0.0;

        if (reviews.size() > 0) {
            totalRating = (double) total / reviews.size();
        }


        DriverInfo driverInfo = new DriverInfo(
                driverId,
                driver.getTitle(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getGender(),
                user.getEmail(),
                "",
                driver.isVerified(),
                driver.getDateCreated(),
                reviewList,
                String.format("%.2f", totalRating),
                0,
                0
        );
        return new ResponseEntity<>(driverInfo, HttpStatus.OK);
    }

    /*
     * Given an ID return all the information about a driver
     * Return values are the number of trips,
     * emergencies , reviews , performance
     */

    @GetMapping("/{driverId}/view-driver-details")
    public ResponseEntity<?> allDriverTips(@PathVariable(value = "driverId") Long driverId) throws ParseException {

        /*
         * Get the current dates/days for the week
         */
        LocalDate current = LocalDate.now();
        LocalDate monday = current.with(previousOrSame(MONDAY));
        LocalDate tuesday = current.with(previousOrSame(TUESDAY));
        LocalDate wednesday = current.with(previousOrSame(WEDNESDAY));
        LocalDate thursday = current.with(previousOrSame(THURSDAY));
        LocalDate friday = current.with(previousOrSame(FRIDAY));
        LocalDate saturday = current.with(nextOrSame(SATURDAY));
        LocalDate sunday = current.with(nextOrSame(SUNDAY));


        // Convert local date to date


        Date mondayDate = Helper.convertLocalDate(monday);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(mondayDate);
        Integer dayMon = calendar1.get(Calendar.DAY_OF_WEEK);


        Date tuesdayDate = Helper.convertLocalDate(tuesday);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(tuesdayDate);
        Integer dayTue = calendar2.get(Calendar.DAY_OF_WEEK);


        Date wednesdayDate = Helper.convertLocalDate(wednesday);

        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(wednesdayDate);
        Integer dayWed = calendar3.get(Calendar.DAY_OF_WEEK);


        Date thursdayDate = Helper.convertLocalDate(thursday);


        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(thursdayDate);
        Integer dayThur = calendar3.get(Calendar.DAY_OF_WEEK);


        Date fridayDate = Helper.convertLocalDate(friday);

        Calendar calendar5 = Calendar.getInstance();
        calendar5.setTime(fridayDate);
        Integer dayFri = calendar5.get(Calendar.DAY_OF_WEEK);

        Date saturdayDate = Helper.convertLocalDate(saturday);

        Calendar calendar6 = Calendar.getInstance();
        calendar6.setTime(saturdayDate);
        Integer daySat = calendar6.get(Calendar.DAY_OF_WEEK);


        Date sundayDate = Helper.convertLocalDate(sunday);

        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(sundayDate);
        Integer daySun = calendar7.get(Calendar.DAY_OF_WEEK);

        HashMap<String, Integer> driverMap = Helper.map();

        List<Trip> driverTrips = tripRepository.findByDriverId(driverId);


        for (Trip t : driverTrips) {
            // Grab the current trip date
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(t.getDateCreated());

            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            // Detemine the day of the current trip
            Integer day = cursor.get(Calendar.DAY_OF_WEEK);

            if (day == dayMon) {
                driverMap.put("MON", driverMap.getOrDefault("MON", 0) + 1);
            } else if (day == dayTue) {
                driverMap.put("TUE", driverMap.getOrDefault("TUE", 0) + 1);
            } else if (day == dayWed) {
                driverMap.put("WED", driverMap.getOrDefault("WED", 0) + 1);
            } else if (day == dayThur) {
                driverMap.put("THUR", driverMap.getOrDefault("THUR", 0) + 1);
            } else if (day == dayFri) {
                driverMap.put("FRI", driverMap.getOrDefault("FRI", 0) + 1);
            } else if (day == daySat) {
                driverMap.put("SAT", driverMap.getOrDefault("SAT", 0) + 1);
            } else if (day == daySun) {
                driverMap.put("SUN", driverMap.getOrDefault("SUN", 0) + 1);
            }

        }


        Driver driver = driverRepository.getById(driverId);

        List<Trip> orgTrips = tripRepository.findByOrgId(driver.getOrganisation().getId());
        HashMap<String, Integer> orgMap = Helper.map();

        for (Trip t : orgTrips) {
            Date date = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").parse(t.getDateCreated());

            Calendar cursor = Calendar.getInstance();
            cursor.setTime(date);
            // Detemine the day of the current trip
            Integer day = cursor.get(Calendar.DAY_OF_WEEK);

            if (day == dayMon) {
                orgMap.put("MON", driverMap.getOrDefault("MON", 0) + 1);
            } else if (day == dayTue) {
                orgMap.put("TUE", driverMap.getOrDefault("TUE", 0) + 1);
            } else if (day == dayWed) {
                orgMap.put("WED", driverMap.getOrDefault("WED", 0) + 1);
            } else if (day == dayThur) {
                orgMap.put("THUR", driverMap.getOrDefault("THUR", 0) + 1);
            } else if (day == dayFri) {
                orgMap.put("FRI", driverMap.getOrDefault("FRI", 0) + 1);
            } else if (day == daySat) {
                orgMap.put("SAT", driverMap.getOrDefault("SAT", 0) + 1);
            } else if (day == daySun) {
                orgMap.put("SUN", driverMap.getOrDefault("SUN", 0) + 1);
            }

        }

        int total = 0;
        List<Review> driverReviews = reviewRepository.findByDriverId(driverId);
        System.out.println("DRIVER REVIEWS " + driverReviews.toString());
        for (Review review : driverReviews) {
            total += review.getRating();
        }

        int size = 5 * driverReviews.size();
        // This is the driver rating out of 5

        double driverRating = (total / size) * 5;
        // check if we are not dividing by zero
        // only if the driver has no reviews
        System.out.println("DRIVER REVIEWS " + size + " " + driverReviews.size() + " " + total);
/*        if(size != 0){
            ;
        }*/

        // Handle for when size is empty

        System.out.println("DRIVER REVIEWS " + driverRating);


        List<Review> orgReviews = reviewRepository.findByOrgId(driver.getOrganisation().getId());
        System.out.println("DRIVER REVIEWS " + orgReviews.toString());
        // This will store the id of the driver and their total reviews
        HashMap<Long, Integer> driverReview = new HashMap<>();
        for (Review review : orgReviews) {
            driverReview.put(driverId, driverReview.getOrDefault(driverId, 0) + review.getRating());
        }


        int index = 0;
        int[] rankings = new int[driverMap.size()];
        for (Integer value : driverReview.values()) {
            rankings[++index] = value;
        }
        // Sort the rankings in asc order
        Arrays.sort(rankings);
        // Show the rankings from highest to lowest
        ArrayUtils.reverse(rankings);
        System.out.println("DRIVER RANKINGS " + Arrays.toString(rankings));

        // Check the ranking of the current driver
        int driverRanking = 0;
        for (int i = 0; i < rankings.length; i++) {
            if (total == rankings[i])
                driverRanking = i;

        }


        DriverDetails driverDetails = new DriverDetails(
                driverTrips.size(),
                driverMap,
                orgMap,
                driverRating,
                driverRanking
        );

        return new ResponseEntity<>(driverDetails, HttpStatus.OK);
    }


    /*
     * Given the id of a driver be able to update their information
     */
    @PutMapping("/{driverId}/update-driver")
    public ResponseEntity<?> updateDriver(@PathVariable(value = "driverId") Long driverId, @RequestBody DriverRequest update) {

        // Retrieve driver that has the supplied ID
        Driver driver = driverRepository.getById(driverId);
        User user = driver.getUser();

        // Change the old values to the sent new values
        driver.setTitle(update.getTitle());
        driver.setFirstName(update.getFirstName());
        driver.setLastName(update.getLastName());
        // The email field is stored in the user table table so the email will be changed as follows
        user.setEmail(update.getEmail());
        driver.setGender(update.getGender());
        driver.setDateCreated(driver.getDateCreated());
        // Keep the same organisation
        driver.setOrganisation(driver.getOrganisation());

        // Save the updated fields in the repository
        driverRepository.save(driver);
        userRepository.save(user);

        return new ResponseEntity<>(driver, HttpStatus.OK);
    }



    /*

     */


}
