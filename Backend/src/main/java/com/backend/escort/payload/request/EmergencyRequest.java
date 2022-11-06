package com.backend.escort.payload.request;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class EmergencyRequest {
    private String duration;
    private String location;
    private String type;
    private String address;
}
