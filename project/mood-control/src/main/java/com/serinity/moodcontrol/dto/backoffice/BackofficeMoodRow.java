package com.serinity.moodcontrol.dto.backoffice;

import javafx.beans.property.*;

public class BackofficeMoodRow {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty userId = new SimpleStringProperty();        // UUID
    private final StringProperty userDisplay = new SimpleStringProperty();   // Name/@username
    private final StringProperty momentType = new SimpleStringProperty();
    private final IntegerProperty moodLevel = new SimpleIntegerProperty();
    private final StringProperty entryDateText = new SimpleStringProperty();
    private final StringProperty emotions = new SimpleStringProperty();
    private final StringProperty influences = new SimpleStringProperty();

    public BackofficeMoodRow() {}

    public BackofficeMoodRow(long id,
                             String userId,
                             String userDisplay,
                             String momentType,
                             int moodLevel,
                             String entryDateText,
                             String emotions,
                             String influences) {
        this.id.set(id);
        this.userId.set(userId);
        this.userDisplay.set(userDisplay);
        this.momentType.set(momentType);
        this.moodLevel.set(moodLevel);
        this.entryDateText.set(entryDateText);
        this.emotions.set(emotions);
        this.influences.set(influences);
    }

    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }

    public String getUserId() { return userId.get(); }
    public StringProperty userIdProperty() { return userId; }

    public String getUserDisplay() { return userDisplay.get(); }
    public StringProperty userDisplayProperty() { return userDisplay; }

    public String getMomentType() { return momentType.get(); }
    public StringProperty momentTypeProperty() { return momentType; }

    public int getMoodLevel() { return moodLevel.get(); }
    public IntegerProperty moodLevelProperty() { return moodLevel; }

    public String getEntryDateText() { return entryDateText.get(); }
    public StringProperty entryDateTextProperty() { return entryDateText; }

    public String getEmotions() { return emotions.get(); }
    public StringProperty emotionsProperty() { return emotions; }

    public String getInfluences() { return influences.get(); }
    public StringProperty influencesProperty() { return influences; }
}