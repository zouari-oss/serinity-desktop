package com.serinity.forumcontrol.Models;

import java.sql.Timestamp;

/**
 * Notification model for user notifications
 */
public class Notification {

    private Long id;
    private Long threadId;
    private String type;  // "follow", "like", "dislike", "comment"
    private String content;
    private boolean seen;
    private Timestamp date;
    private String userId;

    // Constructors
    public Notification() {
    }

    public Notification(Long threadId, String type, String content, String userId) {
        this.threadId = threadId;
        this.type = type;
        this.content = content;
        this.userId = userId;
        this.seen = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", threadId=" + threadId +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", seen=" + seen +
                ", date=" + date +
                ", userId='" + userId + '\'' +
                '}';
    }
}