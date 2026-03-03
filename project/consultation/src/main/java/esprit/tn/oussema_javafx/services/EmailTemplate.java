package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.models.User;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class EmailTemplate {

    public static String rdvReceived(User patient, User doctor, LocalDateTime dateTime){

        DateTimeFormatter dateF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeF = DateTimeFormatter.ofPattern("HH:mm");

        String date = dateTime.format(dateF);
        String time = dateTime.format(timeF);

        String html = """
        <html>
        <body style="margin:0;font-family:Arial,Helvetica,sans-serif;background:#f4f6f9;">

            <table width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
                <tr>
                    <td align="center">

                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:white;border-radius:12px;
                               box-shadow:0 10px 35px rgba(0,0,0,.15);
                               overflow:hidden;">

                            <tr>
                                <td style="background:#2f6fed;padding:22px;color:white;text-align:center;">
                                    <h2 style="margin:0;">Confirmation de réception</h2>
                                    <div style="opacity:.9">Cabinet Médical</div>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:30px;color:#333;">

                                    <h3>Bonjour %s 👋</h3>

                                    <p>
                                    Votre demande de rendez-vous a bien été reçue.
                                    Le médecin va la valider très prochainement.
                                    </p>

                                    <div style="background:#f1f5ff;
                                                padding:18px;
                                                border-radius:10px;
                                                margin:25px 0;">

                                        <b>Médecin :</b> Dr %s<br>
                                        <b>Spécialité :</b> %s<br>
                                        <b>Date :</b> %s<br>
                                        <b>Heure :</b> %s

                                    </div>

                                    <p>
                                    Merci de votre confiance.
                                    </p>

                                </td>
                            </tr>

                            <tr>
                                <td style="background:#f4f6f9;padding:18px;text-align:center;font-size:12px;color:#666;">
                                    Ceci est un email automatique, merci de ne pas répondre.
                                </td>
                            </tr>

                        </table>

                    </td>
                </tr>
            </table>

        </body>
        </html>
        """;

        return String.format(
                html,
                patient.getFullName(),
                doctor.getFullName(),
                doctor.getSpeciality() == null ? "Médecin généraliste" : doctor.getSpeciality(),
                date,
                time
        );
    }
}
