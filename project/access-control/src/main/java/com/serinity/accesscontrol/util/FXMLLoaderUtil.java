/**
 * FXMLLoaderUtil.java
 *
 * Fxml loader util class
 *
 * <p>none</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 * <a href="https://github.com/zouari-oss/serinity" target="_blank">FXMLLoaderUtil.java</a>
 */

// `FXMLLoaderUtil` package name
package com.serinity.accesscontrol.util;

// `java` imports
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// `javafx` imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Fxml loader util class
 *
 * <p>
 * none
 * </p>
 *
 * <pre>
 * {@code
 * // Example
 * FXMLLoaderUtil.loadScene(this.getClass(), FXMLFile.SIGNIN.getFileName())
 * }
 * </pre>
 */
final public class FXMLLoaderUtil {
  // No i18n
  public static Scene loadScene(final Class<?> caller, final String fxmlPath) {
    return loadScene(caller, fxmlPath, null);
  }

  public static Scene loadScene(final Class<?> caller, final String fxmlPath, final ResourceBundle bundle) {
    try {
      final URL fxmlUrl = caller.getResource(fxmlPath);
      if (fxmlUrl == null) {
        throw new IllegalStateException("[ERROR] FXML file not found: " + fxmlPath);
      }

      final FXMLLoader loader = new FXMLLoader(fxmlUrl);
      if (bundle != null) {
        loader.setResources(bundle);
      }

      return new Scene(loader.load());

    } catch (final IOException e) {
      throw new RuntimeException("[ERROR] Failed to load FXML: " + fxmlPath, e);
    }
  }
} // FXMLLoaderUtil class
