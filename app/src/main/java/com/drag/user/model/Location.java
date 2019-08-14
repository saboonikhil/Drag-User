package com.drag.user.model;

import java.io.Serializable;

public class Location implements Serializable {

    private String city;

    public Location(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}

