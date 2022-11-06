package com.backend.escort.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CustomDriver {
    private Long id;
    private String title;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String dateCreated;
    private boolean verified;
    private boolean suspended;
    private String organisationName;
    private Long adminId;

}
