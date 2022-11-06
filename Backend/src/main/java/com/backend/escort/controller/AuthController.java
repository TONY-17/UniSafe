package com.backend.escort.controller;

import com.backend.escort.helpers.Helper;
import com.backend.escort.helpers.NotFoundException;
import com.backend.escort.model.*;
import com.backend.escort.payload.request.CompanyRequest;
import com.backend.escort.payload.request.LoginRequest;
import com.backend.escort.payload.request.RegisterRequest;
import com.backend.escort.payload.request.StudentRequest;
import com.backend.escort.payload.response.AdminResponse;
import com.backend.escort.payload.response.DriverAuthResponse;
import com.backend.escort.payload.response.UserInfoResponse;
import com.backend.escort.repository.*;
import com.backend.escort.security.jwt.JWT;
import com.backend.escort.security.service.ImageStorageService;
import com.backend.escort.security.service.UserDetailsImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    @Autowired
    DriverRepository driverRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager manager;
    @Autowired
    JWT jwt;

    @Autowired
    DriverImageRepository driverImageRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    AdminRepository adminRepository;

    // Create company
    // Extra details on the emergencies
    // -> All durations of the emergencies
    // -> Avg -> Types -> Response time -> new / added


    // Admin register functionality
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody CompanyRequest request) {
        // Check if the email already exists in the data store
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(String.format(Helper.REGISTRATION_EMAIL_EXISTS, request.getEmail()));
        }
        // Create a new account
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ADMIN
        );

        Admin companyProfile = new Admin(
                request.getFirstName(),
                request.getLastName(),
                request.getFirstName() + " Security",
                null,
                user
        );

        userRepository.save(user);
        adminRepository.save(companyProfile);
        return ResponseEntity.ok(String.format(Helper.REGISTRATION_SUCCESS, request.getEmail()));
    }

    // Student register functionality
    @PostMapping("/register-student")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody StudentRequest student) {
        // Check if the email already exists in the data store
        if (userRepository.existsByEmail(student.getEmail())) {
            return ResponseEntity.badRequest().body(String.format(Helper.REGISTRATION_EMAIL_EXISTS, student.getEmail()));
        }
        String domain = Helper.extractDomain(student.getEmail()).toLowerCase();
        /*
         * Check if the organisation exists
         * will compare the extracted domain to the saved domains
         */
        if (!organisationRepository.existsByDomain(domain)) {
            return ResponseEntity.badRequest().body(Helper.REGISTRATION_ORG_ERROR);
        }
        // Obtain an organisation based on the supplied domain
        Organisation organisation = organisationRepository.findByDomain(domain);
        // link the student to the right organisation
        student.setOrganisation(organisation);
        // Create a new account
        User newAccount = new User(
                student.getEmail(),
                passwordEncoder.encode(student.getPassword()),
                Role.USER
        );
        // link student to a user_id
        student.setUser(newAccount);
        // Save the user in the user table
        userRepository.save(newAccount);
        // Save the student in the student table
        studentRepository.save(Helper.generateStudent(student));

        return ResponseEntity.ok(String.format(Helper.REGISTRATION_SUCCESS, student.getEmail().toLowerCase()));
    }

    // Authenticate users based on the supplied details
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Check the supplied email and password
        Authentication authenticate = manager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        UserDetailsImp user = (UserDetailsImp) authenticate.getPrincipal();
        ResponseCookie responseCookie = jwt.generateJWT(user);
        // Stores a list of roles the current user has
        List<String> authorities = user.getAuthorities()
                .stream().map(
                        GrantedAuthority::getAuthority).collect(Collectors.toList());

        if (authorities.get(0).equals("USER")) {
            String domain = Helper.extractDomain(loginRequest.getEmail());
            Organisation organisation = organisationRepository.findByDomain(domain);
            Student student = studentRepository.findByUserId(user.getId());
            UserInfoResponse userInfoResponse = new UserInfoResponse(
                    user.getId(),
                    organisation.getId(),
                    student.getUsername(),
                    student.getId(),
                    organisation.getName(),
                    user.getEmail(),
                    authorities,
                    organisation.getPickUpLocation(),
                    organisation.getCoordinates());

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(userInfoResponse);

        } else if (authorities.get(0).equals("DRIVER")) {
            Driver driver = driverRepository.findByUserId(user.getId()).orElseThrow(() ->
                    new NotFoundException(String.format("ID %s not found", user.getId())));
            if (!driver.isVerified())
                driver.setVerified(true);

            // Check if the user has uploaded a profile yet or not
            boolean profileUploaded = false;
            DriverImages driverImages = null;
            if (driverImageRepository.existsByDriverId(driver.getId()))
                profileUploaded = true;
            driverImages = driverImageRepository.findByDriverId(driver.getId());

            driverRepository.save(driver);

            DriverAuthResponse driverAuthResponse = new DriverAuthResponse(
                    driver.getFirstName(),
                    driver.getId(),
                    profileUploaded,
                    false,
                    authorities,
                    driver.getOrganisation().getId()
            );

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(driverAuthResponse);

        }

        AdminResponse adminResponse = new AdminResponse(

                user.getId(),
                null,
                user.getEmail(),
                authorities
        );

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(adminResponse);
    }




}
