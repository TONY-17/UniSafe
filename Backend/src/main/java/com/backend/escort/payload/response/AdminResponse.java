package com.backend.escort.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private Long adminId;
    private String email;
    private List<String> role;

}
