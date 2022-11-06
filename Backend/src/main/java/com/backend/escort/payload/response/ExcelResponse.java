package com.backend.escort.payload.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExcelResponse {
    private int numAddedDrivers;
    private int numExistingDriver;
    List<String> newAccounts;
    List<String> alreadyExisting;
}
