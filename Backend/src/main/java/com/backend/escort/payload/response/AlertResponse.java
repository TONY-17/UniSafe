package com.backend.escort.payload.response;

import com.backend.escort.model.Alert;

import java.util.ArrayList;
import java.util.List;

public class AlertResponse {
    List<Alert> alertList = new ArrayList<>();

    public AlertResponse(List<Alert> alertList) {
        this.alertList = alertList;
    }
}
