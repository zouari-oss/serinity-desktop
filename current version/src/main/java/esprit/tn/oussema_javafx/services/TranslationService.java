package esprit.tn.oussema_javafx.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    /* ===== DETECT LANGUAGE ===== */
    private static String detectLanguage(String text){

        try{
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);

            String urlStr =
                    "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=en|fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                response.append(line);
            }

            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray matches = json.getJSONArray("matches");

            if(matches.length() > 0){
                return matches.getJSONObject(0).getString("source");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return "fr"; // fallback
    }

    /* ===== TRANSLATE ===== */
    public static String translate(String text, String targetLang){

        try{

            if(text == null || text.isBlank())
                return text;

            // 1️⃣ detect source language
            String sourceLang = detectLanguage(text);

            if(sourceLang.equals(targetLang))
                return text;

            // 2️⃣ translate
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);

            String urlStr =
                    "https://api.mymemory.translated.net/get?q="
                            + encoded +
                            "&langpair=" + sourceLang + "|" + targetLang;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                response.append(line);
            }

            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject data = json.getJSONObject("responseData");

            return data.getString("translatedText");

        }catch(Exception e){
            e.printStackTrace();
            return text;
        }
    }
}