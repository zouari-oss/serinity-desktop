package com.serinity.forumcontrol.Models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Reply {
    private long id;
    private long threadId;
    private String userId;
    private Long parentId;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;


    private Thread thread;
    private Reply parent;
    private List<Reply> children;

    public Reply() {
        this.children = new ArrayList<>();
    }

    public Reply(long threadId, String userId, String content) {
        this.threadId = threadId;
        this.userId = userId;
        this.content = content;
        this.parentId = null;
        this.children = new ArrayList<>();
    }

    public Reply(long threadId, String userId, Long parentId, String content) {
        this.threadId = threadId;
        this.userId = userId;
        this.parentId = parentId;
        this.content = content;
        this.children = new ArrayList<>();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }


    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Reply getParent() {
        return parent;
    }

    public void setParent(Reply parent) {
        this.parent = parent;
    }

    public List<Reply> getChildren() {
        return children;
    }

    public void setChildren(List<Reply> children) {
        this.children = children;
    }

    // Association helper methods
    public void addChild(Reply child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    public void removeChild(Reply child) {
        if (this.children.contains(child)) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    // Helper methods
    public boolean isTopLevel() {
        return parentId == null;
    }

    public boolean isNested() {
        return parentId != null;
    }

    public String getThreadTitle() {
        return thread != null ? thread.getTitle() : "Unknown";
    }

    public int getChildCount() {
        return children.size();
    }

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", threadId=" + threadId +
                ", userId='" + userId + '\'' +
                ", parentId=" + parentId +
                ", content='" + content + '\'' +
                ", childCount=" + children.size() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reply reply = (Reply) o;

        return id == reply.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}