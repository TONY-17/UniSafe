package com.backend.escort.payload.response;

import com.backend.escort.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class DriverInfo {
    private Long id;
    private String title;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String picture;
    private boolean verified;
    private String dateCreated;
    List<Review> reviewList;
    String rating;
    private int totalRides;
    private int totalRatings;
}
