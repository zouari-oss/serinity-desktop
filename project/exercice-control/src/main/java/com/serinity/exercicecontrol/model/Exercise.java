package com.serinity.exercicecontrol.model;

public class Exercise {
    private int id;
    private String title;
    private String type;
    private int level;
    private int durationMinutes;
    private String description;
    private String benefits;
    private String tips;
    private String theme;
    private String guidedInstructions;
    private boolean isActive = true;

    public Exercise() {}

    public Exercise(int id, String title, String type, int level, int durationMinutes, String description,
                    String benefits, String tips, String theme, String guidedInstructions, boolean isActive) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.benefits = benefits;
        this.tips = tips;
        this.theme = theme;
        this.guidedInstructions = guidedInstructions;
        this.isActive = isActive;
    }

    public Exercise(String title, String type, int level, int durationMinutes, String description,
                    String benefits, String tips, String theme, String guidedInstructions, boolean isActive) {
        this.title = title;
        this.type = type;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.benefits = benefits;
        this.tips = tips;
        this.theme = theme;
        this.guidedInstructions = guidedInstructions;
        this.isActive = isActive;
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

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getGuidedInstructions() { return guidedInstructions; }
    public void setGuidedInstructions(String guidedInstructions) { this.guidedInstructions = guidedInstructions; }

    public boolean isActive() { return isActive; }
    public void setIsActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", durationMinutes=" + durationMinutes +
                ", isActive=" + isActive +
                '}';
    }
}
