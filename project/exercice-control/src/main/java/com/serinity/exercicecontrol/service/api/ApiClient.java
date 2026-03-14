package com.serinity.exercicecontrol.service.api;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

  /** Class documentation. */
public class ApiClient {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

  /** Documents get. */
    public String get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET();

        if (headers != null) headers.forEach(b::header);

        HttpResponse<String> res = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " GET " + url + "\n" + res.body());
        }
        return res.body();
    }

  /** Documents postForm. */
    public String postForm(String url, String formUrlEncoded, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formUrlEncoded));

        if (headers != null) headers.forEach(b::header);

        HttpResponse<String> res = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " POST " + url + "\n" + res.body());
        }
        return res.body();
    }

  /** Documents postJson. */
    public String postJson(String url, String jsonBody, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (headers != null) headers.forEach(b::header);

        HttpResponse<String> res = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " POST " + url + "\n" + res.body());
        }
        return res.body();
    }
}
