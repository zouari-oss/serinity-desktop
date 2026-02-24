package com.serinity.forumcontrol.Models;

import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Models.ThreadType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Thread {
    private long id;
    private long categoryId;
    private String userId;
    private String title;
    private String content;
    private ThreadType type;
    private ThreadStatus status;
    private boolean isPinned;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int likecount;
    private int dislikecount;
    private int followcount;
    private int repliescount;
    private String imageUrl;



    // Associations
    private PostInteraction interactions;
    private Category category;
    private List<Reply> replies;

    // Constructors
    public Thread() {
        this.status = ThreadStatus.OPEN;
        this.isPinned = false;
        this.replies = new ArrayList<>();
    }

    public Thread(long categoryId, String userId, String title, String content) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = ThreadType.DISCUSSION;
        this.status = ThreadStatus.OPEN;
        this.isPinned = false;
        this.replies = new ArrayList<>();
    }

    public Thread(long categoryId, String userId, String title, String content, ThreadType type, ThreadStatus status, boolean isPinned) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.status = status;
        this.isPinned = isPinned;
        this.replies = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ThreadType getType() {
        return type;
    }

    public void setType(ThreadType type) {
        this.type = type;
    }

    public ThreadStatus getStatus() {
        return status;
    }

    public void setStatus(ThreadStatus status) {
        this.status = status;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public void addReply(Reply reply) {
        if (!this.replies.contains(reply)) {
            this.replies.add(reply);
            reply.setThread(this);
        }
    }

    public void removeReply(Reply reply) {
        if (this.replies.contains(reply)) {
            this.replies.remove(reply);
            reply.setThread(null);
        }
    }

    // Helper methods

    public String getCategoryName() {
        return category != null ? category.getName() : "Unknown";
    }

    public int getReplyCount() {
        return replies.size();
    }

    @Override
    public String toString() {
        return "Thread{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", isPinned=" + isPinned +
                ", replyCount=" + replies.size() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Thread thread = (Thread) o;

        return id == thread.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public int getDislikecount() {
        return dislikecount;
    }

    public void setDislikecount(int dislikecount) {
        this.dislikecount = dislikecount;
    }

    public int getLikecount() {
        return likecount;
    }

    public void setLikecount(int likecount) {
        this.likecount = likecount;
    }

    public int getFollowcount() {
        return followcount;
    }

    public void setFollowcount(int followcount) {
        this.followcount = followcount;
    }

    public int getRepliescount() {
        return repliescount;
    }

    public void setRepliescount(int repliescount) {
        this.repliescount = repliescount;
    }
}