package com.serinity.moodcontrol.controller.backoffice;

import com.serinity.moodcontrol.dao.backoffice.BackofficeJournalAdminDao;
import com.serinity.moodcontrol.dao.backoffice.BackofficeMoodAdminDao;
import com.serinity.moodcontrol.dto.backoffice.BackofficeJournalRow;
import com.serinity.moodcontrol.dto.backoffice.BackofficeMoodRow;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.*;

public class BackofficeAppController {

    // Mood
    @FXML private TextField tfMoodUser;
    @FXML private Button btnMoodRefresh;
    @FXML private Button btnMoodDeleteSelected;
    @FXML private Button btnMoodClearSelection;
    @FXML private VBox moodCardsBox;

    // Journal
    @FXML private TextField tfJournalUser;
    @FXML private Button btnJournalRefresh;
    @FXML private Button btnJournalDeleteSelected;
    @FXML private Button btnJournalClearSelection;
    @FXML private VBox journalCardsBox;

    @FXML private Label lblStatus;

    private final BackofficeMoodAdminDao moodDao = new BackofficeMoodAdminDao();
    private final BackofficeJournalAdminDao journalDao = new BackofficeJournalAdminDao();

    // selection state
    private final Set<Long> selectedMoodIds = new LinkedHashSet<>();
    private final Map<Long, Node> moodCardById = new HashMap<>();

    private final Set<Long> selectedJournalIds = new LinkedHashSet<>();
    private final Map<Long, Node> journalCardById = new HashMap<>();

    @FXML
    private void initialize() {
        btnMoodRefresh.setOnAction(e -> refreshMood());
        btnJournalRefresh.setOnAction(e -> refreshJournal());

        tfMoodUser.setOnAction(e -> refreshMood());
        tfJournalUser.setOnAction(e -> refreshJournal());

        btnMoodClearSelection.setOnAction(e -> clearMoodSelection());
        btnJournalClearSelection.setOnAction(e -> clearJournalSelection());

        btnMoodDeleteSelected.setOnAction(e -> deleteSelectedMoods());
        btnJournalDeleteSelected.setOnAction(e -> deleteSelectedJournals());

        refreshMood();
        refreshJournal();
        updateSelectionButtons();
    }

    private void refreshMood() {
        final String q = safe(tfMoodUser.getText());
        setStatus("Loading mood entries...");
        runAsync(() -> moodDao.findAll(q), rows -> {
            moodCardsBox.getChildren().clear();
            moodCardById.clear();
            // keep current selection only if ids still present; simplest: clear on refresh
            selectedMoodIds.clear();

            for (BackofficeMoodRow r : rows) {
                Node card = moodCard(r);
                moodCardsBox.getChildren().add(card);
                moodCardById.put(r.getId(), card);
            }
            setStatus("Mood loaded: " + rows.size());
            updateSelectionButtons();
        });
    }

    private void refreshJournal() {
        final String q = safe(tfJournalUser.getText());
        setStatus("Loading journal entries...");
        runAsync(() -> journalDao.findAll(q), rows -> {
            journalCardsBox.getChildren().clear();
            journalCardById.clear();
            selectedJournalIds.clear();

            for (BackofficeJournalRow r : rows) {
                Node card = journalCard(r);
                journalCardsBox.getChildren().add(card);
                journalCardById.put(r.getId(), card);
            }
            setStatus("Journal loaded: " + rows.size());
            updateSelectionButtons();
        });
    }

    private Node moodCard(BackofficeMoodRow r) {
        VBox card = new VBox(6);
        card.getStyleClass().add("backend-card");
        card.setUserData(r.getId());

        // click to toggle selection
        card.setOnMouseClicked(e -> toggleMoodSelection(r.getId(), card));

        HBox head = new HBox(10);

        Label who = new Label(r.getUserDisplay());
        who.getStyleClass().add("backend-card-title");

        Label meta = new Label("• " + r.getMomentType() + " • level " + r.getMoodLevel() + " • " + r.getEntryDateText());
        meta.getStyleClass().add("backend-card-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button del = new Button("Delete");
        del.getStyleClass().addAll("backend-danger", "backend-card-btn");
        del.setOnAction(e -> {
            e.consume();
            deleteSingleMood(r.getId(), card);
        });

        head.getChildren().addAll(who, meta, spacer, del);

        Text emo = new Text("Emotions: " + blankToDash(r.getEmotions()));
        emo.getStyleClass().add("backend-card-body");

        Text inf = new Text("Influences: " + blankToDash(r.getInfluences()));
        inf.getStyleClass().add("backend-card-body");

        Label id = new Label("mood_id=" + r.getId() + " • user_id=" + r.getUserId());
        id.getStyleClass().add("backend-card-foot");

        card.getChildren().addAll(head, emo, inf, id);
        return card;
    }

    private Node journalCard(BackofficeJournalRow r) {
        VBox card = new VBox(6);
        card.getStyleClass().add("backend-card");
        card.setUserData(r.getId());

        card.setOnMouseClicked(e -> toggleJournalSelection(r.getId(), card));

        HBox head = new HBox(10);

        Label who = new Label(r.getUserDisplay());
        who.getStyleClass().add("backend-card-title");

        String metaText = "• " + r.getCreatedAtText();
        if (r.getUpdatedAtText() != null && !r.getUpdatedAtText().isBlank()) {
            metaText += " • updated " + r.getUpdatedAtText();
        }
        Label meta = new Label(metaText);
        meta.getStyleClass().add("backend-card-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button del = new Button("Delete");
        del.getStyleClass().addAll("backend-danger", "backend-card-btn");
        del.setOnAction(e -> {
            e.consume();
            deleteSingleJournal(r.getId(), card);
        });

        head.getChildren().addAll(who, meta, spacer, del);

        Label title = new Label(r.getTitle());
        title.getStyleClass().add("backend-card-title2");
        title.setWrapText(true);

        Label id = new Label("journal_id=" + r.getId() + " • user_id=" + r.getUserId());
        id.getStyleClass().add("backend-card-foot");

        card.getChildren().addAll(head, title, id);
        return card;
    }

    private void toggleMoodSelection(long id, VBox card) {
        if (selectedMoodIds.contains(id)) {
            selectedMoodIds.remove(id);
            card.getStyleClass().remove("backend-card-selected");
        } else {
            selectedMoodIds.add(id);
            if (!card.getStyleClass().contains("backend-card-selected")) {
                card.getStyleClass().add("backend-card-selected");
            }
        }
        updateSelectionButtons();
    }

    private void toggleJournalSelection(long id, VBox card) {
        if (selectedJournalIds.contains(id)) {
            selectedJournalIds.remove(id);
            card.getStyleClass().remove("backend-card-selected");
        } else {
            selectedJournalIds.add(id);
            if (!card.getStyleClass().contains("backend-card-selected")) {
                card.getStyleClass().add("backend-card-selected");
            }
        }
        updateSelectionButtons();
    }

    private void clearMoodSelection() {
        for (Long id : new ArrayList<>(selectedMoodIds)) {
            Node card = moodCardById.get(id);
            if (card != null) card.getStyleClass().remove("backend-card-selected");
        }
        selectedMoodIds.clear();
        updateSelectionButtons();
    }

    private void clearJournalSelection() {
        for (Long id : new ArrayList<>(selectedJournalIds)) {
            Node card = journalCardById.get(id);
            if (card != null) card.getStyleClass().remove("backend-card-selected");
        }
        selectedJournalIds.clear();
        updateSelectionButtons();
    }

    private void deleteSingleMood(long id, Node cardNode) {
        if (!confirm("Delete mood entry " + id + "?")) return;
        setStatus("Deleting mood " + id + "...");
        runAsync(() -> moodDao.delete(id), ok -> {
            if (ok) {
                moodCardsBox.getChildren().remove(cardNode);
                moodCardById.remove(id);
                selectedMoodIds.remove(id);
                updateSelectionButtons();
                setStatus("Deleted mood " + id);
            } else setStatus("Nothing deleted.");
        });
    }

    private void deleteSingleJournal(long id, Node cardNode) {
        if (!confirm("Delete journal entry " + id + "?")) return;
        setStatus("Deleting journal " + id + "...");
        runAsync(() -> journalDao.delete(id), ok -> {
            if (ok) {
                journalCardsBox.getChildren().remove(cardNode);
                journalCardById.remove(id);
                selectedJournalIds.remove(id);
                updateSelectionButtons();
                setStatus("Deleted journal " + id);
            } else setStatus("Nothing deleted.");
        });
    }

    private void deleteSelectedMoods() {
        if (selectedMoodIds.isEmpty()) { setStatus("No mood cards selected."); return; }
        int n = selectedMoodIds.size();
        if (!confirm("Delete " + n + " selected mood entr" + (n == 1 ? "y" : "ies") + "?")) return;

        List<Long> ids = new ArrayList<>(selectedMoodIds);
        setStatus("Deleting " + ids.size() + " mood entries...");
        runAsync(() -> {
            int deleted = 0;
            for (Long id : ids) {
                if (moodDao.delete(id)) deleted++;
            }
            return deleted;
        }, deleted -> {
            for (Long id : ids) {
                Node card = moodCardById.get(id);
                if (card != null) moodCardsBox.getChildren().remove(card);
                moodCardById.remove(id);
            }
            selectedMoodIds.clear();
            updateSelectionButtons();
            setStatus("Deleted moods: " + deleted);
        });
    }

    private void deleteSelectedJournals() {
        if (selectedJournalIds.isEmpty()) { setStatus("No journal cards selected."); return; }
        int n = selectedJournalIds.size();
        if (!confirm("Delete " + n + " selected journal entr" + (n == 1 ? "y" : "ies") + "?")) return;

        List<Long> ids = new ArrayList<>(selectedJournalIds);
        setStatus("Deleting " + ids.size() + " journal entries...");
        runAsync(() -> {
            int deleted = 0;
            for (Long id : ids) {
                if (journalDao.delete(id)) deleted++;
            }
            return deleted;
        }, deleted -> {
            for (Long id : ids) {
                Node card = journalCardById.get(id);
                if (card != null) journalCardsBox.getChildren().remove(card);
                journalCardById.remove(id);
            }
            selectedJournalIds.clear();
            updateSelectionButtons();
            setStatus("Deleted journals: " + deleted);
        });
    }

    private void updateSelectionButtons() {
        if (btnMoodDeleteSelected != null) {
            btnMoodDeleteSelected.setText("Delete Selected (" + selectedMoodIds.size() + ")");
            btnMoodDeleteSelected.setDisable(selectedMoodIds.isEmpty());
        }
        if (btnJournalDeleteSelected != null) {
            btnJournalDeleteSelected.setText("Delete Selected (" + selectedJournalIds.size() + ")");
            btnJournalDeleteSelected.setDisable(selectedJournalIds.isEmpty());
        }
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.CANCEL, ButtonType.OK);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String blankToDash(String s) { return (s == null || s.isBlank()) ? "—" : s; }

    private String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private <T> void runAsync(SqlSupplier<T> work, UiConsumer<T> ui) {
        Task<T> task = new Task<T>() {
            @Override protected T call() throws Exception { return work.get(); }
        };
        task.setOnSucceeded(e -> ui.accept(task.getValue()));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            setStatus("Error: " + (ex == null ? "unknown" : ex.getMessage()));
            if (ex != null) ex.printStackTrace();
        });
        Thread th = new Thread(task, "backend-db-task");
        th.setDaemon(true);
        th.start();
    }

    @FunctionalInterface private interface SqlSupplier<T> { T get() throws SQLException; }
    @FunctionalInterface private interface UiConsumer<T> { void accept(T value); }
}