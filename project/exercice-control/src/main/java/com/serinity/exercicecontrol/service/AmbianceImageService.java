package com.serinity.exercicecontrol.service;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AmbianceImageService {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    // ✅ Picsum (ultra fiable)
    private String buildUrl() {
        long seed = System.currentTimeMillis();
        return "https://picsum.photos/1600/400?random=" + seed;
    }

    public Image fetchImageBlocking() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "SerinityDesktop/1.0")
                .GET()
                .build();

        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200 || response.body() == null) {
            throw new RuntimeException("Image HTTP error");
        }

        return new Image(new ByteArrayInputStream(response.body()));
    }
}