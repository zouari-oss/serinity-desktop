package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dao.MoodEntryDao;
import com.serinity.moodcontrol.dao.MoodHistoryItem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.Normalizer;

public class MoodHistoryController {

    // ---- Card VM UI-shaped data ----
    static class MoodCardVM { // package-private so MoodHistoryCardController can use it
        final long id;
        final LocalDateTime dateTime;
        final String momentType; // "MOMENT"/"DAY" (FILTER ONLY, NOT SEARCHED)
        final int moodLevel;
        final List<String> emotions;   // CODES
        final List<String> influences; // CODES

        MoodCardVM(final long id,
                   final LocalDateTime dateTime,
                   final String momentType,
                   final int moodLevel,
                   final List<String> emotions,
                   final List<String> influences) {
            this.id = id;
            this.dateTime = dateTime;
            this.momentType = momentType;
            this.moodLevel = moodLevel;
            this.emotions = emotions == null ? new ArrayList<String>() : emotions;
            this.influences = influences == null ? new ArrayList<String>() : influences;
        }
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox timelineBox;
    @FXML private ComboBox<String> rangeBox;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField searchField; // <-- add in FXML next to typeBox
    @FXML private ResourceBundle resources;

    // Host injected by MoodHomeController
    private StackPane moodHost;

    // Master cache loaded once
    private List<MoodCardVM> masterItems = new ArrayList<MoodCardVM>();

    // Current view after filters
    private List<MoodCardVM> currentItems = new ArrayList<MoodCardVM>();

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException(
                    "ResourceBundle not injected in MoodHistoryController. Load MoodHistory.fxml with bundle.");
        }

        // Range options (localized labels)
        rangeBox.getItems().setAll(
                t("history.range.all"),
                t("history.range.last30"),
                t("history.range.last7"));
        rangeBox.getSelectionModel().select(0);

        // Type options (localized labels)
        typeBox.getItems().setAll(
                t("history.type.all"),
                t("history.type.moment"),
                t("history.type.day"));
        typeBox.getSelectionModel().select(0);

        // Filters only re-render locally (NO DB call)
        rangeBox.setOnAction(e -> applyFiltersAndRender());
        typeBox.setOnAction(e -> applyFiltersAndRender());

        // Search (local, no SQL)
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndRender());
        }

        // One DB call on page load
        reloadMasterFromDb();
        applyFiltersAndRender();
    }

    public void setMoodHost(final StackPane moodHost) {
        this.moodHost = moodHost;
    }

    @FXML
    private void onRefresh() {
        // DB call + local filtering
        reloadMasterFromDb();
        applyFiltersAndRender();
    }

    @FXML
    private void onLogNew() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/Wizard.fxml"), resources);
            final Parent wizard = loader.load();

            final StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost);

            moodHost.getChildren().setAll(wizard);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- DB LOAD (MASTER) ----------------

    private void reloadMasterFromDb() {
        try {
            final long userId = 1L; // TEMP until user integration

            // Load everything
            final List<MoodHistoryItem> data = new MoodEntryDao().findHistory(userId, null, "ALL");

            final List<MoodCardVM> vms = new ArrayList<MoodCardVM>();
            for (final MoodHistoryItem it : data) {
                vms.add(new MoodCardVM(
                        it.getId(),
                        it.getDateTime(),
                        it.getMomentType(),
                        it.getMoodLevel(),
                        it.getEmotions(),    // CODES
                        it.getInfluences()   // CODES
                ));
            }

            masterItems = vms;

        } catch (final Exception e) {
            e.printStackTrace();
            masterItems = new ArrayList<MoodCardVM>();
        }
    }

    // ---------------- LOCAL FILTERING (+ SEARCH) ----------------

    private void applyFiltersAndRender() {
        final String rangeLabel = rangeBox.getValue();
        final String typeCode = toTypeCode(typeBox.getValue()); // ALL/MOMENT/DAY

        Integer lastDays = null;
        if (t("history.range.last7").equals(rangeLabel)) lastDays = Integer.valueOf(7);
        else if (t("history.range.last30").equals(rangeLabel)) lastDays = Integer.valueOf(30);

        final LocalDateTime cutoff =
                (lastDays == null) ? null : LocalDate.now().minusDays(lastDays.intValue()).atStartOfDay();

        final String q = (searchField == null) ? "" : normalize(searchField.getText());
        final boolean hasQuery = q != null && q.trim().length() > 0;

        final List<MoodCardVM> filtered = new ArrayList<MoodCardVM>();
        for (final MoodCardVM m : masterItems) {

            // 1) range filter
            if (cutoff != null && m.dateTime.isBefore(cutoff)) continue;

            // 2) type filter (momentType)
            if (!"ALL".equalsIgnoreCase(typeCode)) {
                final String mt = (m.momentType == null) ? "" : m.momentType.trim().toUpperCase(Locale.ROOT);
                if (!typeCode.equals(mt)) continue;
            }

            // 3) search filter (NOT searching type/momentType)
            if (hasQuery) {
                final String hay = buildSearchText(m);
                if (!hay.contains(q)) continue;
            }

            filtered.add(m);
        }

        currentItems = filtered;
        renderTimeline(currentItems);
    }

    /**
     * Build a search string that includes:
     * - mood level LABEL (localized)
     * - emotion labels (localized)
     * - influence labels (localized)
     * - date/time text
     *
     * DOES NOT include momentType (because you said: not Type / not moment type).
     */
    private String buildSearchText(final MoodCardVM m) {
        final StringBuilder sb = new StringBuilder(160);

        // date/time
        if (m.dateTime != null) {
            sb.append(DATE_FMT.format(m.dateTime)).append(' ');
            sb.append(TIME_FMT.format(m.dateTime)).append(' ');
        }

        // mood label by level
        sb.append(t("mood.level." + m.moodLevel)).append(' ');

        // emotions labels
        for (final String code : m.emotions) {
            if (code == null) continue;
            sb.append(t("emotion." + code)).append(' ');
        }

        // influences labels
        for (final String code : m.influences) {
            if (code == null) continue;
            sb.append(t("influence." + code)).append(' ');
        }

        return normalize(sb.toString());
    }

    private String normalize(final String s) {
        if (s == null) return "";
        String x = s.toLowerCase(Locale.ROOT).trim();
        // remove accents to make search forgiving (é == e)
        x = Normalizer.normalize(x, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        // collapse spaces
        x = x.replaceAll("\\s+", " ");
        return x;
    }

    // ---------------- RENDERING ----------------

    private void renderTimeline(final List<MoodCardVM> items) {
        timelineBox.getChildren().clear();

        final List<MoodCardVM> sorted = new ArrayList<MoodCardVM>(items);
        sorted.sort(new Comparator<MoodCardVM>() {
            @Override
            public int compare(final MoodCardVM a, final MoodCardVM b) {
                return b.dateTime.compareTo(a.dateTime);
            }
        });

        final Map<LocalDate, List<MoodCardVM>> grouped = new LinkedHashMap<LocalDate, List<MoodCardVM>>();
        for (final MoodCardVM m : sorted) {
            final LocalDate d = m.dateTime.toLocalDate();
            if (!grouped.containsKey(d)) grouped.put(d, new ArrayList<MoodCardVM>());
            grouped.get(d).add(m);
        }

        for (final Map.Entry<LocalDate, List<MoodCardVM>> entry : grouped.entrySet()) {
            final LocalDate date = entry.getKey();
            timelineBox.getChildren().add(dateHeader(date));

            for (final MoodCardVM m : entry.getValue()) {
                timelineBox.getChildren().add(moodCard(m)); // FXML component
            }
        }
    }

    private Node dateHeader(final LocalDate date) {
        String title;
        final LocalDate today = LocalDate.now();

        if (date.equals(today)) title = t("history.today");
        else if (date.equals(today.minusDays(1))) title = t("history.yesterday");
        else title = date.toString();

        final Label lbl = new Label(title);
        lbl.getStyleClass().add("history-date");
        return lbl;
    }

    private Node moodCard(final MoodCardVM m) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/components/MoodHistoryCard.fxml"),
                    resources
            );
            final Node card = loader.load();

            final MoodHistoryCardController c = loader.getController();
            c.setData(
                    m,
                    resources,
                    vm -> onEdit(vm),
                    vm -> onDelete(vm)
            );

            return card;

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load MoodHistoryCard.fxml", e);
        }
    }

    // ---------------- Actions (Edit / Delete) ----------------

    private void onEdit(final MoodCardVM m) {
        try {
            final long userId = 1L;

            final MoodEntryDao dao = new MoodEntryDao();
            final MoodHistoryItem it = dao.findById(m.id, userId);
            if (it == null) return;

            final com.serinity.moodcontrol.model.MoodEntry entry = new com.serinity.moodcontrol.model.MoodEntry();
            entry.setId(it.getId());
            entry.setUserId(userId);
            entry.setMomentType(it.getMomentType());
            entry.setMoodLevel(it.getMoodLevel());
            entry.setEmotions(new ArrayList<String>(it.getEmotions()));      // CODES
            entry.setInfluences(new ArrayList<String>(it.getInfluences())); // CODES

            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/Wizard.fxml"), resources);
            final Parent wizard = loader.load();

            final StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost);
            wiz.startEdit(entry);

            wiz.setOnFinish(new Runnable() {
                @Override
                public void run() {
                    reloadMasterFromDb();
                    applyFiltersAndRender();
                }
            });

            moodHost.getChildren().setAll(wizard);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void onDelete(final MoodCardVM m) {
        final javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);

        alert.setTitle(t("history.delete.title"));
        alert.setHeaderText(t("history.delete.header"));
        alert.setContentText(t("history.delete.body"));

        final Optional<javafx.scene.control.ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {
            try {
                final long userId = 1L;
                final boolean ok = new MoodEntryDao().delete(m.id, userId);

                if (ok) {
                    reloadMasterFromDb();
                    applyFiltersAndRender();
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ---------------- Helpers ----------------

    private String t(final String key) {
        try {
            return resources.getString(key);
        } catch (final MissingResourceException e) {
            return key;
        }
    }

    private String toTypeCode(final String label) {
        if (label == null) return "ALL";
        if (label.equals(t("history.type.moment"))) return "MOMENT";
        if (label.equals(t("history.type.day"))) return "DAY";
        return "ALL";
    }
}
