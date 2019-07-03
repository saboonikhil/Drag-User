package com.drag.user.network;

import com.drag.user.model.User;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private String token;
    private String expires;
    private User dbObj;

    public String token() {
        return token;
    }

    public String expires() {
        return expires;
    }

    public User user() {
        return dbObj;
    }
}