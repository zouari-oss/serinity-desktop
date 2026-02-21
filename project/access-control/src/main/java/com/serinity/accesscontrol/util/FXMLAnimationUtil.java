// `FXMLAnimationUtil` package name
package com.serinity.accesscontrol.util;

// `javafx` import(s)
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * Utility class for handling animations in JavaFX FXML components.
 *
 * <p>
 * This class provides static helper methods to perform common animations
 * on JavaFX nodes, such as sliding a {@link WebView} in and out of the
 * viewport. It is designed to simplify UI transitions and make code more
 * readable and maintainable.
 * </p>
 *
 * <p>
 * NOTE: All methods in this class are static, and the class cannot be
 * instantiated.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-09
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/FXMLAnimationUtil.java">
 *        FXMLAnimationUtil.java
 *        </a>
 */
public final class FXMLAnimationUtil {

  /**
   * Animates a {@link WebView} by sliding it horizontally across the screen.
   *
   * <p>
   * If {@code shift} is {@code true}, the WebView will slide to the right edge of
   * the stage.
   * If {@code shift} is {@code false}, it will slide back to its original
   * position (x = 0).
   * This is useful for creating smooth UI transitions when showing or hiding
   * full-screen
   * content.
   * </p>
   *
   * @param node       the {@link WebView} to animate
   * @param stageWidth the width of the stage, used to calculate the target X
   *                   position
   * @param shift      whether to slide the WebView out (true) or back to its
   *                   original position (false)
   *
   * @see javafx.animation.TranslateTransition
   *
   *      <pre>{@code
   * // Example usage:
   * WebView webView = new WebView();
   * double stageWidth = 1024;
   * // Slide the WebView out to full screen
   * FXMLAnimationUtil.slideFullScreen(webView, stageWidth, true);
   * // Slide the WebView back to its original position
   * FXMLAnimationUtil.slideFullScreen(webView, stageWidth, false);
   * }</pre>
   */
  public static void slideFullScreen(final WebView node, final double stageWidth, final boolean shift) {
    final TranslateTransition tt = new TranslateTransition(Duration.millis(1000), node);
    tt.setToX(shift ? stageWidth - node.getWidth() : 0);
    tt.play();
  }

  /**
   * Animates a {@link Node} by sliding it vertically from top to bottom and
   * optionally hides it after a delay.
   *
   * <p>
   * Useful for status messages, notifications, or temporary banners in your UI.
   * </p>
   *
   * @param node             the {@link Node} to animate (e.g., Label)
   * @param distance         how far to slide the node (positive = down)
   * @param duration         duration of the slide animation in milliseconds
   * @param autoHide         if true, the node will slide back and be hidden after
   *                         {@code hideDelaySeconds}
   * @param hideDelaySeconds seconds to wait before hiding the node
   *
   *                         <pre>{@code
   * // Example usage:
   * FXMLAnimationUtil.slideFromTop(messageStatusLabel, 50, 500, true, 3);
   * }</pre>
   */
  public static void slideFromTop(final Node node, final double distance, final int duration,
      final boolean autoHide, final int hideDelaySeconds) {
    // Ensure the node is visible
    node.setVisible(true);

    // Slide down
    TranslateTransition slideDown = new TranslateTransition(Duration.millis(duration), node);
    slideDown.setFromY(-distance);
    slideDown.setToY(0);
    slideDown.play();

    if (autoHide) {
      PauseTransition wait = new PauseTransition(Duration.seconds(hideDelaySeconds));
      wait.setOnFinished(e -> {
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(duration), node);
        slideUp.setFromY(0);
        slideUp.setToY(-distance);
        slideUp.setOnFinished(ev -> node.setVisible(false));
        slideUp.play();
      });
      wait.play();
    }
  }
} // FXMLAnimationUtil class
