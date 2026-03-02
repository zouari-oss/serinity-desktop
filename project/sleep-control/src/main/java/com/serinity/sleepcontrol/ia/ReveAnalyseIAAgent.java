package com.serinity.sleepcontrol.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serinity.sleepcontrol.model.Reve;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class ReveAnalyseIAAgent {

    private static final String URL_SINGLE = "http://localhost:5000/analyser-reve";
    private static final String URL_ALL    = "http://localhost:5000/analyser-tous";
    private final ObjectMapper  mapper     = new ObjectMapper();
    private final HttpClient    http       = HttpClient.newHttpClient();

    public AnalyseResult analyser(Reve reve) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("titre",       reve.getTitre());
        payload.put("description", reve.getDescription());
        payload.put("emotions",    reve.getEmotions());
        payload.put("typeReve",    reve.getTypeReve());
        payload.put("intensite",   reve.getIntensite());
        payload.put("couleur",     reve.isCouleur());
        payload.put("recurrent",   reve.isRecurrent());
        return envoyerEtParser(URL_SINGLE, mapper.writeValueAsString(payload));
    }

    public AnalyseResult analyserTous(List<Reve> reves) throws Exception {
        if (reves == null || reves.isEmpty()) {
            AnalyseResult vide = new AnalyseResult();
            vide.setConclusion("Aucun rêve enregistré pour l'analyse.");
            return vide;
        }
        List<Map<String, Object>> liste = new ArrayList<>();
        for (Reve r : reves) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("titre",     r.getTitre());
            m.put("typeReve",  r.getTypeReve());
            m.put("emotions",  r.getEmotions());
            m.put("intensite", r.getIntensite());
            liste.add(m);
        }
        return envoyerEtParser(URL_ALL, mapper.writeValueAsString(Map.of("reves", liste)));
    }

    private AnalyseResult envoyerEtParser(String url, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200)
            throw new RuntimeException("Erreur Flask : " + resp.body());
        JsonNode root = mapper.readTree(resp.body());
        String resultStr = root.get("result").asText();
        return mapper.readValue(resultStr, AnalyseResult.class);
    }
}
