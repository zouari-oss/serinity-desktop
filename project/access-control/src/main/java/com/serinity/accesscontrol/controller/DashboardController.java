
// `DashboardController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * `dashboard.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/DashboardController.java">
 *        DashboardController.java
 *        </a>
 */
public final class DashboardController {
  @FXML
  private Button btnDashboard;

  @FXML
  private Button btnSleep;

  @FXML
  private Button btnMood;

  @FXML
  private Button btnSupport;

  @FXML
  private Button btnExercises;

  @FXML
  private Button btnAppointments;

  @FXML
  private Label userNameLabel;

  @FXML
  private StackPane contentHost;

  @FXML
  private Label footerLabel;

  @FXML
  private ResourceBundle resources;

  private List<Button> navButtons;

  @FXML
  public void initialize() {
    navButtons = List.of(
        btnDashboard, btnSleep, btnMood,
        btnSupport, btnExercises, btnAppointments);

    // footerLabel.setText(resources.getString("footer.text"));

    // userNameLabel.setText(resources.getString("user.name.placeholder"));

    // Default landing
    setActiveNav(btnMood);
    // loadIntoHost("/fxml/mood/MoodHome.fxml");
  }

  private void switchLang(final Locale target) {
    if (resources.getLocale().getLanguage().equals(target.getLanguage()))
      return;
  }

  @FXML
  private void onNavClick(final ActionEvent event) {
    final Button clicked = (Button) event.getSource();
    setActiveNav(clicked);

    if (clicked == btnMood) {
      loadIntoHost("/fxml/mood/MoodHome.fxml");
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
} // DashboardController final class
