package com.serinity.sleepcontrol.service;

import com.google.gson.*;
import com.serinity.sleepcontrol.utils.NlpConfig;
import com.serinity.sleepcontrol.model.NlpResultat;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class NlpService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    // ── Sentiment ─────────────────────────────────────────────
    public NlpResultat analyser(String texte) {
        NlpResultat resultat = new NlpResultat();
        resultat.setSentimentLabel("neutral");
        resultat.setSentimentScore(0);
        if (texte == null || texte.trim().length() < 3) return resultat;

        try {
            String body = "{\"inputs\": \"" + texte.replace("\"", "'") + "\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(NlpConfig.HF_URL + NlpConfig.MODEL_SENTIMENT))
                    .header("Authorization", "Bearer " + NlpConfig.HF_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
            if (arr.get(0).isJsonArray()) arr = arr.get(0).getAsJsonArray();

            String bestLabel = "neutral";
            double bestScore = 0;
            for (JsonElement e : arr) {
                JsonObject obj = e.getAsJsonObject();
                double score = obj.get("score").getAsDouble();
                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = obj.get("label").getAsString();
                }
            }
            resultat.setSentimentLabel(bestLabel.toLowerCase());
            resultat.setSentimentScore(bestScore);

        } catch (Exception e) {
            System.err.println("Sentiment erreur : " + e.getMessage());
        }
        return resultat;
    }

    // ── Recommandations GPT ───────────────────────────────────
    public String genererRecommandations(String titrReve, String description,
                                         String humeur, String typeReve,
                                         String sentimentNlp) {
        try {
            // Prompt clair et structuré en français
            String prompt = "[INST] Tu es un expert en bien-être et psychologie du sommeil. "
                    + "Un utilisateur vient de se réveiller après ce rêve :\n"
                    + "- Titre : " + titrReve + "\n"
                    + "- Description : " + description + "\n"
                    + "- Humeur ressentie : " + humeur + "\n"
                    + "- Type de rêve : " + typeReve + "\n"
                    + "- Analyse sentiment : " + sentimentNlp + "\n\n"
                    + "Donne exactement 3 recommandations courtes et bienveillantes "
                    + "pour optimiser sa journée. "
                    + "Format : 1. ... 2. ... 3. ... "
                    + "Réponds uniquement en français, sois positif et concret. [/INST]";

            String body = new Gson().toJson(new GptPayload(prompt, 300, 0.7f));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(NlpConfig.HF_URL + NlpConfig.MODEL_GPT))
                    .header("Authorization", "Bearer " + NlpConfig.HF_TOKEN)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
            String texteGenere = arr.get(0).getAsJsonObject()
                    .get("generated_text").getAsString();

            // Extraire seulement la réponse après [/INST]
            if (texteGenere.contains("[/INST]")) {
                texteGenere = texteGenere
                        .substring(texteGenere.lastIndexOf("[/INST]") + 7)
                        .trim();
            }
            return texteGenere;

        } catch (Exception e) {
            return "1. Prends quelques respirations profondes ce matin.\n"
                    + "2. Note ce rêve dans ton journal pour mieux le comprendre.\n"
                    + "3. Commence ta journée avec une activité qui te fait du bien.";
        }
    }

    // ── Inner classes payload ─────────────────────────────────
    private record GptPayload(String inputs,
                              @com.google.gson.annotations.SerializedName("max_new_tokens")
                              int maxNewTokens,
                              float temperature) {}
}
