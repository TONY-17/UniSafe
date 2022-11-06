package com.backend.escort.controller;


import com.backend.escort.helpers.Helper;
import com.backend.escort.helpers.NotFoundException;
import com.backend.escort.helpers.dataimportexport.ExcelHelper;
import com.backend.escort.helpers.dataimportexport.ExcelService;
import com.backend.escort.payload.response.ExcelResponse;
import com.backend.escort.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/excel")
public class ExcelController {

    @Autowired
    ExcelService excelService;

    @Autowired
    OrganisationRepository organisationRepository;

    /*
     * Generates an excel file about the all the drivers in the organisation
     */
    @GetMapping("/{adminId}/export-drivers")
    public ResponseEntity<Resource> getDriverFile(@PathVariable(value = "adminId") Long adminId) {
        String fileName = "drivers.xlsx";
        InputStreamResource file = new InputStreamResource(excelService.loadDrivers(adminId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    /*
     * Generates an excel file about the all the trips in the organisation
     */
    @GetMapping("/{orgId}/export-trips")
    public ResponseEntity<Resource> getTripFile(@PathVariable(value = "orgId") Long orgId) {

        if (!organisationRepository.existsById(orgId)) {
            throw new NotFoundException(String.format(Helper.ORG_NOT_FOUND, orgId));
        }

        String fileName = "trips.xlsx";
        InputStreamResource file = new InputStreamResource(excelService.loadTrips(orgId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);

    }

    /*
     * Allow admins to upload an excel file then store the data in our database
     */

    // Change date created to todays date
    @PostMapping("/{adminId}/upload-file")
    public ResponseEntity<?> uploadDriverExcel(
            @PathVariable(value = "adminId") Long adminId,
            @RequestPart (value = "file", required = true) MultipartFile file) {

        // Check if the file uploaded is the required format
        if (ExcelHelper.isExcelFile(file)) {
            ExcelResponse excelResponse = excelService.save(file, adminId);
            return ResponseEntity.status(HttpStatus.OK).body(
                    "File uploaded: " + file.getOriginalFilename() + "\n"
                            + "Emails added : " + excelResponse.getNumAddedDrivers() + "\n"
                            + "Emails rejected : " + excelResponse.getNumExistingDriver() + "\n"
                            + "New emails : " + excelResponse.getNewAccounts() + "\n"
                            + "Existing emails: " + excelResponse.getAlreadyExisting()
            );
        }

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("File format not supported");
    }




}
