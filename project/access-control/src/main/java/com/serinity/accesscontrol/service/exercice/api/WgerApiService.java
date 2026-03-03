package com.serinity.accesscontrol.service.exercice.api;

import com.google.gson.*;
import com.serinity.accesscontrol.model.exercice.api.ExerciseSuggestion;

import java.util.ArrayList;
import java.util.List;

public class WgerApiService {
    private final ApiClient client = new ApiClient();
    private final Gson gson = new Gson();

    // language=2 غالبًا EN; يمكنك تبدّلها حسب احتياجك
    public List<ExerciseSuggestion> fetchExercises(int limit, int offset) throws Exception {
        String url = "https://wger.de/api/v2/exercise/?language=2&limit=" + limit + "&offset=" + offset;
        String json = client.get(url, null);

        JsonObject root = gson.fromJson(json, JsonObject.class);
        JsonArray results = root.getAsJsonArray("results");

        List<ExerciseSuggestion> out = new ArrayList<>();
        for (JsonElement el : results) {
            JsonObject o = el.getAsJsonObject();
            int id = o.get("id").getAsInt();
            String name = safeStr(o, "name");
            String desc = safeStr(o, "description"); // غالبًا فيه HTML
            if (name == null || name.isBlank()) continue;
            out.add(new ExerciseSuggestion(id, name, desc));
        }
        return out;
    }

    private static String safeStr(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "";
        return o.get(key).getAsString();
    }
}