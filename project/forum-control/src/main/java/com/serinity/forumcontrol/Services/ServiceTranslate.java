package com.serinity.forumcontrol.Services;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ServiceTranslate {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String API_KEY = dotenv.get("API_Translate");

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public boolean isApiConfigured() {
        return API_KEY != null && !API_KEY.isEmpty();
    }

    public String translateText(String text, String targetLanguage) throws Exception {

        String prompt = "Translate the following text to " + targetLanguage +
                ". Only return the translated text:\n\n" + text;

        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();

        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();

        JSONObject part = new JSONObject();
        part.put("text", prompt);

        parts.put(part);
        content.put("parts", parts);
        contents.put(content);

        body.put("contents", contents);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        Scanner scanner;
        if (responseCode >= 200 && responseCode < 300) {
            scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
        } else {
            scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8);
        }

        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        if (responseCode >= 200 && responseCode < 300) {
            JSONObject json = new JSONObject(response);

            return json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        } else {
            throw new RuntimeException("API Error: " + response);
        }
    }
}