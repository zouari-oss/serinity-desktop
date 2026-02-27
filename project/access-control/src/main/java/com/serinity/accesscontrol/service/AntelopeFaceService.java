// `AntelopeFaceService` package name
package com.serinity.accesscontrol.service;

// `onnxruntime` import(s)
import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

// `opencv` import(s)
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.model.UserFace;
import com.serinity.accesscontrol.repository.UserFaceRepository;
import com.serinity.accesscontrol.util.AntelopeUtil;

// `java` import(s)
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Combined face detection + recognition service using AntelopeV2 ONNX models.
 *
 * <ul>
 * <li>Detection — SCRFD (scrfd_10g_bnkps.onnx)</li>
 * <li>Recognition — ArcFace / GlinT R100 (glintr100.onnx)</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/service/AntelopeFaceService.java">
 *        AntelopeFaceService.java
 *        </a>
 */
public final class AntelopeFaceService {

  // ---- SCRFD constants ----
  private static final int DETECTION_INPUT_SIZE = 640;
  private static final float CONF_THRESHOLD = 0.65f;
  private static final int MIN_FACE_SIZE = 60;
  private static final int NUM_ANCHORS = 2;

  // stride → expected anchor count per stride
  private static final int[] STRIDES = { 8, 16, 32 };

  // ---- ArcFace constants ----
  private static final double SIMILARITY_THRESHOLD = 0.6;

  private final OrtEnvironment env;
  private final OrtSession detectionSession;
  private final OrtSession recognitionSession;
  private final String detectionInputName;
  private final String recognitionInputName;
  private final UserFaceRepository userFaceRepository;

  // Resolved by shape at construction time: index = stride index (0=8,1=16,2=32)
  private final String[] scoreOutputNames = new String[3];
  private final String[] bboxOutputNames = new String[3];

  public AntelopeFaceService(
      final String scrfdModelPath,
      final String arcfaceModelPath,
      final UserFaceRepository userFaceRepository) throws Exception {
    this.userFaceRepository = userFaceRepository;

    env = OrtEnvironment.getEnvironment();
    this.detectionSession = AntelopeUtil.loadSession(env, ResourceFile.ANTELOPEV2_SCRFD_10G_BNKPS_ONNX.getFileName());
    this.recognitionSession = AntelopeUtil.loadSession(env, ResourceFile.ANTELOPEV2_GLINTR100_ONNX.getFileName());
    this.detectionInputName = detectionSession.getInputNames().iterator().next();
    this.recognitionInputName = recognitionSession.getInputNames().iterator().next();
    resolveOutputNames();
  }

  /**
   * Identifies score and bbox output names by their tensor shape:
   * score → [N, 1], bbox → [N, 4], kps → [N, 10]
   * Groups them by N (anchor count) to match the stride they belong to.
   */
  private void resolveOutputNames() throws OrtException {
    final int[] anchorCounts = new int[STRIDES.length];
    for (int i = 0; i < STRIDES.length; i++) {
      int cells = DETECTION_INPUT_SIZE / STRIDES[i];
      anchorCounts[i] = cells * cells * NUM_ANCHORS;
    }

    final float[][][][] dummy = new float[1][3][DETECTION_INPUT_SIZE][DETECTION_INPUT_SIZE];
    try (OnnxTensor tensor = OnnxTensor.createTensor(env, dummy)) {
      final OrtSession.Result result;
      synchronized (detectionSession) {
        result = detectionSession.run(Collections.singletonMap(detectionInputName, tensor));
      }
      try (result) {
        for (Map.Entry<String, NodeInfo> entry : detectionSession.getOutputInfo().entrySet()) {
          final String name = entry.getKey();
          final OnnxValue val = result.get(name).orElse(null);
          if (val == null)
            continue;
          final float[][] data = (float[][]) val.getValue();
          final long n = data.length;
          final long cols = data[0].length;

          for (int i = 0; i < anchorCounts.length; i++) {
            if (n == anchorCounts[i]) {
              if (cols == 1 && scoreOutputNames[i] == null)
                scoreOutputNames[i] = name;
              else if (cols == 4 && bboxOutputNames[i] == null)
                bboxOutputNames[i] = name;
              break;
            }
          }
        }
      }
    }
  }

  /* ===================== DETECTION ===================== */

  /** Returns the bounding box of the largest detected face, or {@code null}. */
  public Rect detectBestFace(final Mat frame) throws Exception {
    final List<Rect> faces = detectFaces(frame);
    if (faces.isEmpty())
      return null;
    return Collections.max(faces, (a, b) -> Integer.compare((int) a.area(), (int) b.area()));
  }

  /** Crops the face region from the frame, clamped to frame boundaries. */
  public Mat cropFaceSafely(final Mat frame, final Rect face) {
    final int x = Math.max(0, face.x);
    final int y = Math.max(0, face.y);
    final int w = Math.min(face.width, frame.cols() - x);
    final int h = Math.min(face.height, frame.rows() - y);
    return new Mat(frame, new Rect(x, y, w, h));
  }

  private List<Rect> detectFaces(final Mat frame) throws Exception {
    final int origH = frame.rows(), origW = frame.cols();
    final Mat resized = new Mat();
    Imgproc.resize(frame, resized, new Size(DETECTION_INPUT_SIZE, DETECTION_INPUT_SIZE));
    final float scaleX = (float) origW / DETECTION_INPUT_SIZE;
    final float scaleY = (float) origH / DETECTION_INPUT_SIZE;
    final float[][][][] tensorData = AntelopeUtil.detectionMatToTensor(resized);

    final List<Rect> boxes = new ArrayList<>();
    final List<Float> scores = new ArrayList<>();

    try (OnnxTensor tensor = OnnxTensor.createTensor(env, tensorData)) {
      final OrtSession.Result result;
      synchronized (detectionSession) {
        result = detectionSession.run(Collections.singletonMap(detectionInputName, tensor));
      }
      try (result) {

        for (int i = 0; i < STRIDES.length; i++) {
          final int stride = STRIDES[i];
          if (scoreOutputNames[i] == null || bboxOutputNames[i] == null)
            continue;

          final OnnxValue scoreVal = result.get(scoreOutputNames[i]).orElse(null);
          final OnnxValue bboxVal = result.get(bboxOutputNames[i]).orElse(null);
          if (scoreVal == null || bboxVal == null)
            continue;

          final float[][] scoreData = (float[][]) scoreVal.getValue();
          final float[][] bboxData = (float[][]) bboxVal.getValue();

          final int fh = DETECTION_INPUT_SIZE / stride;
          final int fw = DETECTION_INPUT_SIZE / stride;

          int idx = 0;
          outer: for (int y = 0; y < fh; y++) {
            for (int x = 0; x < fw; x++) {
              for (int a = 0; a < NUM_ANCHORS; a++) {
                if (idx >= scoreData.length)
                  break outer;
                // scores are raw logits — apply sigmoid before thresholding
                final float score = (float) (1.0 / (1.0 + Math.exp(-scoreData[idx][0])));
                if (score >= CONF_THRESHOLD) {
                  final float cx = (x + 0.5f) * stride;
                  final float cy = (y + 0.5f) * stride;
                  final float x1 = (cx - bboxData[idx][0] * stride) * scaleX;
                  final float y1 = (cy - bboxData[idx][1] * stride) * scaleY;
                  final float x2 = (cx + bboxData[idx][2] * stride) * scaleX;
                  final float y2 = (cy + bboxData[idx][3] * stride) * scaleY;
                  final int bw = Math.max(1, Math.round(x2 - x1));
                  final int bh = Math.max(1, Math.round(y2 - y1));
                  if (bw >= MIN_FACE_SIZE && bh >= MIN_FACE_SIZE) {
                    boxes.add(new Rect(Math.max(0, Math.round(x1)), Math.max(0, Math.round(y1)), bw, bh));
                    scores.add(score);
                  }
                }
                idx++;
              }
            }
          }
        }
      }
    }
    return AntelopeUtil.applyNMS(boxes, scores);
  }

  /**
   * Detects and recognizes a face in the full frame in one call.
   * Returns the matched {@link User} or {@code null}.
   */
  public User recognizeUser(final Mat face) throws Exception {
    final float[] embedding = extractEmbedding(face);
    final List<UserFace> allFaces = userFaceRepository.findAll();

    User bestUser = null;
    double bestSimilarity = SIMILARITY_THRESHOLD;
    for (final UserFace userFace : allFaces) {
      final User candidate = userFace.getUser();
      if (candidate == null || !candidate.isFaceRecognitionEnabled())
        continue;
      final float[] stored = AntelopeUtil.bytesToFloats(userFace.getEmbedding());
      final double sim = AntelopeUtil.cosineSimilarity(embedding, stored);
      if (sim > bestSimilarity) {
        bestSimilarity = sim;
        bestUser = candidate;
      }
    }
    return bestUser;
  }

  /** Extracts a 512-d L2-normalized ArcFace embedding from a face crop. */
  public float[] extractEmbedding(final Mat face) throws Exception {
    final Mat aligned = AntelopeUtil.recognitionPreprocess(face);
    final float[][][][] tensorData = AntelopeUtil.recognitionMatToTensor(aligned);
    try (OnnxTensor tensor = OnnxTensor.createTensor(env, tensorData)) {
      final OrtSession.Result result;
      synchronized (recognitionSession) {
        result = recognitionSession.run(Collections.singletonMap(recognitionInputName, tensor));
      }
      try (result) {
        final float[][] output = (float[][]) result.get(0).getValue();
        return AntelopeUtil.l2Normalize(output[0]);
      }
    }
  }

  public void close() throws OrtException {
    detectionSession.close();
    recognitionSession.close();
  }
} // AntelopeFaceService final class
