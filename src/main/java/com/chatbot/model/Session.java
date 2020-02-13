package com.chatbot.model;



import javax.persistence.*;
import java.util.UUID;

@Entity
public class Session {

    private String projectName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String sessionID;

    private Integer fallbackCount;

    @OneToOne(cascade = {CascadeType.ALL})
    private User user;

    public String getProjectName() {
        return projectName;
    }

    public Integer getFallbackCount() {
        return fallbackCount;
    }

    public void setFallbackCount(Integer fallbackCount) {
        this.fallbackCount = fallbackCount;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
