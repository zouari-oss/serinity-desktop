package com.serinity.exercicecontrol.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API publique (sans clé): Radio Browser
 * Objectif: récupérer des streams "doux" (relax / meditation / sleep) et éviter dance/club/house.
 */
public class AmbientSoundApiService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // ✅ Base fiable + fallback si un serveur est down
    private static final String[] BASES = new String[] {
            "https://all.api.radio-browser.info",
            "https://de1.api.radio-browser.info",
            "https://fr1.api.radio-browser.info",
            "https://nl1.api.radio-browser.info"
    };

    // Ex: query = "relax" / "meditation" / "sleep"
    public List<Station> searchStations(String query, int limit) {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);

        RuntimeException last = null;

        for (String base : BASES) {
            String url = base + "/json/stations/search?name=" + q + "&limit=" + limit;

            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("User-Agent", "SerinityApp/1.0 (JavaFX)")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() != 200) {
                    throw new RuntimeException("RadioBrowser HTTP " + res.statusCode() + " (" + base + ")");
                }

                // ✅ Même si parsing renvoie vide, on a un résultat “valide”
                return parseStations(res.body());

            } catch (Exception e) {
                last = new RuntimeException("Erreur RadioBrowser sur " + base + ": " + e.getMessage(), e);
                // on tente le serveur suivant
            }
        }

        // Si tous échouent
        throw (last != null) ? last : new RuntimeException("Erreur RadioBrowser inconnue.");
    }

    /**
     * Parsing léger, mais plus sûr que (mn.find() && mu.find()) :
     * - on découpe par objet JSON { ... } puis on extrait name/url_resolved dans le même objet.
     */
    private List<Station> parseStations(String json) {
        List<Station> out = new ArrayList<>();
        if (json == null || json.isBlank()) return out;

        Pattern objP  = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
        Pattern nameP = Pattern.compile("\"name\"\\s*:\\s*\"(.*?)\"");
        Pattern urlP  = Pattern.compile("\"url_resolved\"\\s*:\\s*\"(.*?)\"");

        Matcher mo = objP.matcher(json);
        while (mo.find()) {
            String obj = mo.group(1);

            String name = null;
            String url = null;

            Matcher mn = nameP.matcher(obj);
            if (mn.find()) name = unescape(mn.group(1));

            Matcher mu = urlP.matcher(obj);
            if (mu.find()) url = unescape(mu.group(1));

            if (url == null || url.isBlank()) continue;

            String lower = (name == null) ? "" : name.toLowerCase();

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
                out.add(new Station(name == null ? "Station" : name, url));
            }
        }

        return out;
    }

    private String unescape(String s) {
        if (s == null) return null;
        return s.replace("\\/", "/")
                .replace("\\\"", "\"")
                .replace("\\n", " ")
                .replace("\\t", " ")
                .trim();
    }

    public record Station(String name, String streamUrl) {}
}