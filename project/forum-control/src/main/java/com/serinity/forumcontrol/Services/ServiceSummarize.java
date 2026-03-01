package com.serinity.forumcontrol.Services;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Local T5 Summarization Service
 * Calls a local Python server running T5-small model
 *
 * ADVANTAGES:
 * - Completely free (no API costs)
 * - No rate limits
 * - Works offline (after model download)
 * - Fast (runs on your machine)
 * - Private (data never leaves your computer)
 */
public class ServiceSummarize {

    // Local Python server endpoint
    private static final String LOCAL_API_URL = "http://localhost:5000/summarize";
    private static final String HEALTH_CHECK_URL = "http://localhost:5000/health";

    /**
     * Check if the local T5 service is running
     */
    public boolean isServiceRunning() {
        try {
            URL url = new URL(HEALTH_CHECK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate summary using local T5 model
     *
     * @param text The text to summarize
     * @return Summarized text
     * @throws IOException if service is not running or request fails
     */
    public String summarizeText(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Check if service is running
        if (!isServiceRunning()) {
            throw new IOException(
                    "Local T5 service is not running!\n" +
                            "Please start it by running: python t5_service.py"
            );
        }

        text = text.trim();

        // If text is too short, return as-is
        if (text.length() < 200) {
            return text;
        }

        // Truncate if too long (T5-small handles up to ~2000 chars well)
        if (text.length() > 2000) {
            text = text.substring(0, 2000);
        }

        // Build request
        JSONObject payload = new JSONObject();
        payload.put("text", text);
        payload.put("max_length", 130);
        payload.put("min_length", 30);

        System.out.println("🔄 Sending to local T5 service...");

        // Create connection to local server
        URL url = new URL(LOCAL_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(30000);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        System.out.println("📥 Response code: " + responseCode);

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            System.out.println("✅ Summary received!");
            return parseSummary(response);
        } else {
            String error = readResponse(conn.getErrorStream());
            System.err.println("❌ Error: " + error);
            throw new IOException("Summarization failed: " + error);
        }
    }

    /**
     * Generate summary with custom length
     */
    public String summarizeText(String text, int maxLength, int minLength) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        if (!isServiceRunning()) {
            throw new IOException("Local T5 service is not running!");
        }

        text = text.trim();

        if (text.length() < 200) {
            return text;
        }

        if (text.length() > 2000) {
            text = text.substring(0, 2000);
        }

        JSONObject payload = new JSONObject();
        payload.put("text", text);
        payload.put("max_length", maxLength);
        payload.put("min_length", minLength);

        URL url = new URL(LOCAL_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            return parseSummary(response);
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new IOException("Summarization failed: " + error);
        }
    }

    /**
     * Read response from input stream
     */
    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * Parse JSON response and extract summary
     * Response format: {"summary": "...", "input_length": 123, "output_length": 45}
     */
    private String parseSummary(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            if (jsonObject.has("summary")) {
                return jsonObject.getString("summary");
            }

            System.err.println("⚠️ No 'summary' field in response");

        } catch (Exception e) {
            System.err.println("❌ Parse error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if service is configured and ready
     */
    public boolean isApiConfigured() {
        return isServiceRunning();
    }

    /**
     * Quick summary (shorter)
     */
    public String quickSummary(String text) throws IOException {
        return summarizeText(text, 80, 20);
    }

    /**
     * Detailed summary (longer)
     */
    public String detailedSummary(String text) throws IOException {
        return summarizeText(text, 150, 40);
    }
}