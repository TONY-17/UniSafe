package com.backend.escort.payload.response;

import com.backend.escort.model.Driver;
import com.backend.escort.model.Notification;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class OrgInfoResponse {
    private String name;
/*    private List<Driver> driverList;*/
    private List<Notification> notificationList;
}
