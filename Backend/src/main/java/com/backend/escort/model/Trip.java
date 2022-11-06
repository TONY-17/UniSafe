package com.backend.escort.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name = "trips"
)
@Getter
@Setter
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @NotBlank
    @Column(name = "pick_up")
    private String pickUp;
    @NotBlank
    @Column(name = "destination")
    private String destination;

    @Column(name = "date_created")
    private String dateCreated;

    @NotBlank
    @Column(name = "pick_up_point")
    private String pickUpPoint;
    @NotBlank
    @Column(name = "destination_point")
    private String destinationPoint;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "cancelled")
    private boolean cancelled;

    @Column(name = "accepted")
    private boolean accepted;

    @Column(name = "driver_id")
    private Long driverId;


    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "channel")
    private String channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @Column(name = "student")
    private String student;

    public Trip(@NotBlank String dateCreated,
                @NotBlank String pickUp,
                @NotBlank String destination,
                @NotBlank String pickUpPoint,
                @NotBlank String destinationPoint,
                @NotBlank boolean completed,
                @NotBlank boolean cancelled,
                @NotBlank boolean accepted,
                @NotBlank Long driverId,
                @NotBlank Long orgId,
                @NotBlank String channel,
                @NotBlank String student) {

        this.dateCreated = dateCreated;
        this.pickUp = pickUp;
        this.destination = destination;
        this.pickUpPoint = pickUpPoint;
        this.destinationPoint = destinationPoint;
        this.completed = completed;
        this.cancelled = cancelled;
        this.accepted = accepted;
        this.driverId = driverId;
        this.orgId = orgId;
        this.channel = channel;
        this.student = student;
    }
}
