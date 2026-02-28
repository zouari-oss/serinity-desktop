// `OpenCvUtil` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.io.ByteArrayInputStream;

// `opencv` import(s)
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

// `javafx` import(s)
import javafx.scene.image.Image;

/**
 * Utility class providing OpenCV-to-JavaFX conversion helpers.
 *
 * <p>
 * Bridges the gap between OpenCV's {@link org.opencv.core.Mat} image format
 * and JavaFX's {@link javafx.scene.image.Image}, enabling frames captured
 * from the camera service to be displayed in the UI.
 * </p>
 *
 * <p>
 * NOTE: This class is {@code final} and cannot be instantiated.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 * @see com.serinity.accesscontrol.service.CameraDesktopService
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/OpenCvUtil.java">
 *      OpenCvUtil.java
 *      </a>
 */
public final class OpenCvUtil {
  /**
   * Converts an OpenCV {@link org.opencv.core.Mat} frame to a JavaFX
   * {@link javafx.scene.image.Image} by encoding it as PNG in memory.
   *
   * @param frame the OpenCV image matrix to convert
   * @return a JavaFX {@link javafx.scene.image.Image} ready for display
   */
  public static Image matToImage(final Mat frame) {
    final MatOfByte buffer = new MatOfByte();
    Imgcodecs.imencode(".png", frame, buffer);
    return new Image(new ByteArrayInputStream(buffer.toArray()));
  }
} // `OpenCvUtil` final class
