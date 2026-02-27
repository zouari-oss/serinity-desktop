package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dto.ImpactReport;
import com.serinity.moodcontrol.dto.ImpactRow;
import com.serinity.moodcontrol.service.MoodAnalyticsService;
import com.serinity.moodcontrol.service.QuoteService;
import com.serinity.moodcontrol.service.QuoteService.QuoteResult;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MoodHomeController {

    // TEMP user id until access-control integration
    private static final String USER_ID = "6affa2df-dda9-442d-99ee-d2a3c1e78c64";

    @FXML private StackPane moodHost;

    @FXML private StackPane cardLogMood;
    @FXML private StackPane cardMoodHistory;
    @FXML private StackPane cardJournal;

    // Insights UI
    @FXML private StackPane insightsOverlay;
    @FXML private VBox insightsSheet;

    @FXML private Label insightsStatusLabel;
    @FXML private ListView<ImpactRow> influenceImpactList;
    @FXML private ListView<ImpactRow> emotionImpactList;

    // Daily Zen UI
    @FXML private Label zenQuoteText;
    @FXML private Label zenQuoteAuthor;
    @FXML private Button btnNewQuote;

    @FXML private ResourceBundle resources;

    private final QuoteService quoteService = new QuoteService();
    private final MoodAnalyticsService analyticsService = new MoodAnalyticsService();

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected for MoodHome. Load MoodHome.fxml with a bundle.");
        }

        // Existing navigation
        cardLogMood.setOnMouseClicked(e -> loadWizard());
        cardMoodHistory.setOnMouseClicked(e -> loadHistory());
        cardJournal.setOnMouseClicked(e -> loadJournal());

        // Daily Zen actions
        if (btnNewQuote != null) {
            btnNewQuote.setOnAction(e -> loadZenQuote());
        }

        // Setup Insights lists
        setupInsightList(influenceImpactList);
        setupInsightList(emotionImpactList);

        // Make sure sheet starts closed (in case FXML defaults change)
        setInsightsOpen(false);

        // Close sheet on ESC
        moodHost.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                setInsightsOpen(false);
                e.consume();
            }
        });

        // Load on open
        loadZenQuote();
        loadInsightsAsync();
    }

    // Insights

    @FXML
    private void onOpenInsights(ActionEvent event) {
        setInsightsOpen(true);
    }

    @FXML
    private void onCloseInsights(ActionEvent event) {
        setInsightsOpen(false);
    }

    @FXML
    private void onCloseInsights() {
        // for onMouseClicked in FXML overlay
        setInsightsOpen(false);
    }

    private void setInsightsOpen(boolean open) {
        if (insightsOverlay != null) {
            insightsOverlay.setVisible(open);
            insightsOverlay.setManaged(open);
        }
        if (insightsSheet != null) {
            insightsSheet.setVisible(open);
            insightsSheet.setManaged(open);
        }
    }

    // Insights Data

    @FXML
    private void onRefreshInsights(ActionEvent event) {
        loadInsightsAsync();
    }

    private void setupInsightList(ListView<ImpactRow> lv) {
        if (lv == null) return;

        lv.setFocusTraversable(false);

        lv.setCellFactory(list -> new ListCell<ImpactRow>() {
            private final HBox root = new HBox();
            private final Label name = new Label();
            private final Region spacer = new Region();

            private final Label count = new Label();
            private final Label avg = new Label();
            private final Label low = new Label();

            {
                root.getStyleClass().add("insight-row");

                name.getStyleClass().add("name");

                // Reuse same numeric styling as your header row for alignment
                count.getStyleClass().add("insight-h-num");
                avg.getStyleClass().add("insight-h-num");
                low.getStyleClass().add("insight-h-num");

                HBox.setHgrow(spacer, Priority.ALWAYS);

                // order matches header
                root.getChildren().addAll(name, spacer, count, avg, low);
            }

            @Override
            protected void updateItem(ImpactRow item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                name.setText(item.getLabel());

                // # column
                count.setText(String.valueOf(item.getTotalSamples()));

                // Avg column
                avg.setText(format2(item.getAvgMood()));

                // Low column
                low.setText(item.getLowMoodCount() + "/" + item.getTotalSamples());

                setText(null);
                setGraphic(root);
            }
        });
    }

    private void loadInsightsAsync() {
        if (insightsStatusLabel != null) {
            insightsStatusLabel.setText(i18nOr("mood.insights.loading", "Loading insights..."));
        }
        if (influenceImpactList != null) influenceImpactList.getItems().clear();
        if (emotionImpactList != null) emotionImpactList.getItems().clear();

        Task<ImpactReport> task = new Task<ImpactReport>() {
            @Override
            protected ImpactReport call() throws SQLException {
                return analyticsService.getImpactReport(USER_ID, Integer.valueOf(30), "ALL");
            }
        };

        task.setOnSucceeded(e -> {
            ImpactReport report = task.getValue();

            if (influenceImpactList != null) {
                List<ImpactRow> infl = report.getInfluences();
                influenceImpactList.getItems().setAll(infl);
            }
            if (emotionImpactList != null) {
                List<ImpactRow> emo = report.getEmotions();
                emotionImpactList.getItems().setAll(emo);
            }
            if (insightsStatusLabel != null) {
                insightsStatusLabel.setText(i18nOr("mood.insights.ready", "Insights updated."));
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (insightsStatusLabel != null) {
                insightsStatusLabel.setText(i18nOr("mood.insights.error", "Could not load insights."));
            }
        });

        Thread t = new Thread(task, "mood-insights-loader");
        t.setDaemon(true);
        t.start();
    }

    private String format2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    // Daily Zen

    private void loadZenQuote() {
        setQuoteLoadingState(true);

        quoteService.fetchFormattedQuote().thenAccept(result ->
                Platform.runLater(() -> applyQuoteResult(result))
        );
    }

    private void applyQuoteResult(QuoteResult result) {
        setQuoteLoadingState(false);

        if (result != null && result.isSuccess()) {
            zenQuoteText.setText(result.getQuote());

            String author = result.getAuthor();
            if (author != null && !author.isBlank()) {
                zenQuoteAuthor.setText("— " + author);
            } else {
                zenQuoteAuthor.setText("");
            }
            return;
        }

        zenQuoteText.setText(i18nOr("mood.zen.error", "Couldn’t load a quote right now."));
        zenQuoteAuthor.setText("");
    }

    private void setQuoteLoadingState(boolean loading) {
        if (zenQuoteText != null) {
            zenQuoteText.setText(loading
                    ? i18nOr("mood.zen.loading", "Loading…")
                    : zenQuoteText.getText());
        }
        if (zenQuoteAuthor != null && loading) {
            zenQuoteAuthor.setText("");
        }
        if (btnNewQuote != null) {
            btnNewQuote.setDisable(loading);
        }
    }

    private String i18nOr(String key, String fallback) {
        try {
            return resources != null ? resources.getString(key) : fallback;
        } catch (MissingResourceException e) {
            return fallback;
        }
    }

    // Navigation

    private void loadWizard() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/Wizard.fxml"),
                    resources);
            final Parent view = loader.load();

            final StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost);

            moodHost.getChildren().setAll(view);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load /fxml/mood/Wizard.fxml", e);
        }
    }

    private void loadHistory() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/MoodHistory.fxml"),
                    resources);
            final Parent view = loader.load();

            final MoodHistoryController c = loader.getController();
            c.setMoodHost(moodHost);

            moodHost.getChildren().setAll(view);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load /fxml/mood/MoodHistory.fxml", e);
        }
    }

    private void loadJournal() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/Journal.fxml"),
                    resources
            );

            final Parent view = loader.load();
            moodHost.getChildren().setAll(view);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load /fxml/mood/Journal.fxml", e);
        }
    }
}