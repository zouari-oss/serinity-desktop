package com.serinity.consultationcontrol;

import com.serinity.consultationcontrol.util.Router;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class App extends Application {
  private static Locale forcedLocale = null;
  private static final String DEFAULT_START_FXML = "/fxml/doctor/dashboard.fxml";
  private static final String DEFAULT_START_TITLE = "SERINITY";

  public static void setForcedLocale(Locale locale) {
    forcedLocale = locale;
  }

  public static Locale getEffectiveLocale() {
    return (forcedLocale != null) ? forcedLocale : Locale.getDefault();
  }

  public static void main(final String[] args) {
    launch();
  }

  @Override
  public void start(final Stage stage) throws Exception {
    final Locale locale = getEffectiveLocale();
    final String startFxml = System.getProperty("consultation.start.fxml", DEFAULT_START_FXML);
    final String startTitle = System.getProperty("consultation.start.title", DEFAULT_START_TITLE);

    FXMLLoader loader;
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
      loader = new FXMLLoader(getClass().getResource(startFxml), bundle);
    } catch (MissingResourceException ignored) {
      loader = new FXMLLoader(getClass().getResource(startFxml));
    }

    final Parent root = loader.load();
    final Scene scene = new Scene(root);
    stage.setTitle(startTitle);
    stage.setScene(scene);
    stage.setMinWidth(1040);
    stage.setMinHeight(720);

    if (getClass().getResource("/styles/app.css") != null) {
      scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
    }

    Router.init(stage);
    stage.centerOnScreen();
    stage.show();
  }
}
