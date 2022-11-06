package com.backend.escort.helpers.dataimportexport;

import ch.lambdaj.Lambda;
import com.backend.escort.helpers.Helper;
import com.backend.escort.model.*;
import com.backend.escort.payload.response.ExcelResponse;
import com.backend.escort.repository.DriverRepository;
import com.backend.escort.repository.OrganisationRepository;
import com.backend.escort.repository.TripRepository;
import com.backend.escort.repository.UserRepository;
import com.backend.escort.security.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {
    @Autowired
    ImageStorageService imageStorageService;
    @Autowired
    Helper helper = new Helper();
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    TripRepository tripRepository;

    public ByteArrayInputStream loadDrivers(Long adminId) {
        // Only return all the drivers are registered by certain company or admin
        List<Driver> drivers = driverRepository.findByAdminId(adminId);
        ByteArrayInputStream byteArrayInputStream = ExcelHelper.driversToExcelSheet(drivers);
        return byteArrayInputStream;
    }

    public ByteArrayInputStream loadTrips(Long orgId) {
        List<Trip> trips = tripRepository.findByOrgId(orgId);
        ByteArrayInputStream byteArrayInputStream = ExcelHelper.tripsToExcelSheet(trips);
        return byteArrayInputStream;
    }


    // Receive an excel file from admin
    public ExcelResponse save(MultipartFile multipartFile, Long adminId) {
        try {
            List<CustomDriver> drivers = ExcelHelper.importExcelFile(multipartFile.getInputStream());
            List<Driver> driverList = new ArrayList<>();

            List<String> alreadyExisting = new ArrayList<>();
            for (CustomDriver driver : drivers) {
                // NB!! Improve system to check for existing users
                if (!userRepository.existsByEmail(driver.getEmail())) {
                    // Create user accounts for each of the drivers and send them credentials to access the system
                    String password = Helper.generatePassword(10);
                    User user = new User(
                            driver.getEmail(),
                            passwordEncoder.encode(password),
                            Role.DRIVER
                    );
                    // Send each driver an invitation
                    helper.sendMail(
                            driver.getFirstName(),
                            driver.getOrganisationName(),
                            driver.getEmail(),
                            password
                    );
                    // Determine the org they are in based on the supplied name from the excel file
                    Organisation organisation = organisationRepository.findByName(driver.getOrganisationName());
                    // Create an actual driver from imported details
                    Driver driver1 = new Driver(
                            driver.getTitle(),
                            driver.getFirstName(),
                            driver.getLastName(),
                            driver.getGender(),
                            driver.getDateCreated(),
                            organisation,
                            user,
                            adminId
                    );

                    driverList.add(driver1);
                    userRepository.save(user);
                    driverRepository.save(driver1);
                    imageStorageService.save2(null, driver.getId());
                } else {
                    alreadyExisting.add(driver.getEmail());
                }
            }

            List<String> newAccounts = Lambda.extract(driverList, Lambda.on(Driver.class).getUser().getEmail());
            return new ExcelResponse(
                    driverList.size(),
                    alreadyExisting.size(),
                    newAccounts,
                    alreadyExisting
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to import file data " + e.getMessage());
        }
    }

}
