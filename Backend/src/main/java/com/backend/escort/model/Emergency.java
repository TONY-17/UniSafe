package com.backend.escort.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name = "emergencies"
)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Emergency {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @NotBlank
    @Column(name = "duration")
    private String duration;

    @Column(name = "dateCreated")
    private String dateCreated;

    @Column(name = "studentId")
    private Long studentId;

    @Column(name = "orgId")
    private Long orgId;

    @Column(name = "location")
    private String location;

    @Column(name = "address")
    private String address;

    @Column(name = "type")
    private String type;


    public Emergency(@NotBlank String duration, String dateCreated, Long studentId, Long orgId, String location, String type,String address) {
        this.duration = duration;
        this.dateCreated = dateCreated;
        this.studentId = studentId;
        this.orgId = orgId;
        this.location = location;
        this.type = type;
        this.address = address;
    }
}
