package com.serinity.forumcontrol.Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Moderate {

    private static final String BASE_URL = "https://www.purgomalum.com/service/containsprofanity?text=";

    public boolean isToxic(String text) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + encodedText))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());

            // API returns "true" or "false"
            return Boolean.parseBoolean(response.body());

        } catch (Exception e) {
            System.err.println("PurgoMalum moderation error: " + e.getMessage());
            return false;
        }
    }
}