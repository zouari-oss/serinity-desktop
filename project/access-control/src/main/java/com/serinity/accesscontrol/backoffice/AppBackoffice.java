package com.serinity.accesscontrol.backoffice;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppBackoffice extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(
        AppBackoffice.class.getResource("/fxml/backoffice/BackofficeApp.fxml"));

    Scene scene = new Scene(loader.load(), 1180, 720); // default size

    var backendCss = AppBackoffice.class.getResource("/styles/backoffice.css");
    if (backendCss != null)
      scene.getStylesheets().add(backendCss.toExternalForm());

    var globalCss = AppBackoffice.class.getResource("/styles/styles.css");
    if (globalCss != null)
      scene.getStylesheets().add(globalCss.toExternalForm());

    stage.setTitle("Serinity • Backoffice Admin");
    stage.setMinWidth(980);
    stage.setMinHeight(620);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
