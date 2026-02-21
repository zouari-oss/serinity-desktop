package com.serinity.forumcontrol.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class Moderate {

    private static final String API_KEY = "AIzaSyCqxmJu5nIF2_YzklnSew_5fMXw9r-Ihfg";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public boolean isToxic(String text) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = """
                You are a content moderation system. Analyze the following text and determine if it is toxic, hateful, insulting, or harmful.
                Reply with ONLY a valid JSON object in this exact format, nothing else:
                {"toxic": true, "reason": "brief reason"}
                or
                {"toxic": false, "reason": "brief reason"}
                
                Text to analyze: "%s"
                """.formatted(text);

            JSONObject requestBody = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("text", prompt)))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Raw: " + response.body());

            // Extract the text from Gemini's response
            JSONObject result = new JSONObject(response.body());
            System.out.println("Full response: " + result.toString(2));
            String geminiText = result
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();

            System.out.println("Gemini verdict: " + geminiText);

            // Parse Gemini's JSON reply
            JSONObject verdict = new JSONObject(geminiText);
            boolean toxic = verdict.getBoolean("toxic");
            String reason = verdict.getString("reason");

            System.out.println("Toxic: " + toxic + " | Reason: " + reason);

            return toxic;

        } catch (Exception e) {
            System.err.println("Gemini moderation error: " + e.getMessage());
            return false;
        }
    }
}