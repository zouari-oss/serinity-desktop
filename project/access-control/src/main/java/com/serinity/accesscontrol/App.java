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
 * <a href="https://github.com/zouari-oss/hashhash-books" target="_blank">App.java</a>
 */

// `App` package name
package com.serinity.accesscontrol;

// `serinity` imports
import com.serinity.accesscontrol.flag.FXMLFile;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` imports
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
        FXMLFile.SIGNIN.getFileName(),
        I18nUtil.getBundle()));
    stage.show();
  }
} // App class
