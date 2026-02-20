package com.serinity.forumcontrol.Services;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ServiceImgBB {

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";

    private static final String API_KEY = "77a7c45139ecec874f7d237e00102014";

    public String uploadImage(File imageFile) throws IOException {
        if (API_KEY == null ) {
            throw new IOException("ImgBB API key not configured");
        }

        byte[] fileContent = java.nio.file.Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);

        return uploadBase64Image(base64Image, imageFile.getName());
    }

    public String uploadImage(byte[] imageBytes, String filename) throws IOException {
        if (API_KEY == null ) {
            throw new IOException("ImgBB API key not configured");
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return uploadBase64Image(base64Image, filename);
    }

    private String uploadBase64Image(String base64Image, String filename) throws IOException {
        String params = "key=" + URLEncoder.encode(API_KEY, StandardCharsets.UTF_8) +
                "&image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8) +
                "&name=" + URLEncoder.encode(filename, StandardCharsets.UTF_8);

        URL url = new URL(IMGBB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(conn.getInputStream());
            return parseImageUrl(response);
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new IOException("ImgBB upload failed: " + error);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String parseImageUrl(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);

        if (json.getBoolean("success")) {
            JSONObject data = json.getJSONObject("data");
            return data.getString("display_url");
        }

        return null;
    }

    public boolean isValidImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        long maxSize = 32 * 1024 * 1024;
        if (file.length() > maxSize) {
            return false;
        }

        String filename = file.getName().toLowerCase();
        return filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".png") ||
                filename.endsWith(".gif") ||
                filename.endsWith(".bmp") ||
                filename.endsWith(".webp");
    }
     public boolean isApiKeyConfigured() {
         return API_KEY != null ;
     }
}