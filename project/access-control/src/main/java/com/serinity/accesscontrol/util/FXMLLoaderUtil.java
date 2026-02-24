// `FXMLLoaderUtil` package name
package com.serinity.accesscontrol.util;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Utility class for loading JavaFX FXML views.
 *
 * <p>
 * Provides factory methods for loading FXML files together with their
 * controllers and an optional {@link ResourceBundle} for i18n support.
 * All methods throw {@link RuntimeException} wrapping any {@link IOException}
 * so callers are not required to handle checked exceptions.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 */
public final class FXMLLoaderUtil {

  private FXMLLoaderUtil() {
  }

  // -------------------------------------------------------------------------
  // ViewLoader record
  // -------------------------------------------------------------------------

  /**
   * Holds the loaded FXML root node together with its controller.
   *
   * @param <T> controller type
   */
  public static final class ViewLoader<T> {

    private final Parent root;
    private final T controller;

    private ViewLoader(final Parent root, final T controller) {
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

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Loads an FXML view and returns a {@link ViewLoader} containing both the
   * root {@link Parent} and the associated controller.
   *
   * @param <T>    controller type
   * @param caller class used to resolve the FXML resource
   * @param fxml   path to the FXML file (e.g. {@code "/fxml/login.fxml"})
   * @param bundle resource bundle for i18n; may be {@code null}
   * @return a {@link ViewLoader} with root and controller
   */
  public static <T> ViewLoader<T> loadView(
      final Class<?> caller,
      final String fxml,
      final ResourceBundle bundle) {

    final FXMLLoader loader = createLoader(caller, fxml, bundle);
    try {
      final Parent root = loader.load();
      final T controller = loader.getController();
      return new ViewLoader<>(root, controller);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to load FXML: " + fxml, e);
    }
  }

  /**
   * Loads an FXML file and returns only the root {@link Parent} node.
   *
   * @param caller class used to resolve the FXML resource
   * @param fxml   path to the FXML file
   * @param bundle resource bundle for i18n; may be {@code null}
   * @return the loaded root {@link Parent}
   */
  public static Parent loadFXML(
      final Class<?> caller,
      final String fxml,
      final ResourceBundle bundle) {

    return loadView(caller, fxml, bundle).getRoot();
  }

  /**
   * Loads an FXML file and wraps the root {@link Parent} in a new
   * {@link Scene}.
   *
   * @param caller class used to resolve the FXML resource
   * @param fxml   path to the FXML file
   * @param bundle resource bundle for i18n; may be {@code null}
   * @return a {@link Scene} containing the loaded root
   */
  public static Scene loadScene(
      final Class<?> caller,
      final String fxml,
      final ResourceBundle bundle) {

    return new Scene(loadFXML(caller, fxml, bundle));
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private static FXMLLoader createLoader(
      final Class<?> caller,
      final String fxml,
      final ResourceBundle bundle) {

    final FXMLLoader loader = new FXMLLoader(caller.getResource(fxml));
    if (bundle != null) {
      loader.setResources(bundle);
    }
    return loader;
  }
} // FXMLLoaderUtil final class
