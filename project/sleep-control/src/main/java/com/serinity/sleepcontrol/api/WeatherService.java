package com.serinity.sleepcontrol.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WeatherService {

    private final OkHttpClient client;

    public WeatherService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    // ─── Récupère la météo actuelle ──────────────────────────────────────────────

    public WeatherData getMeteoActuelle() {
        WeatherData data = new WeatherData();

        String url = String.format(
                "%s?q=%s,%s&appid=%s&units=%s&lang=%s",
                WeatherConfig.BASE_URL,
                WeatherConfig.CITY,
                WeatherConfig.COUNTRY,
                WeatherConfig.API_KEY,
                WeatherConfig.UNITS,
                WeatherConfig.LANG
        );

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                data.setErreur("Réponse invalide : " + response.code());
                return data;
            }

            String json = response.body().string();
            JSONObject obj = new JSONObject(json);

            JSONObject main = obj.getJSONObject("main");
            data.setTemperature(main.getDouble("temp"));
            data.setTemperatureRessentie(main.getDouble("feels_like"));
            data.setHumidite(main.getDouble("humidity"));

            JSONObject wind = obj.getJSONObject("wind");
            data.setVitesseVent(wind.getDouble("speed") * 3.6);

            JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
            data.setDescription(weather.getString("description"));
            data.setIcone(weather.getString("icon"));

            data.setVille(obj.getString("name"));
            data.setChargee(true);

        } catch (IOException e) {
            data.setErreur("Erreur réseau : " + e.getMessage());
        }

        return data;
    }

    // ─── Emoji météo selon icône OpenWeather ────────────────────────────────────

    public static String getEmojiMeteo(String icone) {
        if (icone == null) return "🌡️";
        return switch (icone.substring(0, 2)) {
            case "01" -> "☀️";
            case "02" -> "🌤️";
            case "03" -> "⛅";
            case "04" -> "☁️";
            case "09" -> "🌧️";
            case "10" -> "🌦️";
            case "11" -> "⛈️";
            case "13" -> "❄️";
            case "50" -> "🌫️";
            default   -> "🌡️";
        };
    }
}
