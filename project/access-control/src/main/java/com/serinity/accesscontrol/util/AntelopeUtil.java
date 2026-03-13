// `AntelopeUtil` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// `opencv` import(s)
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

// `serinity` import(s)
import com.serinity.accesscontrol.service.AntelopeFaceService;

// `onnxruntime` import(s)
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * Utility class providing low-level helpers for the AntelopeV2 face
 * detection and recognition pipeline.
 *
 * <p>
 * Covers the following responsibilities:
 * </p>
 * <ul>
 * <li>Converting OpenCV {@link org.opencv.core.Mat} frames to ONNX input
 * tensors for SCRFD (detection) and ArcFace (recognition).</li>
 * <li>L2-normalization and cosine-similarity of embedding vectors.</li>
 * <li>Serialization / deserialization of {@code float[]} embeddings to
 * {@code byte[]} for database storage.</li>
 * <li>Loading ONNX model sessions from classpath resources.</li>
 * <li>Applying Non-Maximum Suppression (NMS) to raw bounding-box outputs.</li>
 * </ul>
 *
 * <p>
 * NOTE: This class is {@code final} and cannot be instantiated.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 * @see com.serinity.accesscontrol.service.AntelopeFaceService
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/AntelopeUtil.java">
 *      AntelopeUtil.java
 *      </a>
 */
public final class AntelopeUtil {
  // SCRFD constants
  private static final float NMS_THRESHOLD = 0.4f;

  // Arcface constants
  private static final int RECOGNITION_INPUT_SIZE = 112;

  /**
   * Converts an OpenCV BGR {@link Mat} to a 4-D SCRFD detection input tensor
   * with pixel values normalized to the range {@code [-1, 1]}.
   *
   * @param mat the BGR image (must be the SCRFD input size, i.e. 640×640)
   * @return tensor of shape {@code [1][3][H][W]} with R, G, B channel order
   */
  public static float[][][][] detectionMatToTensor(final Mat mat) {
    final int h = mat.rows(), w = mat.cols();
    final float[][][][] t = new float[1][3][h][w];
    // Read all pixels in one call, 30-50× faster than per-pixel mat.get(y,x)
    final byte[] buf = new byte[h * w * 3];
    mat.get(0, 0, buf);
    int idx = 0;
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x) {
        t[0][0][y][x] = ((buf[idx + 2] & 0xFF) - 127.5f) / 128.0f; // R
        t[0][1][y][x] = ((buf[idx + 1] & 0xFF) - 127.5f) / 128.0f; // G
        t[0][2][y][x] = ((buf[idx] & 0xFF) - 127.5f) / 128.0f; // B
        idx += 3;
      }
    return t;
  }

  /**
   * Preprocesses a face crop for the ArcFace recognition model:
   * converts BGR to RGB and resizes to 112×112.
   *
   * @param face the cropped face region as a BGR {@link Mat}
   * @return RGB {@link Mat} resized to 112×112
   */
  public static Mat recognitionPreprocess(final Mat face) {
    final Mat rgb = new Mat();
    Imgproc.cvtColor(face, rgb, Imgproc.COLOR_BGR2RGB);
    final Mat resized = new Mat();
    Imgproc.resize(rgb, resized, new Size(RECOGNITION_INPUT_SIZE, RECOGNITION_INPUT_SIZE));
    return resized;
  }

  /**
   * Converts a preprocessed (RGB, 112×112) {@link Mat} to a 4-D ArcFace
   * input tensor with pixel values normalized to {@code [-1, 1]}.
   *
   * @param mat the RGB 112×112 face image
   * @return tensor of shape {@code [1][3][112][112]}
   */
  public static float[][][][] recognitionMatToTensor(final Mat mat) {
    final int h = mat.rows(), w = mat.cols();
    final float[][][][] t = new float[1][3][h][w];
    final byte[] buf = new byte[h * w * 3];
    mat.get(0, 0, buf);
    int idx = 0;
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x) {
        t[0][0][y][x] = ((buf[idx] & 0xFF) - 127.5f) / 128.0f;
        t[0][1][y][x] = ((buf[idx + 1] & 0xFF) - 127.5f) / 128.0f;
        t[0][2][y][x] = ((buf[idx + 2] & 0xFF) - 127.5f) / 128.0f;
        idx += 3;
      }
    return t;
  }

  /**
   * L2-normalizes a float vector in-place.
   *
   * @param v the input vector
   * @return a new array containing the L2-normalized values
   */
  public static float[] l2Normalize(final float[] v) {
    double sum = 0.0;
    for (final float f : v)
      sum += f * f;
    final double norm = Math.sqrt(sum);
    final float[] out = new float[v.length];
    for (int i = 0; i < v.length; i++)
      out[i] = (float) (v[i] / norm);
    return out;
  }

  /**
   * Computes the cosine similarity between two embedding vectors.
   *
   * @param a first embedding vector
   * @param b second embedding vector
   * @return similarity score in the range {@code [-1, 1]}; higher is more similar
   */
  public static double cosineSimilarity(final float[] a, final float[] b) {
    double dot = 0, nA = 0, nB = 0;
    for (int i = 0; i < a.length; i++) {
      dot += a[i] * b[i];
      nA += a[i] * a[i];
      nB += b[i] * b[i];
    }
    return dot / (Math.sqrt(nA) * Math.sqrt(nB));
  }

  /**
   * Loads an ONNX model from the classpath into an {@link OrtSession}.
   * The model is copied to a temporary file before loading.
   *
   * @param env          the ONNX Runtime environment
   * @param resourcePath classpath path to the {@code .onnx} model file
   * @return a new {@link OrtSession} backed by the model
   * @throws IOException  if the resource stream cannot be read
   * @throws OrtException if the ONNX Runtime fails to create the session
   */
  public static OrtSession loadSession(final OrtEnvironment env, final String resourcePath)
      throws IOException, OrtException {
    final InputStream stream = AntelopeFaceService.class.getResourceAsStream("/" + resourcePath);
    if (stream == null)
      throw new RuntimeException("Model not found: " + resourcePath);

    final Path temp = Files.createTempFile("model_", ".onnx");
    Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
    temp.toFile().deleteOnExit();
    return env.createSession(temp.toAbsolutePath().toString());
  }

  /**
   * Serializes a {@code float[]} embedding to a little-endian {@code byte[]}.
   *
   * @param floats the float array to serialize
   * @return byte representation suitable for database storage
   */
  public static byte[] floatsToBytes(final float[] floats) {
    final ByteBuffer buf = ByteBuffer
        .allocate(Float.BYTES * floats.length)
        .order(ByteOrder.LITTLE_ENDIAN);

    for (final float f : floats)
      buf.putFloat(f);

    return buf.array();
  }

  /**
   * Deserializes a little-endian {@code byte[]} back to a {@code float[]}
   * embedding.
   *
   * @param bytes the raw bytes to deserialize
   * @return the reconstructed float array
   */
  public static float[] bytesToFloats(final byte[] bytes) {
    final ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    final float[] floats = new float[bytes.length / Float.BYTES];

    for (int i = 0; i < floats.length; ++i)
      floats[i] = buf.getFloat();

    return floats;
  }

  /**
   * Applies Non-Maximum Suppression to a list of bounding boxes.
   * Boxes with IoU above {@code NMS_THRESHOLD} relative to a higher-scored
   * box are suppressed.
   *
   * @param boxes  candidate bounding boxes
   * @param scores corresponding confidence scores
   * @return filtered list of kept bounding boxes
   */
  public static List<Rect> applyNMS(final List<Rect> boxes, final List<Float> scores) {
    if (boxes.isEmpty())
      return boxes;
    final List<Integer> indices = IntStream.range(0, boxes.size()).boxed()
        .sorted((a, b) -> Float.compare(scores.get(b), scores.get(a)))
        .collect(Collectors.toList());
    final boolean[] suppressed = new boolean[boxes.size()];
    final List<Rect> kept = new ArrayList<>();
    for (final int i : indices) {
      if (suppressed[i])
        continue;
      kept.add(boxes.get(i));
      for (final int j : indices) {
        if (!suppressed[j] && j != i && iou(boxes.get(i), boxes.get(j)) > NMS_THRESHOLD)
          suppressed[j] = true;
      }
    }
    return kept;
  }

  /**
   * Computes the Intersection-over-Union (IoU) ratio between two rectangles.
   *
   * @param a first rectangle
   * @param b second rectangle
   * @return IoU value in the range {@code [0, 1]}
   */
  public static float iou(final Rect a, final Rect b) {
    final int x1 = Math.max(a.x, b.x), y1 = Math.max(a.y, b.y);
    final int x2 = Math.min(a.x + a.width, b.x + b.width);
    final int y2 = Math.min(a.y + a.height, b.y + b.height);
    if (x2 <= x1 || y2 <= y1)
      return 0f;
    final float inter = (x2 - x1) * (y2 - y1);
    return inter / (a.width * a.height + b.width * b.height - inter);
  }
} // `AntelopeUtil` final class
