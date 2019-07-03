package com.drag.user.model;

import java.io.Serializable;

public class Trip implements Serializable {

    private String status;
    private Cab cab;
    private Cab travelDetails;

    public Trip(String status, Cab cab, Cab travelDetails) {
        this.status = status;
        this.cab = cab;
        this.travelDetails = travelDetails;
    }

    public String getStatus() {
        return status;
    }

    public Cab getCab() {
        return cab;
    }

    public Cab getTravelDetails() {
        return travelDetails;
    }
}