package com.serinity.consultationcontrol.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class MedicalAIService {

    private static final String DEFAULT_API_URL = "http://127.0.0.2:8000/predict";
    private static final String ALT_LOCAL_API_URL = "http://127.0.0.1:8000/predict";
    private static final String ENV_API_URL = "SERINITY_CONSULTATION_AI_URL";
    private static final String PROP_API_URL = "serinity.consultation.ai.url";

    private static String normalizeApiUrl(String raw) {
        if (raw == null) {
            return null;
        }

        String url = raw.trim();
        if (url.isEmpty()) {
            return null;
        }

        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (!url.endsWith("/predict")) {
            url = url + "/predict";
        }

        return url;
    }

    private static List<String> candidateApiUrls() {
        LinkedHashSet<String> urls = new LinkedHashSet<>();

        String fromProp = normalizeApiUrl(System.getProperty(PROP_API_URL));
        if (fromProp != null) {
            urls.add(fromProp);
        }

        String fromEnv = normalizeApiUrl(System.getenv(ENV_API_URL));
        if (fromEnv != null) {
            urls.add(fromEnv);
        }

        urls.add(DEFAULT_API_URL);
        urls.add(ALT_LOCAL_API_URL);

        return new ArrayList<>(urls);
    }

    public static class AIResult {
        public String urgency;
        public String emotion;
        public String recommendation;
        public boolean fallbackUsed;
        public String errorMessage;
        public String resolvedApiUrl;
    }

    public static AIResult predict(String text) {
        Exception lastError = null;

        for (String apiUrl : candidateApiUrls()) {
            try {
                AIResult result = predictFromApi(text, apiUrl);
                result.fallbackUsed = false;
                result.errorMessage = null;
                result.resolvedApiUrl = apiUrl;
                return result;
            } catch (Exception e) {
                lastError = e;
            }
        }

        return predictOffline(text, lastError);
    }

    private static AIResult predictFromApi(String text, String apiUrl) throws Exception {
        AIResult result = new AIResult();
        HttpURLConnection conn = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            JSONObject body = new JSONObject();
            body.put("text", text);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                response = sb.toString();
            }

            if (status < 200 || status >= 300) {
                throw new RuntimeException("AI server returned HTTP " + status + ": " + response);
            }

            JSONObject json = new JSONObject(response);
            result.urgency = normalizeUrgency(json.optString("urgency", ""));
            result.emotion = json.optString("emotion", "Stable");
            result.recommendation = json.optString("recommendation", "");

            if ("UNKNOWN".equals(result.urgency)) {
                AIResult fallback = predictOffline(text, null);
                result.urgency = fallback.urgency;
                if (result.emotion == null || result.emotion.isBlank()) {
                    result.emotion = fallback.emotion;
                }
                if (result.recommendation == null || result.recommendation.isBlank()) {
                    result.recommendation = fallback.recommendation;
                }
            }

            if (result.emotion == null || result.emotion.isBlank()) {
                result.emotion = "Stable";
            }

            if (result.recommendation == null || result.recommendation.isBlank()) {
                result.recommendation = buildRecommendation(result.urgency, "Anxious".equalsIgnoreCase(result.emotion));
            }

            return result;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static boolean isServerReachable() {
        for (String apiUrl : candidateApiUrls()) {
            try {
                URL predictUrl = new URL(apiUrl);
                String healthUrl = predictUrl.getProtocol() + "://" + predictUrl.getHost() + ":" + predictUrl.getPort() + "/health";
                HttpURLConnection conn = (HttpURLConnection) new URL(healthUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(3000);
                int status = conn.getResponseCode();
                conn.disconnect();
                if (status >= 200 && status < 300) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static String preferredEndpointSummary() {
        return String.join("  |  ", candidateApiUrls());
    }

    private static String normalizeUrgency(String rawUrgency) {
        if (rawUrgency == null) {
            return "UNKNOWN";
        }

        String normalized = rawUrgency.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("HIGH")) {
            return "HIGH";
        }
        if (normalized.contains("MEDIUM")) {
            return "MEDIUM";
        }
        if (normalized.contains("LOW")) {
            return "LOW";
        }
        return "UNKNOWN";
    }

    private static AIResult predictOffline(String text, Exception cause) {
        AIResult result = new AIResult();
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);

        boolean high = containsAny(
                normalized,
                "chest pain", "douleur poitrine", "douleur thoracique",
                "can't breathe", "cannot breathe", "respire difficilement", "shortness of breath",
                "essoufflement", "fainted", "perte de connaissance", "convulsion",
                "stroke", "avc", "severe bleeding", "hemorragie", "hemorrhage");

        boolean medium = containsAny(
                normalized,
                "fever", "fievre", "vomiting", "vomissements",
                "infection", "dizziness", "vertige", "migraine",
                "persistent pain", "douleur persistante", "palpitations",
                "rash", "eruption");

        boolean anxious = containsAny(
                normalized,
                "stress", "anxious", "anxiety", "panic", "panique",
                "worried", "inquiet", "peur");

        result.urgency = high ? "HIGH" : (medium ? "MEDIUM" : "LOW");
        result.emotion = anxious ? "Anxious" : "Stable";
        result.recommendation = buildRecommendation(result.urgency, anxious);
        result.fallbackUsed = true;
        result.errorMessage = cause == null ? null : cause.getMessage();
        result.resolvedApiUrl = null;
        return result;
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String buildRecommendation(String urgency, boolean anxious) {
        String prefix = anxious
                ? "The text also suggests elevated stress. "
                : "";

        switch (urgency) {
            case "HIGH":
                return prefix + "The symptoms may be urgent. Seek medical care immediately or contact emergency services.";
            case "MEDIUM":
                return prefix + "A prompt medical consultation is recommended, ideally within the next 24 hours.";
            default:
                return prefix + "The symptoms appear lower priority. Rest, hydration, and close monitoring are recommended if the condition stays stable.";
        }
    }
}
