package com.serinity.exercicecontrol.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WeatherApiService {
    private final ApiClient client = new ApiClient();
    private final Gson gson = new Gson();

    public record WeatherNow(double temperatureC, double windKph, double precipitationMm, boolean isRaining) {}

    public WeatherNow fetchNow(double lat, double lon) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,wind_speed_10m"
                + "&hourly=precipitation"
                + "&forecast_days=1"
                + "&timezone=auto";

        String json = client.get(url, null);
        JsonObject root = gson.fromJson(json, JsonObject.class);

        JsonObject current = root.getAsJsonObject("current");
        double temp = current.get("temperature_2m").getAsDouble();
        double wind = current.get("wind_speed_10m").getAsDouble();

        JsonObject hourly = root.getAsJsonObject("hourly");
        JsonArray precipArr = hourly.getAsJsonArray("precipitation");
        double precip = (precipArr != null && precipArr.size() > 0) ? precipArr.get(0).getAsDouble() : 0.0;

        boolean raining = precip >= 0.2; // seuil simple
        return new WeatherNow(temp, wind, precip, raining);
    }
}