// `CameraDesktopController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

// `opencv` import(s)
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.model.UserFace;
import com.serinity.accesscontrol.repository.UserFaceRepository;
import com.serinity.accesscontrol.service.AntelopeFaceService;
import com.serinity.accesscontrol.service.CameraDesktopService;
import com.serinity.accesscontrol.util.AntelopeUtil;
import com.serinity.accesscontrol.util.I18nUtil;
import com.serinity.accesscontrol.util.OpenCvUtil;

// `javafx` import(s)
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller for 'camera-desktop.fxml'.
 *
 * <p>
 * Supports two modes:
 * <ul>
 * <li><b>RECOGNIZE</b> (default) – matches a detected face against all stored
 * embeddings
 * and closes the window on success (used at login).</li>
 * <li><b>ENROLL</b> – captures the user's face, saves the embedding to the DB,
 * and closes the window (used from the user dashboard toggle).</li>
 * </ul>
 * Set enroll mode before showing the stage via
 * {@link #setEnrollMode(User, Runnable)}.
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/CameraDesktopController.java">
 *        CameraDesktopController.java
 *        </a>
 */
public final class CameraDesktopController {

  private enum Mode {
    RECOGNIZE, ENROLL
  }

  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(CameraDesktopController.class);

  // Enroll: best face capture over ENROLL_DURATION_MS
  private static final long ENROLL_DURATION_MS = 5_000;

  @FXML
  private ResourceBundle resources;

  @FXML
  private URL location;

  @FXML // fx:id="cameraImageView"
  private ImageView cameraImageView;

  private CameraDesktopService cameraService;
  private AntelopeFaceService faceService;

  private Thread captureThread;
  private final ExecutorService inferenceExecutor = Executors.newSingleThreadExecutor(
      r -> new Thread(r, "Inference-Thread"));
  private final AtomicBoolean inferenceRunning = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean enrolling = new AtomicBoolean(false);

  private long enrollStartTime = 0;
  private Mat bestCrop = null;
  private float bestScore = -1f;
  private volatile Rect lastFaceRect = null; // cached for drawing on skipped frames
  private Mode mode = Mode.RECOGNIZE;
  private User enrollUser;
  private Runnable onEnrollSuccess;
  private Consumer<User> onRecognizeSuccess;

  /* ===================== MODE CONFIGURATION ===================== */

  /**
   * Switch to recognize mode with a callback. Must be called before the stage is
   * shown.
   *
   * @param onRecognizeSuccess callback invoked on the JavaFX thread with the
   *                           matched User
   */
  public void setRecognizeMode(final Consumer<User> onRecognizeSuccess) {
    this.mode = Mode.RECOGNIZE;
    this.onRecognizeSuccess = onRecognizeSuccess;
  }

  /**
   * Switch to enroll mode. Must be called before the stage is shown.
   *
   * @param user            the user whose face will be enrolled
   * @param onEnrollSuccess callback invoked on the JavaFX thread after successful
   *                        enrollment
   */
  public void setEnrollMode(final User user, final Runnable onEnrollSuccess) {
    this.mode = Mode.ENROLL;
    this.enrollUser = user;
    this.onEnrollSuccess = onEnrollSuccess;
  }

  @FXML
  void onStopButtonAction(final ActionEvent event) {
    stopCamera();
    closeStage();
  }

  @FXML
  void onStartButtonAction(final ActionEvent event) {
    if (running.get())
      return;

    try {
      final UserFaceRepository userFaceRepository = new UserFaceRepository(
          SkinnedRatOrmEntityManager.getEntityManager());
      faceService = new AntelopeFaceService(userFaceRepository);

      cameraService = new CameraDesktopService(0);
      running.set(true);
      captureThread = cameraService.startCapture(this::processFrame, running);

    } catch (final Exception e) {
      _LOGGER.error("Failed to start camera or face service", e);
    }
  }

  @FXML
  void initialize() {
    assert cameraImageView != null
        : "fx:id=\"cameraImageView\" was not injected: check your FXML file 'camera-desktop.fxml'.";
  }

  private void processFrame(final Mat frame) {
    // Draw cached face rect so display is always up to date regardless of inference
    final Rect rect = lastFaceRect;
    if (rect != null) {
      Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
    }

    // Display at full camera FPS (non-blocking)
    Platform.runLater(() -> cameraImageView.setImage(OpenCvUtil.matToImage(frame)));

    // Submit inference only when previous one has finished — never blocks capture
    if (inferenceRunning.compareAndSet(false, true)) {
      final Mat inferenceFrame = frame.clone();
      inferenceExecutor.submit(() -> {
        try {
          runInference(inferenceFrame);
        } finally {
          inferenceRunning.set(false);
        }
      });
    }
  }

  private void runInference(final Mat frame) {
    try {
      final Rect face = faceService.detectBestFace(frame);
      lastFaceRect = face;

      if (face == null)
        return;

      final Mat crop = faceService.cropFaceSafely(frame, face);

      if (mode == Mode.ENROLL) {
        handleEnrollFrame(face, crop);
      } else {
        final User user = faceService.recognizeUser(crop);
        if (user != null) {
          stopCamera();
          Platform.runLater(() -> {
            closeStage();
            if (onRecognizeSuccess != null)
              onRecognizeSuccess.accept(user);
          });
        }
      }
    } catch (final Exception e) {
      _LOGGER.error("Error in face inference", e);
    }
  }

  private void handleEnrollFrame(final Rect face, final Mat crop) throws Exception {
    if (enrolling.get())
      return; // already saving

    final long now = System.currentTimeMillis();

    // Start the 5s window on first detection
    if (enrollStartTime == 0) {
      enrollStartTime = now;
      bestCrop = null;
      bestScore = -1f;
      Platform.runLater(() -> cameraImageView.setUserData("recording"));
    }

    // Track best face (largest area = closest face = best quality)
    final float score = face.width * face.height;
    if (score > bestScore) {
      bestScore = score;
      bestCrop = crop.clone(); // clone so it survives the next frame
    }

    final long elapsed = now - enrollStartTime;
    final long remaining = (ENROLL_DURATION_MS - elapsed) / 1000 + 1;

    // Update countdown on UI
    Platform.runLater(() -> {
      final Stage stage = (Stage) cameraImageView.getScene().getWindow();
      stage.setTitle(
          MessageFormat.format(I18nUtil.getValue("camera.stage.enrollment.countdown"), Math.max(0, remaining)));
    });

    // Time is up — save best capture
    if (elapsed >= ENROLL_DURATION_MS && enrolling.compareAndSet(false, true)) {
      if (bestCrop != null) {
        saveBestFace(bestCrop);
      } else {
        // No face detected at all
        enrolling.set(false);
        enrollStartTime = 0;
        Platform.runLater(() -> {
          final Stage stage = (Stage) cameraImageView.getScene().getWindow();
          stage.setTitle(I18nUtil.getValue("camera.stage.enrollment.no_face"));
        });
      }
    }
  }

  private void saveBestFace(final Mat crop) throws Exception {
    final float[] embedding = faceService.extractEmbedding(crop);
    stopCamera();

    final byte[] embeddingBytes = AntelopeUtil.floatsToBytes(embedding);

    final UserFaceRepository userFaceRepository = new UserFaceRepository(
        SkinnedRatOrmEntityManager.getEntityManager());

    final UserFace existing = userFaceRepository.findByUserId(enrollUser.getId());
    if (existing != null) {
      userFaceRepository.delete(existing);
    }
    userFaceRepository.save(new UserFace(enrollUser, embeddingBytes));

    Platform.runLater(() -> {
      if (onEnrollSuccess != null)
        onEnrollSuccess.run();
      closeStage();
    });
  }

  private void stopCamera() {
    running.set(false);
    if (cameraService != null)
      cameraService.close();
    // Wait for the capture thread to finish its current frame
    if (captureThread != null) {
      try {
        captureThread.join(2000);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    // Shutdown inference executor and wait for any running inference to complete
    inferenceExecutor.shutdown();
    try {
      inferenceExecutor.awaitTermination(3000, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    try {
      if (faceService != null)
        faceService.close();
    } catch (final Exception e) {
      _LOGGER.error("Error closing face service", e);
    }
  }

  private void closeStage() {
    if (cameraImageView.getScene() != null) {
      final Stage stage = (Stage) cameraImageView.getScene().getWindow();
      stage.close();
    }
  }
} // `CameraDesktopController` final class
