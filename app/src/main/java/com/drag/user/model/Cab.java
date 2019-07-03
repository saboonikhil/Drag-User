package com.drag.user.model;

import java.io.Serializable;

public class Cab implements Serializable {

    private String _id;
    private boolean isAvailable;
    private String tripId;
    private String city;
    private String pickup;
    private String drop;
    private String startTime;
    private String endTime;
    private String seats;
    private String fare;
    private String driverName;
    private String driverContact;
    private String carName;
    private String carNumber;

    public Cab(String city, String pickup, String drop, String startTime, String seats) {
        this.city = city;
        this.pickup = pickup;
        this.drop = drop;
        this.startTime = startTime;
        this.seats = seats;
    }

    public String get_id() {
        return _id;
    }

    public String getCity() {
        return city;
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

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getCarName() {
        return carName;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getFare() {
        return fare;
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

    public String getTripId() {
        return tripId;
    }
}