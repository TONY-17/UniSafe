package com.backend.escort.payload.response;

import com.backend.escort.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripInfoRes {
    List<Trip> allTrips;
    Long totalExpired;
    Long totalCancelled;
    Long totalOngoing ;
    Long totalCompleted;
    double  cancelledPercent;
    double  ongoingPercent;
    double  completedPercent;
    double  expiredPercent;
}
