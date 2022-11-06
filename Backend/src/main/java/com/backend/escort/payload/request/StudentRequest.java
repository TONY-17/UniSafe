package com.backend.escort.payload.request;

import com.backend.escort.model.Organisation;
import com.backend.escort.model.User;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class StudentRequest {
    @NotBlank
    @Column(name = "username")
    private String username;
    @NotBlank
    @Column(name = "email")
    private String email;
    @NotBlank
    @Column(name = "password")
    private String password;

    private Organisation organisation;
    private User user;
}
