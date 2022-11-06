package com.backend.escort.payload.request;

import com.backend.escort.model.Role;
import com.backend.escort.model.User;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegisterRequest {
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

}
