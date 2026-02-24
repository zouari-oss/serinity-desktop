package com.serinity.exercicecontrol.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmbientSoundApiService {

    private final HttpClient http = HttpClient.newHttpClient();

    public List<Station> searchStations(String query, int limit) {
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // Endpoint simple + public
            String url = "https://de1.api.radio-browser.info/json/stations/search?name=" + q + "&limit=" + limit;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new RuntimeException("RadioBrowser HTTP " + res.statusCode());
            }

            return parseStations(res.body());

        } catch (Exception e) {
            throw new RuntimeException("Erreur RadioBrowser API: " + e.getMessage(), e);
        }
    }

    private List<Station> parseStations(String json) {
        List<Station> out = new ArrayList<>();

        Pattern nameP = Pattern.compile("\"name\"\\s*:\\s*\"(.*?)\"");
        Pattern urlP  = Pattern.compile("\"url_resolved\"\\s*:\\s*\"(.*?)\"");

        Matcher mn = nameP.matcher(json);
        Matcher mu = urlP.matcher(json);

        while (mn.find() && mu.find()) {
            String name = unescape(mn.group(1));
            String url  = unescape(mu.group(1));

            // ignore vides
            if (url != null && !url.isBlank()) {
                out.add(new Station(name, url));
            }
        }
        return out;
    }

    private String unescape(String s) {
        return s.replace("\\/", "/").replace("\\\"", "\"");
    }

    public record Station(String name, String streamUrl) {}
}