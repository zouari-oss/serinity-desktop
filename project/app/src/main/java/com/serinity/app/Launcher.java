package com.serinity.app;

import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

import javafx.application.Application;
import javafx.stage.Stage;

public final class Launcher extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(final Stage stage) {
    I18nUtil.applySupportedLocale();
    stage.setScene(FXMLLoaderUtil.loadScene(
        com.serinity.accesscontrol.App.class,
        ResourceFile.ROOT_FXML.getFileName(),
        I18nUtil.getBundle()));
    stage.setTitle(I18nUtil.getValue("app.scene.title.sign_in"));
    stage.centerOnScreen();
    stage.show();
  }
}
