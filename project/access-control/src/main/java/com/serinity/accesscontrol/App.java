// `App` package name
package com.serinity.accesscontrol;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` import(s)
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * App.java
 *
 * Serinity app entry point
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/App.java">
 *        App.java
 *        </a>
 */
public class App extends Application {
  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(App.class);

  public static void main(final String[] args) {
    launch();
  }

  @Override
  public void start(final Stage stage) {
    I18nUtil.applySupportedLocale();
    stage.setScene(FXMLLoaderUtil.loadScene(
        this.getClass(),
        ResourceFile.ROOT_FXML.getFileName(),
        I18nUtil.getBundle()));
    stage.centerOnScreen();
    stage.show();
    _LOGGER.info("App Launched successfully!");
  }
} // App class
