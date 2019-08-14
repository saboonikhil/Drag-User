package com.drag.user.model;

import com.drag.user.network.LoginResponse;

import java.io.Serializable;

public class User implements Serializable {

    private String _id;
    private String name;
    private String email;
    private String contact;
    private String alternateContact;

    private Boolean res;
    private String response;
    private LoginResponse token;

    public User(String name, String email, String contact, String alternateContact) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.alternateContact = alternateContact;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAlternateContact() {
        return alternateContact;
    }

    public void setAlternateContact(String alternateContact) {
        this.alternateContact = alternateContact;
    }

    public Boolean res() {
        return res;
    }

    public String response() {
        return response;
    }

    public LoginResponse token() {
        return token;
    }
}