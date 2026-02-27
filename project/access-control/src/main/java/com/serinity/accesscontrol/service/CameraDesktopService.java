// `CameraDesktopService` package name
package com.serinity.accesscontrol.service;

// `java` import(s)
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

// `opencv` import(s)
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 * CameraService handles webcam access and frame capture.
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/service/CameraDesktopService.java">
 *        CameraDesktopService.java
 *        </a>
 */
public final class CameraDesktopService {

  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(CameraDesktopService.class);
  private static final ReentrantLock lock = new ReentrantLock();

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load opencv lib (`libopencv_java4130.so`)
  }

  private boolean open;
  private final VideoCapture capture;

  public CameraDesktopService(final int cameraIndex) {
    lock.lock();

    try {
      capture = new VideoCapture(cameraIndex);
      open = capture.isOpened();
      if (!open) {
        throw new RuntimeException("Cannot open camera with index: " + cameraIndex);
      }

    } finally {
      lock.unlock();
    }
  }

  public boolean isOpen() {
    lock.lock();
    try {
      return open && capture.isOpened();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Starts a background thread that continuously captures frames from the camera
   * and passes each frame to the provided consumer.
   *
   * <p>
   * The loop runs as long as {@code running} is {@code true} and the camera is
   * open. Set {@code running} to {@code false} to stop capture gracefully.
   * </p>
   *
   * @param frameConsumer callback invoked with each captured {@link Mat} frame
   * @param running       atomic flag; set to {@code false} to stop the loop
   */
  public void startCapture(final Consumer<Mat> frameConsumer, final AtomicBoolean running) {
    new Thread(() -> {
      while (running.get() && isOpen()) {
        try {
          final Mat frame = captureFrame();
          frameConsumer.accept(frame);
        } catch (final Exception e) {
          _LOGGER.error("Camera capture error", e);
        }
      }
    }, "Camera-Thread").start();
  }

  /**
   * Captures a single frame from the camera.
   *
   * @return OpenCV Mat containing the frame
   */
  public Mat captureFrame() {
    lock.lock();
    try {
      if (!open) {
        throw new IllegalStateException("Camera not opened");
      }
      final Mat frame = new Mat();
      if (capture.read(frame)) {
        return frame;
      } else {
        throw new RuntimeException("Failed to capture frame from camera");
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Releases the camera resources.
   */
  public void close() {
    lock.lock();
    try {
      if (capture.isOpened()) {
        capture.release();
        open = false;
      }
    } finally {
      lock.unlock();
    }
  }
} // `CameraDesktopService` final class
