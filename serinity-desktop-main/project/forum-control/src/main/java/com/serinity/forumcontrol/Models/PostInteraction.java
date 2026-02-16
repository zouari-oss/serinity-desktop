package com.serinity.forumcontrol.Models;

public class PostInteraction {
    private int id;
    private int threadId;
    private String userId;
    private boolean follow;
    private int vote;

    public PostInteraction() {
    }

    public PostInteraction(int threadId, String userId) {
        this.threadId = threadId;
        this.userId = userId;
        this.follow = false;
        this.vote = 0;
    }

    public PostInteraction(int threadId, String userId, boolean follow, int vote) {
        this.threadId = threadId;
        this.userId = userId;
        this.follow = follow;
        this.vote = vote;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public boolean hasUpvoted() {
        return vote == 1;
    }

    public boolean hasDownvoted() {
        return vote == -1;
    }

    public boolean hasNoVote() {
        return vote == 0;
    }

    public boolean isEmpty() {
        return !follow && vote == 0;
    }

    @Override
    public String toString() {
        return "PostInteraction{" +
                "id=" + id +
                ", threadId=" + threadId +
                ", userId='" + userId + '\'' +
                ", follow=" + follow +
                ", vote=" + vote +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PostInteraction that = (PostInteraction) o;

        if (threadId != that.threadId) return false;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        int result = threadId;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}