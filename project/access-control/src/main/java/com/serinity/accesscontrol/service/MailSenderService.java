// `MailSenderService` package name
package com.serinity.accesscontrol.service;

// `java` import(s)
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// `simplejavamail` import(s)
import org.simplejavamail.MailException;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

// `serinity` import(s)
import com.serinity.accesscontrol.config.EnvironmentVariableLoader;
import com.serinity.accesscontrol.flag.ResourceFile;

/**
 * Utility service responsible for sending emails using SMTP.
 *
 * <p>
 * This class provides a centralized method for sending HTML emails through an
 * SMTP server configured via environment variables loaded by
 * {@link EnvironmentVariableLoader}.
 * </p>
 *
 * <p>
 * The class uses the Mailer library to construct and send emails with the
 * specified subject, HTML content, and recipient.
 * </p>
 *
 * <p>
 * The SMTP server, port, username, and authentication token are retrieved
 * from environment variables. If the email cannot be sent due to an SMTP
 * or mail-related exception, it is caught and printed to the console.
 * </p>
 *
 * <p>
 * Exposed functionality:
 * </p>
 * <ul>
 * <li>{@link #send(String, String, String)} – Sends an HTML email to a
 * specified recipient.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * MailSenderService.send(
 *     "zouariomar20@gmail.com",
 *     "Welcome!",
 *     "Hello World");
 * </pre>
 *
 * <p>
 * Exceptions:
 * </p>
 * <ul>
 * <li>{@link com.icegreen.greenmail.util.MailException} – Caught internally
 * and printed if the email cannot be sent.</li>
 * </ul>
 * 
 * <p>
 * SMTP configuration must be correctly set in environment variables; otherwise,
 * email sending will fail.
 * </p>
 * 
 * @see EnvironmentVariableLoader
 * 
 * @version 1.0
 * @since 2026-02-16
 * @author @ZouariOmar (zouariomar20@gmail.com)
 */
public final class MailSenderService {

  /**
   * Sends an HTML email to a specific recipient using SMTP configuration
   * loaded from environment variables.
   *
   * @param to       The recipient's email address.
   * @param subject  The subject line of the email.
   * @param htmlText The HTML content of the email.
   */
  public static final void send(final String toEmail, final String subject, final String htmlText) {
    try {
      MailerBuilder
          .withSMTPServer(
              EnvironmentVariableLoader.getSmtpHost(),
              EnvironmentVariableLoader.getSmtpPort(),
              EnvironmentVariableLoader.getSmtpUsername(),
              EnvironmentVariableLoader.getSmtpPassword())
          .withTransportStrategy(
              EnvironmentVariableLoader.isSmptTls()
                  ? TransportStrategy.SMTP_TLS
                  : TransportStrategy.SMTP)
          .buildMailer() // Building the Mailer
          .sendMail( // Send the mail
              EmailBuilder
                  .startingBlank()
                  .from(EnvironmentVariableLoader.getSmtpFromName(), EnvironmentVariableLoader.getSmtpUsername())
                  .to(toEmail)
                  .withSubject(subject)
                  .withHTMLText(htmlText)
                  .buildEmail() // Building the Email
          );
    } catch (final MailException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
      // _LOGGER.warn("com.mycompany.hirelog.service.MailSenderService#send - Email
      // sending error!");
    }
  }

  /**
   * Load HTML template from ResourceFile enum
   *
   * @param username      the name of the recipient to personalize the template
   * @param toEmail       the recipient's email address
   * @param generatedCode a code or token to insert into the template
   */
  public static final void sendPasswordReset(final String username, final String toEmail, final String generatedCode) {
    final String htmlTemplate = loadHtmlTemplate(ResourceFile.FORGET_PASSWORD_HTML);
    final String htmlContent = htmlTemplate
        .replace("{{username}}", username)
        .replace("{{email}}", toEmail)
        .replace("{{generated-code}}", generatedCode);
    send(toEmail, "Reset Your Password", htmlContent);
  }

  /**
   * Load HTML template from ResourceFile enum.
   *
   * @param resourceFile html resource file
   * @return the content of the HTML template as a String
   */
  private static String loadHtmlTemplate(final ResourceFile resourceFile) {
    try (InputStream is = MailSenderService.class.getResourceAsStream(resourceFile.getFileName())) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + resourceFile);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to load resource: " + resourceFile, e);
    }
  }
} // MailSenderService final class
