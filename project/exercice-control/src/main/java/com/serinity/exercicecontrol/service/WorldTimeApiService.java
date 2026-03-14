package com.serinity.exercicecontrol.service;

import java.net.URI;
import java.net.http.*;
import java.time.OffsetDateTime;
import java.util.regex.*;

public class WorldTimeApiService {

    private final HttpClient http = HttpClient.newHttpClient();

    public WorldTimeInfo fetchTime(String timezone) {
        try {
            String url = "https://worldtimeapi.org/api/timezone/" + timezone;
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new RuntimeException("HTTP " + res.statusCode());

            String json = res.body();
            String datetime = extract(json, "\"datetime\"\\s*:\\s*\"(.*?)\"");
            OffsetDateTime odt = OffsetDateTime.parse(datetime);

            int hour = odt.getHour();
            DayPhase phase = dayPhase(hour);

            return new WorldTimeInfo(timezone, odt, hour, phase);
        } catch (Exception e) {
            throw new RuntimeException("WorldTimeAPI: " + e.getMessage(), e);
        }
    }

    private DayPhase dayPhase(int hour) {
        if (hour >= 6 && hour < 12) return DayPhase.MORNING;
        if (hour >= 12 && hour < 18) return DayPhase.AFTERNOON;
        if (hour >= 18 && hour < 22) return DayPhase.EVENING;
        return DayPhase.NIGHT;
    }

    private String extract(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        if (!m.find()) return null;
        return m.group(1).replace("\\/", "/");
    }

    public enum DayPhase { MORNING, AFTERNOON, EVENING, NIGHT }

    public record WorldTimeInfo(String timezone, OffsetDateTime dateTime, int hour, DayPhase phase) {}
}