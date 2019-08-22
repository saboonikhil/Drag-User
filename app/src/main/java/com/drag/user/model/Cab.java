package com.drag.user.model;

import java.io.Serializable;

public class Cab implements Serializable {

    private String _id;
    private boolean isAvailable;
    private boolean isShared;
    private String type;
    private String pickup;
    private String drop;
    private String startTime;
    private String endTime;
    private String fare;
    private Rider[] riders;
    private String driverName;
    private String driverContact;
    private String carName;
    private String carNumber;

    public Cab(String pickup, String drop, String startTime) {
        this.pickup = pickup;
        this.drop = drop;
        this.startTime = startTime;
    }

    public String get_id() {
        return _id;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDrop() {
        return drop;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getType() {
        return type;
    }

    public String getCarName() {
        return carName;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverContact() {
        return driverContact;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean isShared() {
        return isShared;
    }

    public Rider[] getRiders() {
        return riders;
    }

    public String getFare() {
        return fare;
    }
}