// `MailSenderServiceTest` package name
package com.serinity.accesscontrol;

// `junit` static import(s)
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// `junit` import(s)
import org.junit.jupiter.api.Test;

// `serinity` import(s)
import com.serinity.accesscontrol.service.MailSenderService;

public final class MailSenderServiceTest {
  private String to = "zouariomar20@gmail.com";

  /**
   * Integration test for sending an email.
   * 
   * This test uses the real environment variables loaded by
   * EnvironmentVariableLoader. Make sure SMTP credentials are set correctly
   * in your environment before running this test.
   */
  @Test
  void send_shouldNotThrow_withValidData() {
    final String subject = "Integration Test Email";
    final String htmlText = "<h1>Hello!</h1><p>This is a test email.</p>";

    // The test passes if no exception is thrown
    assertDoesNotThrow(() -> MailSenderService.send(to, subject, htmlText));
  }

  /**
   * Test sending to an invalid email address.
   * The MailSenderService should handle exceptions internally.
   */
  @Test
  void send_shouldNotThrow_withInvalidEmail() {
    to = "invalid-email-address";
    final String subject = "Invalid Email Test";
    final String htmlText = "<p>Testing invalid email handling.</p>";

    // Expect a RuntimeException when an invalid email is sent
    final RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      MailSenderService.send(to, subject, htmlText);
    });

    // Verify the cause is MailInvalidAddressException
    assertTrue(thrown.getCause() instanceof org.simplejavamail.mailer.MailInvalidAddressException);
  }

  @Test
  void sendPasswordReset_shouldNotThrow_withValidData() {
    final String username = "Zouari Omar";
    final String generatedCode = "AB12CD";

    // The test passes if no exception is thrown
    assertDoesNotThrow(() -> MailSenderService.sendPasswordReset(username, to, generatedCode));
  }
} // MailSenderService final test class
