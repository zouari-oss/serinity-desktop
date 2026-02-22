// `FXMLLoaderUtil` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// `javafx` import(s)
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Fxml loader util class
 *
 * <p>
 * see `/fxml/*`
 * </p>
 *
 * <pre>{@code
 * // Example usage
 * Scene scene = FXMLLoaderUtil.loadScene(this.getClass(), FXMLFile.SIGNIN.getFileName());
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/FXMLLoaderUtil.java">
 *        FXMLLoaderUtil.java
 *        </a>
 */
final public class FXMLLoaderUtil {
  public static Scene loadScene(final Class<?> caller, final String fxmlPath) {
    return loadScene(caller, fxmlPath, null);
  }

  public static Scene loadScene(final Class<?> caller, final String fxmlPath, final ResourceBundle bundle) {
    return new Scene(loadRoot(caller, fxmlPath, bundle));
  }

  public static Parent loadFXML(final Class<?> caller, final String fxmlPath) {
    return loadFXML(caller, fxmlPath, null);
  }

  public static Parent loadFXML(final Class<?> caller, final String fxmlPath, final ResourceBundle bundle) {
    return loadRoot(caller, fxmlPath, bundle);
  }

  public static <T> ViewLoader<T> loadView(
      final Class<?> caller,
      final String fxmlPath,
      final ResourceBundle bundle) {

    try {
      final URL fxmlUrl = caller.getResource(fxmlPath);
      if (fxmlUrl == null) {
        throw new IllegalStateException("[ERROR] FXML file not found: " + fxmlPath);
      }

      final FXMLLoader loader = new FXMLLoader(fxmlUrl);
      if (bundle != null) {
        loader.setResources(bundle);
      }

      Parent root = loader.load();
      T controller = loader.getController();

      return new ViewLoader<>(root, controller);

    } catch (IOException e) {
      throw new RuntimeException("[ERROR] Failed to load FXML: " + fxmlPath, e);
    }
  }

  /**
   * Wrapper class that holds both the loaded FXML root node and its controller.
   *
   * <p>
   * This is typically used for navigation systems to access both the root
   * node to add to a layout (e.g., StackPane) and the controller to inject
   * dependencies or configure callbacks.
   * </p>
   *
   * @param <T> The type of the controller associated with the FXML.
   */
  public static final class ViewLoader<T> {

    private final Parent root;
    private final T controller;

    public ViewLoader(Parent root, T controller) {
      this.root = root;
      this.controller = controller;
    }

    public Parent getRoot() {
      return root;
    }

    public T getController() {
      return controller;
    }
  }

  private static Parent loadRoot(final Class<?> caller, final String fxmlPath, final ResourceBundle bundle) {
    try {
      final URL fxmlUrl = caller.getResource(fxmlPath);
      if (fxmlUrl == null) {
        throw new IllegalStateException("[ERROR] FXML file not found: " + fxmlPath);
      }

      final FXMLLoader loader = new FXMLLoader(fxmlUrl);
      if (bundle != null) {
        loader.setResources(bundle);
      }

      return loader.load();

    } catch (IOException e) {
      throw new RuntimeException("[ERROR] Failed to load FXML: " + fxmlPath, e);
    }
  }
} // FXMLLoaderUtil class
