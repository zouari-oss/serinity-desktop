package com.serinity.accesscontrol.service.exercice.api;

import com.google.gson.*;
import com.serinity.accesscontrol.model.exercice.api.VideoSuggestion;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class YouTubeApiService {
    private final ApiClient client = new ApiClient();
    private final Gson gson = new Gson();

    // ✅ FIX: on lit la clé depuis .env ou variable d’environnement
    private final String apiKey = EnvConfig.require("YOUTUBE_API_KEY");

    public List<VideoSuggestion> searchMeditationOrYoga(String query, int maxResults) throws Exception {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url =
                "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet&type=video&maxResults=" + maxResults
                        + "&q=" + q
                        + "&key=" + apiKey;

        String json = client.get(url, null);
        JsonObject root = gson.fromJson(json, JsonObject.class);
        JsonArray items = root.getAsJsonArray("items");

        // 1) collect ids + snippet
        List<Temp> tmp = new ArrayList<>();
        for (JsonElement el : items) {
            JsonObject item = el.getAsJsonObject();
            JsonObject idObj = item.getAsJsonObject("id");
            if (idObj == null || !idObj.has("videoId")) continue;

            String videoId = idObj.get("videoId").getAsString();
            JsonObject snip = item.getAsJsonObject("snippet");
            String title = snip.get("title").getAsString();
            String channelTitle = snip.get("channelTitle").getAsString();

            String thumb = "";
            if (snip.has("thumbnails")) {
                JsonObject thumbs = snip.getAsJsonObject("thumbnails");
                thumb = pickThumb(thumbs);
            }

            tmp.add(new Temp(videoId, title, channelTitle, thumb));
        }

        if (tmp.isEmpty()) return List.of();

        // 2) fetch durations
        Map<String, Integer> durations = fetchDurationsSeconds(tmp.stream().map(t -> t.videoId).toList());

        // 3) build result
        List<VideoSuggestion> out = new ArrayList<>();
        for (Temp t : tmp) {
            int dur = durations.getOrDefault(t.videoId, 0);
            out.add(new VideoSuggestion(t.videoId, t.title, t.channelTitle, t.thumbnailUrl, dur));
        }
        return out;
    }

    // Filter helper: pick videos between min and max duration
    public List<VideoSuggestion> filterByDuration(List<VideoSuggestion> vids, int minSec, int maxSec) {
        return vids.stream()
                .filter(v -> v.durationSeconds() >= minSec && v.durationSeconds() <= maxSec)
                .toList();
    }

    private Map<String, Integer> fetchDurationsSeconds(List<String> ids) throws Exception {
        String joined = ids.stream().collect(Collectors.joining(","));
        String url =
                "https://www.googleapis.com/youtube/v3/videos"
                        + "?part=contentDetails&id=" + joined
                        + "&key=" + apiKey;

        String json = client.get(url, null);
        JsonObject root = gson.fromJson(json, JsonObject.class);
        JsonArray items = root.getAsJsonArray("items");

        Map<String, Integer> out = new HashMap<>();
        for (JsonElement el : items) {
            JsonObject item = el.getAsJsonObject();
            String id = item.get("id").getAsString();
            JsonObject cd = item.getAsJsonObject("contentDetails");
            String iso = cd.get("duration").getAsString(); // ex: PT5M30S
            out.put(id, parseIso8601DurationSeconds(iso));
        }
        return out;
    }

    private static int parseIso8601DurationSeconds(String iso) {
        // format: PT#H#M#S (simple parser)
        int h = 0, m = 0, s = 0;
        String t = iso.replace("PT", "");
        String num = "";
        for (char c : t.toCharArray()) {
            if (Character.isDigit(c)) num += c;
            else {
                int v = num.isEmpty() ? 0 : Integer.parseInt(num);
                if (c == 'H') h = v;
                if (c == 'M') m = v;
                if (c == 'S') s = v;
                num = "";
            }
        }
        return h * 3600 + m * 60 + s;
    }

    private static String pickThumb(JsonObject thumbs) {
        // prefer high > medium > default
        if (thumbs.has("high")) return thumbs.getAsJsonObject("high").get("url").getAsString();
        if (thumbs.has("medium")) return thumbs.getAsJsonObject("medium").get("url").getAsString();
        if (thumbs.has("default")) return thumbs.getAsJsonObject("default").get("url").getAsString();
        return "";
    }

    private record Temp(String videoId, String title, String channelTitle, String thumbnailUrl) {}
}