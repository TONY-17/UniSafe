package com.backend.escort.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DriverDeletionReq {
    List<Long> driverIDs;

}
