package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainTemplateController {

    @FXML private Button btnDashboard;
    @FXML private Button btnSleep;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;

    @FXML private Label userNameLabel;
    @FXML private StackPane contentHost;
    @FXML private Label footerLabel;

    // Footer language menu (Template.fxml)
    @FXML private MenuButton langMenu;
    @FXML private MenuItem langEN;
    @FXML private MenuItem langFR;

    @FXML private ResourceBundle resources;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected. Make sure Template.fxml is loaded with a bundle.");
        }

        navButtons = List.of(
                btnDashboard, btnSleep, btnMood,
                btnSupport, btnExercises, btnAppointments
        );

        footerLabel.setText(resources.getString("footer.text"));

        userNameLabel.setText(resources.getString("user.name.placeholder"));

        initLanguageMenu();

    }

    private void initLanguageMenu() {
        Locale current = resources.getLocale();
        langMenu.setText("fr".equals(current.getLanguage()) ? "FR" : "EN");

        langEN.setOnAction(e -> switchLang(Locale.ENGLISH));
        langFR.setOnAction(e -> switchLang(Locale.FRENCH));
    }

    private void switchLang(Locale target) {
        if (resources.getLocale().getLanguage().equals(target.getLanguage())) return;

        try {
            App.setForcedLocale(target);

            Stage stage = (Stage) contentHost.getScene().getWindow();
            new App().start(stage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch language.", e);
        }
    }

    @FXML
    private void onNavClick(final ActionEvent event) {
        final Button clicked = (Button) event.getSource();
        setActiveNav(clicked);

        if (clicked == btnSupport) {
            loadIntoHost("/fxml/forum/ForumPostsView.fxml");
        } else {
            loadIntoHost("/fxml/pages/Blank.fxml");
        }
    }

    private void setActiveNav(final Button activeBtn) {
        for (final Button b : navButtons) {
            if (!b.getStyleClass().contains("nav-btn")) {
                b.getStyleClass().add("nav-btn");
            }
            b.getStyleClass().remove("nav-btn-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadIntoHost(final String fxmlPath) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), resources);
            final Parent page = loader.load();
            contentHost.getChildren().setAll(page);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }
}
