package com.backend.escort.payload.response;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmergencyResponse {
    private double latitude;
    private double longitude;

}
