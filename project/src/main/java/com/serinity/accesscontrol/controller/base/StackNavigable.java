// `BaseController` package name
package com.serinity.accesscontrol.controller.base;

import java.util.function.Consumer;

// `serinity` import(s)
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` import(s)
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Provides stack-based navigation (push, replace, pop) for controllers
 * that manage content within a {@link javafx.scene.layout.StackPane}.
 *
 * <p>
 * Implementing controllers can use this interface to dynamically
 * load and swap FXML views in a StackPane without tightly coupling
 * to the root layout.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * controller.setStackHost(rootStackPane);
 * controller.push("some-view.fxml");
 * controller.pop();
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 * @see javafx.scene.layout.StackPane
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/base/StackNavigable.java">
 *      StackNavigable.java
 *      </a>
 */
public interface StackNavigable {

  /**
   * Updates the stage title if {@code controller} implements {@link StageTitled}.
   */
  private static void applySceneTitle(final StackPane host, final Object controller) {
    if (!(controller instanceof final StageTitled titled))
      return;
    if (host.getScene() == null)
      return;
    ((Stage) host.getScene().getWindow()).setTitle(I18nUtil.getValue(titled.getSceneTitleKey()));
  }

  /**
   * Returns the StackPane hosting dynamic views.
   */
  StackPane getStackHost();

  /**
   * Inject the StackPane host.
   */
  default void setStackHost(final StackPane host) {
    // Implementing class should override if needed
  }

  /**
   * Inject the status message provider.
   */
  default void setStatusProvider(final StatusMessageProvider provider) {
    // Implementing class should override if needed
  }

  /**
   * Push a new FXML view onto the stack.
   *
   * @param fxml path to FXML
   * @param <T>  type of controller
   */
  default <T> void push(
      final String fxml,
      final Consumer<T> controllerInitializer) {

    final StackPane host = getStackHost();
    if (host == null)
      return;

    final FXMLLoaderUtil.ViewLoader<T> view = FXMLLoaderUtil.loadView(
        getClass(),
        fxml,
        I18nUtil.getBundle());

    final T controller = view.getController();

    // Inject navigation
    if (controller instanceof final StackNavigable navController) {
      navController.setStackHost(host);

      if (this instanceof final StatusMessageProvider statusProvider) {
        navController.setStatusProvider(statusProvider);
      }
    }

    // Inject custom data
    if (controllerInitializer != null) {
      controllerInitializer.accept(controller);
    }

    // Store controller on root node so pop() can retrieve it for title restore
    view.getRoot().setUserData(controller);
    host.getChildren().add(view.getRoot());

    // Update stage title if the new controller declares one
    applySceneTitle(host, controller);
  }

  default <T> void push(final String fxml) {
    push(fxml, null);
  }

  /**
   * Replace the current view with a new FXML.
   *
   * @param fxml path to FXML
   */
  default void replace(final String fxml) {
    replace(fxml, null);
  }

  /**
   * Replace the current view with a new FXML.
   *
   * @param fxml path to FXML
   * @param <T>  type of controller
   */
  default <T> void replace(final String fxml,
      final Consumer<T> controllerInitializer) {
    final StackPane host = getStackHost();
    if (host == null)
      return;

    final FXMLLoaderUtil.ViewLoader<T> view = FXMLLoaderUtil.loadView(
        getClass(),
        fxml,
        I18nUtil.getBundle());

    final T controller = view.getController();

    // Inject navigation
    if (controller instanceof final StackNavigable navController) {
      navController.setStackHost(host);

      if (this instanceof final StatusMessageProvider statusProvider) {
        navController.setStatusProvider(statusProvider);
      }
    }

    // Inject custom data
    if (controllerInitializer != null) {
      controllerInitializer.accept(controller);
    }

    // Store controller on root node (for title restore consistency)
    view.getRoot().setUserData(controller);

    // Replace current content
    host.getChildren().setAll(view.getRoot());

    // Update stage title
    applySceneTitle(host, controller);
  }

  /**
   * Pop the top view from the stack and restore the stage title of the
   * view now on top (if it implements {@link StageTitled}).
   */
  default void pop() {
    final StackPane host = getStackHost();
    if (host == null)
      return;

    if (!host.getChildren().isEmpty()) {
      host.getChildren().remove(host.getChildren().size() - 1);
    }

    // Restore the title of the view now on top
    if (!host.getChildren().isEmpty()) {
      final Node top = host.getChildren().get(host.getChildren().size() - 1);
      if (top.getUserData() != null) {
        applySceneTitle(host, top.getUserData());
      }
    }
  }
} // StackNavigable interface
