package esprit.tn.oussema_javafx.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProfanityService {

    /* =====================================================
       FILTRE TEXTE → remplace automatiquement les insultes
       ===================================================== */
    public static String cleanText(String text){

        try{
            if(text == null || text.isBlank())
                return text;

            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // API qui retourne directement le texte filtré
            String api = "https://www.purgomalum.com/service/plain?text=" + encoded;

            URL url = new URL(api);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String filtered = reader.readLine();
            reader.close();

            return filtered;

        }catch(Exception e){
            e.printStackTrace();
            // si problème internet → on retourne texte original
            return text;
        }
    }
}