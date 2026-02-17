package com.serinity.exercicecontrol.model;

public class Resource {
    private int id;
    private String title;
    private String mediaType;
    private String url;
    private String content;
    private int durationSeconds;
    private int exerciseId;

    public Resource() {}

    public Resource(int id, String title, String mediaType, String url, String content,
                    int durationSeconds, int exerciseId) {
        this.id = id;
        this.title = title;
        this.mediaType = mediaType;
        this.url = url;
        this.content = content;
        this.durationSeconds = durationSeconds;
        this.exerciseId = exerciseId;
    }

    public Resource(String title, String mediaType, String url, String content,
                    int durationSeconds, int exerciseId) {
        this.title = title;
        this.mediaType = mediaType;
        this.url = url;
        this.content = content;
        this.durationSeconds = durationSeconds;
        this.exerciseId = exerciseId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", exerciseId=" + exerciseId +
                '}';
    }
}
