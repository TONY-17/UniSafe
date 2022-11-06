package com.backend.escort.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EmergencyReports {
    Long emergencyId;
    private String student;
    private String location;
    private boolean resolved;
    String type;
}
