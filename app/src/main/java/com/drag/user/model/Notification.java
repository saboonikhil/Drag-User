package com.drag.user.model;

import java.io.Serializable;

public class Notification implements Serializable {

    private String type;
    private String subject;
    private String body;
    private String updatedAt;

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

