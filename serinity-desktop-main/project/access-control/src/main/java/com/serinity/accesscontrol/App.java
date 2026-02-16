/**
 * App.java
 *
 * Serinity app entry point
 *
 * <p>none</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/App.java"
 * target="_blank">
 * App.java
 * </a>
 */

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
 * App class entry point (launcher)
 *
 * <p>
 * More detailed description or notes.
 * </p>
 */
public class App extends Application {
  public static void main(final String[] args) {
    launch();
  }

  @Override
  public void start(final Stage stage) {
    I18nUtil.applySupportedLocale();
    stage.setScene(FXMLLoaderUtil.loadScene(
        this.getClass(),
        ResourceFile.LOGIN_FXML.getFileName(),
        I18nUtil.getBundle()));
    stage.show();
  }
} // App class
