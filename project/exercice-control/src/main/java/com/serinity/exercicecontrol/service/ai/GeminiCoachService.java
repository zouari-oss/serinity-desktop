package com.serinity.exercicecontrol.service.ai;

import com.google.gson.*;
import com.serinity.exercicecontrol.service.performance.PerformanceReport;
import com.serinity.exercicecontrol.util.Env;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class GeminiCoachService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public CoachAnswer callCoach(PerformanceReport report) throws Exception {

        String apiKey = Env.get("GEMINI_API_KEY"); // ✅ le NOM de la variable
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Missing config: GEMINI_API_KEY (loaded from " + Env.debugWhereLoaded() + ")"
            );
        }

        String model = Env.getOrDefault("GEMINI_MODEL", "gemini-2.5-flash");

        String url = "https://generativelanguage.googleapis.com/v1/models/"
                + encode(model) + ":generateContent?key=" + encode(apiKey);

        String prompt = buildPrompt(report);

        JsonObject body = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject one = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject p = new JsonObject();
        p.addProperty("text", prompt);
        parts.add(p);
        one.add("parts", parts);
        contents.add(one);
        body.add("contents", contents);

        JsonObject genCfg = new JsonObject();
        genCfg.addProperty("temperature", 0.4);
        body.add("generationConfig", genCfg);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(45))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (res.statusCode() >= 300) {
            throw new RuntimeException("Gemini HTTP " + res.statusCode() + " => " + res.body());
        }

        String text = extractText(res.body());
        return parseCoachAnswer(text);
    }

    // -------- helpers --------

    private static String buildPrompt(PerformanceReport r) {
        return """
Tu es un coach sportif bienveillant. Donne des conseils courts, concrets, et sûrs.

Données:
- status=%s
- trendDeltaPercent=%.1f
- sessions=%d/%d
- avgActiveSeconds=%d
- fatigueFlag=%s
- painFlag=%s
- nextSessionPlan=%s

Réponds en JSON EXACT suivant ce schéma:
{
 "summary": "1-2 phrases",
 "actions": ["...", "...", "..."],
 "nextSession": {"warmup":"...", "main":"...", "cooldown":"..."},
 "safetyNote":"..."
}
"""
                .formatted(
                        r.status(),
                        r.trendDeltaPercent(),
                        r.sessionsCompleted(), r.sessionsTotal(),
                        r.avgActiveSeconds(),
                        r.fatigueFlag(), r.painFlag(),
                        r.nextSessionPlan() == null ? "" : r.nextSessionPlan().objective()
                );
    }

    private static String extractText(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) return "";
        JsonObject c0 = candidates.get(0).getAsJsonObject();
        JsonObject content = c0.getAsJsonObject("content");
        if (content == null) return "";
        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) return "";
        JsonObject p0 = parts.get(0).getAsJsonObject();
        return p0.has("text") ? p0.get("text").getAsString() : "";
    }

    private static CoachAnswer parseCoachAnswer(String text) {
        String trimmed = text == null ? "" : text.trim();
        int a = trimmed.indexOf('{');
        int b = trimmed.lastIndexOf('}');
        if (a >= 0 && b > a) trimmed = trimmed.substring(a, b + 1);

        JsonObject o = JsonParser.parseString(trimmed).getAsJsonObject();
        String summary = o.has("summary") ? o.get("summary").getAsString() : "—";

        List<String> actions = new java.util.ArrayList<>();
        if (o.has("actions") && o.get("actions").isJsonArray()) {
            for (var el : o.getAsJsonArray("actions")) actions.add(el.getAsString());
        }

        JsonObject ns = o.has("nextSession") ? o.getAsJsonObject("nextSession") : new JsonObject();
        CoachAnswer.NextSession next = new CoachAnswer.NextSession(
                ns.has("warmup") ? ns.get("warmup").getAsString() : "",
                ns.has("main") ? ns.get("main").getAsString() : "",
                ns.has("cooldown") ? ns.get("cooldown").getAsString() : ""
        );

        String safety = o.has("safetyNote") ? o.get("safetyNote").getAsString() : "";

        return new CoachAnswer(summary, actions, next, safety);
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}