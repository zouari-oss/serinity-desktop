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

/**
 * API publique (sans clé): Radio Browser
 * Objectif: récupérer des streams "doux" (relax / meditation / sleep) et éviter dance/club/house.
 */
public class AmbientSoundApiService {

    private final HttpClient http = HttpClient.newHttpClient();

    // Ex: query = "relax" / "meditation" / "sleep"
    public List<Station> searchStations(String query, int limit) {
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
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

    // Parsing léger: on récupère name + url_resolved, puis on filtre pour ne garder que du "doux"
    private List<Station> parseStations(String json) {
        List<Station> out = new ArrayList<>();

        Pattern nameP = Pattern.compile("\"name\"\\s*:\\s*\"(.*?)\"");
        Pattern urlP  = Pattern.compile("\"url_resolved\"\\s*:\\s*\"(.*?)\"");

        Matcher mn = nameP.matcher(json);
        Matcher mu = urlP.matcher(json);

        while (mn.find() && mu.find()) {
            String name = unescape(mn.group(1));
            String url  = unescape(mu.group(1));

            if (url == null || url.isBlank()) continue;

            String lower = (name == null) ? "" : name.toLowerCase();

            // ✅ mots-clés "doux"
            boolean soft =
                    lower.contains("ambient")
                            || lower.contains("relax")
                            || lower.contains("relaxation")
                            || lower.contains("meditation")
                            || lower.contains("sleep")
                            || lower.contains("calm")
                            || lower.contains("piano")
                            || lower.contains("lounge")
                            || lower.contains("chillout")
                            || lower.contains("nature")
                            || lower.contains("zen");

            // ❌ mots-clés à exclure (souvent trop “énergique”)
            boolean hard =
                    lower.contains("house")
                            || lower.contains("deep")
                            || lower.contains("dance")
                            || lower.contains("club")
                            || lower.contains("party")
                            || lower.contains("electro")
                            || lower.contains("techno")
                            || lower.contains("trance");

            if (soft && !hard) {
                out.add(new Station(name, url));
            }
        }

        return out;
    }

    private String unescape(String s) {
        if (s == null) return null;
        return s.replace("\\/", "/").replace("\\\"", "\"");
    }

    public record Station(String name, String streamUrl) {}
}