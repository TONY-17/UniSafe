package com.backend.escort.payload.request;

import com.backend.escort.model.Organisation;
import com.backend.escort.model.User;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DriverRequest {
    @NotBlank
    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "email")
    @Email
    private String email;

    @NotBlank
    @Column(name = "name")
    private String firstName;

    @NotBlank
    @Column(name = "surname")
    private String lastName;

    @NotBlank
    @Column(name = "gender")
    private String gender;

    @NotBlank
    @Column(name = "date_created")
    private String dateCreated;

    private Organisation organisation;
    private User user;
    private Long adminId;
}
