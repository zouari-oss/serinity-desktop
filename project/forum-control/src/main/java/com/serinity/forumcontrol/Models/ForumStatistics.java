package com.serinity.forumcontrol.Models;

/**
 * Model for forum statistics
 */
public class ForumStatistics {

    // Thread statistics
    private int totalThreads;
    private int openThreads;
    private int lockedThreads;
    private int archivedThreads;
    private int discussionThreads;
    private int questionThreads;
    private int announcementThreads;

    // User activity statistics
    private int totalReplies;
    private int totalUsers;
    private int activeUsersToday;
    private int activeUsersThisWeek;

    // Interaction statistics
    private int totalLikes;
    private int totalDislikes;
    private int totalFollows;

    // Category statistics
    private int totalCategories;

    // Constructors
    public ForumStatistics() {
    }

    // Getters and Setters
    public int getTotalThreads() {
        return totalThreads;
    }

    public void setTotalThreads(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public int getOpenThreads() {
        return openThreads;
    }

    public void setOpenThreads(int openThreads) {
        this.openThreads = openThreads;
    }

    public int getLockedThreads() {
        return lockedThreads;
    }

    public void setLockedThreads(int lockedThreads) {
        this.lockedThreads = lockedThreads;
    }

    public int getArchivedThreads() {
        return archivedThreads;
    }

    public void setArchivedThreads(int archivedThreads) {
        this.archivedThreads = archivedThreads;
    }

    public int getDiscussionThreads() {
        return discussionThreads;
    }

    public void setDiscussionThreads(int discussionThreads) {
        this.discussionThreads = discussionThreads;
    }

    public int getQuestionThreads() {
        return questionThreads;
    }

    public void setQuestionThreads(int questionThreads) {
        this.questionThreads = questionThreads;
    }

    public int getAnnouncementThreads() {
        return announcementThreads;
    }

    public void setAnnouncementThreads(int announcementThreads) {
        this.announcementThreads = announcementThreads;
    }

    public int getTotalReplies() {
        return totalReplies;
    }

    public void setTotalReplies(int totalReplies) {
        this.totalReplies = totalReplies;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getActiveUsersToday() {
        return activeUsersToday;
    }

    public void setActiveUsersToday(int activeUsersToday) {
        this.activeUsersToday = activeUsersToday;
    }

    public int getActiveUsersThisWeek() {
        return activeUsersThisWeek;
    }

    public void setActiveUsersThisWeek(int activeUsersThisWeek) {
        this.activeUsersThisWeek = activeUsersThisWeek;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public int getTotalDislikes() {
        return totalDislikes;
    }

    public void setTotalDislikes(int totalDislikes) {
        this.totalDislikes = totalDislikes;
    }

    public int getTotalFollows() {
        return totalFollows;
    }

    public void setTotalFollows(int totalFollows) {
        this.totalFollows = totalFollows;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }

    @Override
    public String toString() {
        return "ForumStatistics{" +
                "totalThreads=" + totalThreads +
                ", openThreads=" + openThreads +
                ", lockedThreads=" + lockedThreads +
                ", totalReplies=" + totalReplies +
                ", totalUsers=" + totalUsers +
                '}';
    }
}