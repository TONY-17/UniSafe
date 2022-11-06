package com.backend.escort.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class InfoUpdate {
    private String email;
    private String username;
}
