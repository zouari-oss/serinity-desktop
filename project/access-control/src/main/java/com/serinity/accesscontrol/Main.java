// `Main` package name
package com.serinity.accesscontrol;

import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Serinity access-control module.
 *
 * <p>
 * Bootstraps the JavaFX application and loads the root FXML layout which
 * hosts the {@code RootController}.  All subsequent navigation is driven
 * by stack-based FXML loading through {@link FXMLLoaderUtil}.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 */
public class Main extends Application {

  @Override
  public void start(final Stage primaryStage) {
    final Scene scene = FXMLLoaderUtil.loadScene(
        getClass(),
        ResourceFile.ROOT_FXML.getFileName(),
        I18nUtil.getBundle());

    primaryStage.setTitle("Serinity");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  public static void main(final String[] args) {
    launch(args);
  }
} // Main class
