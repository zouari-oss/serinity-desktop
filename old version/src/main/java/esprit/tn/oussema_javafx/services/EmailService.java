package esprit.tn.oussema_javafx.services;



import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String username = "oussema_bouabdallah@longevityplus.store";
    private final String password = "oussema_bouabdallah";

    public void sendEmail(String to, String subject, String content){

        new Thread(() -> {

            try{

                Properties props = new Properties();
                props.put("mail.smtp.host", "longevityplus.store");
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");

                Session session = Session.getInstance(props,
                        new Authenticator(){
                            protected PasswordAuthentication getPasswordAuthentication(){
                                return new PasswordAuthentication(username, password);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(to)
                );

                message.setSubject(subject);
                message.setContent(content, "text/html; charset=UTF-8");

                Transport.send(message);

                System.out.println("EMAIL SENT SUCCESSFULLY");

            }catch (Exception e){
                e.printStackTrace();
            }

        }).start();
    }
}
