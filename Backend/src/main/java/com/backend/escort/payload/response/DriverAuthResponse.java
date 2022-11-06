package com.backend.escort.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DriverAuthResponse {
    private String name;
    private Long driverId;
    private boolean profileUploaded;
    private boolean suspended;
    private List<String> role;
    private Long orgId;
}
