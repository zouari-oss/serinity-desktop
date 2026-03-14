package com.serinity.consultationcontrol;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  private static Locale forcedLocale = null;

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
    FXMLLoader loader;
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
      loader = new FXMLLoader(getClass().getResource("/fxml/doctor/dashboard.fxml"), bundle);
    } catch (MissingResourceException ignored) {
      loader = new FXMLLoader(getClass().getResource("/fxml/doctor/dashboard.fxml"));
    }
    final Parent root = loader.load();
    final Scene scene = new Scene(root);
    stage.setTitle("SERINITY");
    stage.setScene(scene);
    stage.show();
  }
}
