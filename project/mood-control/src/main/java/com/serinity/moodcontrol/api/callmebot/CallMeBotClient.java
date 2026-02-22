package com.serinity.moodcontrol.api.callmebot;

import com.serinity.moodcontrol.api.http.ApiClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CallMeBotClient extends ApiClient {

    private static final String BASE_URL = "https://api.callmebot.com/whatsapp.php";

    public CallMeBotClient() {
        super();
    }

    public void sendWhatsAppMessage(String phone, String apiKey, String message) throws Exception {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("phone is required");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("apiKey is required");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message is required");
        }


        String normalizedPhone = phone.trim().replace(" ", "");

        String url = BASE_URL
                + "?phone=" + normalizedPhone
                + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                + "&apikey=" + URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("CallMeBot HTTP " + response.statusCode() + " - " + response.body());
        }

        // validation
        String body = response.body() == null ? "" : response.body().toLowerCase();
        if (body.contains("error")) {
            throw new IllegalStateException("CallMeBot error response: " + response.body());
        }
    }
}