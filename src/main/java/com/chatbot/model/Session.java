package com.chatbot.model;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class Session extends BaseEntity {

    private String projectName;
    private UUID sessionID;
    private User user;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public void setSessionID(UUID sessionID) {
        this.sessionID = sessionID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
