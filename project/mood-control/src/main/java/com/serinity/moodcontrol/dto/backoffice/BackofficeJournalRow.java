package com.serinity.moodcontrol.dto.backoffice;

import javafx.beans.property.*;

public class BackofficeJournalRow {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty userId = new SimpleStringProperty();        // UUID
    private final StringProperty userDisplay = new SimpleStringProperty();   // Name/@username
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty createdAtText = new SimpleStringProperty();
    private final StringProperty updatedAtText = new SimpleStringProperty();

    public BackofficeJournalRow() {}

    public BackofficeJournalRow(long id,
                                String userId,
                                String userDisplay,
                                String title,
                                String createdAtText,
                                String updatedAtText) {
        this.id.set(id);
        this.userId.set(userId);
        this.userDisplay.set(userDisplay);
        this.title.set(title);
        this.createdAtText.set(createdAtText);
        this.updatedAtText.set(updatedAtText);
    }

    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }

    public String getUserId() { return userId.get(); }
    public StringProperty userIdProperty() { return userId; }

    public String getUserDisplay() { return userDisplay.get(); }
    public StringProperty userDisplayProperty() { return userDisplay; }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }

    public String getCreatedAtText() { return createdAtText.get(); }
    public StringProperty createdAtTextProperty() { return createdAtText; }

    public String getUpdatedAtText() { return updatedAtText.get(); }
    public StringProperty updatedAtTextProperty() { return updatedAtText; }
}