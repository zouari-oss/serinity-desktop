// `FreeImageHostClient` package name
package com.serinity.accesscontrol.service;

// `java` import(s)
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;

// `serinity` import(s)
import com.serinity.accesscontrol.config.EnvironmentVariableLoader;

/**
 * Utility client responsible for uploading image files to
 * <a href="https://freeimage.host/">FreeImage.host</a> using its HTTP API.
 *
 * <p>
 * This class provides a centralized and static mechanism for sending
 * multipart/form-data HTTP POST requests to the FreeImage.host image upload
 * endpoint. The API key and request URL are loaded from environment variables
 * via {@link EnvironmentVariableLoader}.
 * </p>
 *
 * <p>
 * The client uses Java's {@link java.net.http.HttpClient} to construct and
 * execute HTTP requests. Images are uploaded as raw byte arrays within a
 * manually constructed multipart request body.
 * </p>
 *
 * <p>
 * Configuration:
 * </p>
 * <ul>
 * <li><b>IMAGE_API_KEY</b> – Retrieved from environment variables.</li>
 * <li><b>IMAGE_REQUEST_URL</b> – Retrieved from environment variables.</li>
 * </ul>
 *
 * <p>
 * Exposed functionality:
 * </p>
 * <ul>
 * <li>{@link #uploadImage(File)} – Uploads an image file and returns its public
 * URL.</li>
 * <li>{@link #extractImageUrl(String)} – Extracts the uploaded image URL from
 * the JSON response.</li>
 * </ul>
 *
 * <p>
 * The upload process:
 * </p>
 * <ol>
 * <li>Validates that the provided file exists.</li>
 * <li>Reads the file into memory as a byte array.</li>
 * <li>Constructs a multipart/form-data request body with a unique
 * boundary.</li>
 * <li>Sends the request to the FreeImage.host API using
 * {@link java.net.http.HttpClient}.</li>
 * <li>Parses the JSON response to extract the public image URL.</li>
 * </ol>
 *
 * <p>
 * The JSON parsing is performed manually using string operations and brace
 * matching to locate the <code>"image"</code> object and extract its
 * <code>"url"</code> field. Escape sequences (e.g., <code>\/</code>) are
 * cleaned
 * before returning the final URL.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * File image = new File("profile.png");
 * String imageUrl = FreeImageHostClient.uploadImage(image);
 * System.out.println("Uploaded image URL: " + imageUrl);
 * </pre>
 *
 * <p>
 * Exceptions:
 * </p>
 * <ul>
 * <li>{@link IllegalArgumentException} – If the file does not exist.</li>
 * <li>{@link IOException} – If an I/O error occurs during file reading or HTTP
 * communication.</li>
 * <li>{@link InterruptedException} – If the HTTP request is interrupted.</li>
 * <li>{@link IllegalStateException} – If the response JSON is malformed or does
 * not contain the expected fields.</li>
 * </ul>
 *
 * <p>
 * Note:
 * </p>
 * <ul>
 * <li>The entire file is loaded into memory before upload; large files may
 * impact memory usage.</li>
 * <li>Environment variables must be properly configured; otherwise, the upload
 * request will fail.</li>
 * <li>For production-grade systems, consider using a JSON parser library
 * instead of manual string parsing.</li>
 * </ul>
 *
 * @version 1.0
 * @since 2026-02-16
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @see EnvironmentVariableLoader
 * 
 *      <a
 *      href=
 *      "https://freeimage.host/api">
 *      API version 1
 *      </a>
 * 
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/service/FreeImageHostClient.java">
 *      FreeImageHostClient.java
 *      </a>
 */
public class FreeImageHostClient {

  private static final String IMAGE_API_KEY;
  private static final String IMAGE_REQUEST_URL;
  private static final HttpClient httpClient;

  static {
    IMAGE_API_KEY = EnvironmentVariableLoader.getImageApiKey();
    IMAGE_REQUEST_URL = EnvironmentVariableLoader.getImageRequestUrl();
    httpClient = HttpClient.newHttpClient();
  }

  /**
   * Uploads an image file to FreeImage.host and returns the public URL.
   */
  public static String uploadImage(File imageFile) throws IOException, InterruptedException {
    if (!imageFile.exists()) {
      throw new IllegalArgumentException("File does not exist: " + imageFile.getAbsolutePath());
    }

    byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
    String boundary = "Boundary-" + System.currentTimeMillis();
    String CRLF = "\r\n";

    // Build multipart header
    StringBuilder builder = new StringBuilder();
    builder.append("--").append(boundary).append(CRLF);
    builder.append("Content-Disposition: form-data; name=\"source\"; filename=\"")
        .append(imageFile.getName()).append("\"").append(CRLF);
    builder.append("Content-Type: application/octet-stream").append(CRLF).append(CRLF);

    byte[] headerBytes = builder.toString().getBytes();
    byte[] footerBytes = (CRLF + "--" + boundary + "--" + CRLF).getBytes();
    byte[] requestBody = new byte[headerBytes.length + fileBytes.length + footerBytes.length];

    System.arraycopy(headerBytes, 0, requestBody, 0, headerBytes.length);
    System.arraycopy(fileBytes, 0, requestBody, headerBytes.length, fileBytes.length);
    System.arraycopy(footerBytes, 0, requestBody, headerBytes.length + fileBytes.length, footerBytes.length);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(IMAGE_REQUEST_URL + "?key=" + IMAGE_API_KEY))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(BodyPublishers.ofByteArray(requestBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    return extractImageUrl(response.body());
  }

  /**
   * Extracts "url" field from FreeImage.host JSON response using regex.
   */
  public static String extractImageUrl(String jsonResponse) {
    int imageIndex = jsonResponse.indexOf("\"image\":{");
    if (imageIndex == -1) {
      throw new IllegalStateException("Image object not found in response: " + jsonResponse);
    }

    // Start from first { after `"image":`
    int braceStart = jsonResponse.indexOf("{", imageIndex);
    int braceCount = 0;
    int endIndex = -1;

    for (int i = braceStart; i < jsonResponse.length(); i++) {
      char c = jsonResponse.charAt(i);
      if (c == '{')
        braceCount++;
      else if (c == '}')
        braceCount--;
      if (braceCount == 0) {
        endIndex = i;
        break;
      }
    }

    if (endIndex == -1)
      throw new IllegalStateException("Malformed JSON: " + jsonResponse);

    String imageJson = jsonResponse.substring(braceStart, endIndex + 1);

    // Look for top-level "url"
    int urlIndex = imageJson.indexOf("\"url\":\"");
    if (urlIndex == -1)
      throw new IllegalStateException("Image URL not found in response: " + jsonResponse);

    int urlStart = urlIndex + "\"url\":\"".length();
    int urlEnd = imageJson.indexOf("\"", urlStart);
    String url = imageJson.substring(urlStart, urlEnd);

    // Clean escape sequences
    return url.replace("\\/", "/");
  }
} // `FreeImageHostClient` final class
