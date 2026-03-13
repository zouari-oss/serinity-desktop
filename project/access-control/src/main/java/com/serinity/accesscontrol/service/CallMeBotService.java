package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.api.callmebot.CallMeBotClient;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class CallMeBotService {

  private final CallMeBotClient client;
  private final Dotenv dotenv;

  private final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
  private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

  public CallMeBotService() {
    this.client = new CallMeBotClient();
    this.dotenv = Dotenv.configure()
        .filename(".env")
        .ignoreIfMissing()
        .load();
  }

  /**
   * Sends a WhatsApp notification when a journal entry is saved.
   * Runs async so it won't block the JavaFX thread.
   */

  public CompletableFuture<Void> notifyJournalSaved(String title, LocalDateTime createdAt) {
    final String phone = dotenv.get("WHATSAPP_PHONE");
    final String apiKey = dotenv.get("WHATSAPP_API_KEY");

    // No-op if not configured (dev env only)
    if (isBlank(phone) || isBlank(apiKey)) {
      return CompletableFuture.completedFuture(null);
    }

    final String safeTitle = isBlank(title) ? "Untitled" : title.trim();
    final LocalDateTime ts = (createdAt == null) ? LocalDateTime.now() : createdAt;

    final String message = buildJournalSavedMessage(safeTitle, ts);

    return CompletableFuture.runAsync(new Runnable() {
      @Override
      public void run() {
        try {
          client.sendWhatsAppMessage(phone, apiKey, message);
        } catch (Exception ignored) {
          // debug
          System.err.println("CallMeBot WhatsApp notification failed: " + ignored.getMessage());
        }
      }
    });
  }

  private String buildJournalSavedMessage(String title, LocalDateTime createdAt) {
    String date = createdAt.toLocalDate().format(dateFmt);
    String time = createdAt.toLocalTime().format(timeFmt);

    return String.format(
        "✅ Journal entry saved successfully%n%n" +
            "📝 Title: %s%n" +
            "📅 Date: %s%n" +
            "⏰ Time: %s%n%n" +
            "Thanks for checking in today with Serinity.",
        title, date, time);
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
