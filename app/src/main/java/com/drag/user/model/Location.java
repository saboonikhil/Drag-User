package com.drag.user.model;

import java.io.Serializable;

public class Location implements Serializable {

    private String city;
    private String[] setA;
    private String[] setB;

    public Location(String city, String[] setA, String[] setB) {
        this.city = city;
        this.setA = setA;
        this.setB = setB;
    }

    public String getCity() {
        return city;
    }

    public String[] getSetA() {
        return setA;
    }

    public String[] getSetB() {
        return setB;
    }
}

