package com.drag.user.model;

import java.io.Serializable;

public class Rider implements Serializable {

    private String tripId;
    private String tripStatus;
    private String pickup;
    private String drop;
    private String seats;
    private String fare;

    public String getTripId() {
        return tripId;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public String getFare() {
        return fare;
    }

    public String getSeats() {
        return seats;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDrop() {
        return drop;
    }
}