package esprit.tn.oussema_javafx.services;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GoogleCalendarLinkService {

    private static final DateTimeFormatter GOOGLE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public static void openEvent(
            String title,
            String description,
            String location,
            LocalDateTime start,
            LocalDateTime end
    ) {
        try {

            // format google UTC
            String startStr = start.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(GOOGLE_FMT);

            String endStr = end.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(GOOGLE_FMT);

            String url = "https://calendar.google.com/calendar/render?action=TEMPLATE"
                    + "&text=" + encode(title)
                    + "&details=" + encode(description)
                    + "&location=" + encode(location)
                    + "&dates=" + startStr + "/" + endStr;

            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encode(String s){
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}