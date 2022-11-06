package com.backend.escort.payload.response;
import java.util.List;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EmergencyReportsRes {
    String organisationName;
    int totalEmergencies;
    int awaitingPickup;
    int avgEmergencies;
    String emergencyAverage;
    List<EmergencyReports> recentEmergencies;
}
