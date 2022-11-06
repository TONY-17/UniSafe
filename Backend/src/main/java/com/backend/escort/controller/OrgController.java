package com.backend.escort.controller;


import com.backend.escort.helpers.Helper;
import com.backend.escort.model.Emergency;
import com.backend.escort.payload.response.EmergencyResponse;
import com.backend.escort.repository.EmergencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/v1/org")
public class OrgController {

    @Autowired
    EmergencyRepository emergencyRepository;

    @GetMapping("/{orgId}/org-emergencies")
    public ResponseEntity<?> getAllOrgEmergencies(@PathVariable(value = "orgId") Long orgId) throws ParseException {
        List<Emergency> emergencies = emergencyRepository.findByOrgId(orgId);
        if (emergencies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(emergencies, HttpStatus.OK);
    }
}
