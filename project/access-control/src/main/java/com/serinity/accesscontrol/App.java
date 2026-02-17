// `App` package name
package com.serinity.accesscontrol;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.migration.SkinnedRatOrmMigrator;
// import com.serinity.accesscontrol.migration.SkinnedRatOrmMigrator; // TO_MIGRATE
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
  public static void main(final String[] args) {
    launch();
  }

  @Override
  public void start(final Stage stage) throws Exception {
    // TEST:
    SkinnedRatOrmMigrator.migrate();
    I18nUtil.applySupportedLocale();
    stage.setScene(FXMLLoaderUtil.loadScene(
        this.getClass(),
        ResourceFile.LOGIN_FXML.getFileName(),
        I18nUtil.getBundle()));
    stage.show();
  }
} // App class
