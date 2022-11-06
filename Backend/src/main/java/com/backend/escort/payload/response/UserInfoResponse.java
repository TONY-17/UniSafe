package com.backend.escort.payload.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@ToString
@Getter
@Setter
public class UserInfoResponse {
    private Long id;
    private String email;
    private String username;
    private String organisation;
    private Long orgId;
    private Long studentId;
    private String imageURL;
    private List<String> role;
    private List<String> pickUpPoints;
    private double[] coordinates;


    public UserInfoResponse(Long id, Long orgId, String username, Long studentId, String organisation, String email,
                            List<String> role,
                            List<String> pickUpPoints,
                            double[] coordinates) {
        this.id = id;
        this.orgId = orgId;
        this.email = email;
        this.username = username;
        this.organisation = organisation;
        this.role = role;
        this.studentId = studentId;
        this.pickUpPoints = pickUpPoints;
        this.coordinates = coordinates;
    }

}
