package com.backend.escort.payload.request;

import com.backend.escort.model.User;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TripRequest {
    @NotBlank
    @Column(name = "pickUp")
    private String pickUp;
    @NotBlank
    @Column(name = "destination")
    private String destination;
    @NotBlank
    @Column(name = "pickUpPoint")
    private String pickUpPoint;
    @NotBlank
    @Column(name = "destinationPoint")
    private String destinationPoint;
    @Column(name = "date_created")
    private String dateCreated;
    @Column(name = "completed")
    private boolean completed;

    @Column(name = "cancelled")
    private boolean cancelled;

    @Column(name = "accepted")
    private boolean accepted;


    @Column(name = "channel")
    private String channel;

    // The driver is going to a user
    private User user;
}
