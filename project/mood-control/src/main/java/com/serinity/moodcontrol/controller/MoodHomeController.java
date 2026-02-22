package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.service.QuoteService;
import com.serinity.moodcontrol.service.QuoteService.QuoteResult;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MoodHomeController {

    @FXML
    private StackPane moodHost;

    @FXML
    private StackPane cardLogMood;

    @FXML
    private StackPane cardMoodHistory;

    @FXML
    private StackPane cardJournal;

    // === Daily Zen UI ===
    @FXML
    private Label zenQuoteText;

    @FXML
    private Label zenQuoteAuthor;

    @FXML
    private Button btnNewQuote;

    @FXML
    private ResourceBundle resources;

    private final QuoteService quoteService = new QuoteService();

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

        // Load a quote on page open
        loadZenQuote();
    }

    private void loadZenQuote() {
        // UI: loading state
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

        // Error fallback (from i18n if present)
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

    private void loadWizard() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/Wizard.fxml"),
                    resources);
            final Parent view = loader.load();

            final StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost); // link loadi fel stackpane

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