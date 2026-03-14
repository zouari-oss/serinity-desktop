package com.serinity.exercicecontrol.model;

public class Exercise {
    private int id;
    private String title;
    private String type;
    private int level;
    private int durationMinutes;
    private String description;

    public Exercise() {}

    public Exercise(int id, String title, String type, int level, int durationMinutes, String description) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.description = description;
    }

    public Exercise(String title, String type, int level, int durationMinutes, String description) {
        this.title = title;
        this.type = type;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", durationMinutes=" + durationMinutes +
                '}';
    }
}
