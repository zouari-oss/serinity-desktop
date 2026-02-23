package esprit.tn.oussema_javafx.services;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MedicalAIService {

    public static class AIResult {
        public String urgency;
        public String emotion;
        public String recommendation;
    }

    public static AIResult predict(String text) {

        AIResult result = new AIResult();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://127.0.0.1:8000/predict");
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
                while ((line = br.readLine()) != null) sb.append(line);
                response = sb.toString();
            }

            JSONObject json = new JSONObject(response);

            result.urgency = json.optString("urgency", "UNKNOWN");
            result.emotion = json.optString("emotion", "unknown");
            result.recommendation = json.optString("recommendation", "");

        } catch (Exception e) {
            result.urgency = "UNKNOWN";
            result.emotion = "Connexion IA impossible";
            result.recommendation = "Vérifiez que l’API FastAPI est lancée sur http://127.0.0.1:8000";
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }
}