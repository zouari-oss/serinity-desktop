package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.api.zenquotes.ZenQuotesClient;
import com.serinity.accesscontrol.api.zenquotes.dto.ZenQuoteDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service layer for fetching a Zen quote without blocking the JavaFX UI thread.
 */
public class QuoteService {

  private final ZenQuotesClient client;
  private final ExecutorService executor;

  public QuoteService() {
    this.client = new ZenQuotesClient();
    this.executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "serinity-quote-service");
      t.setDaemon(true);
      return t;
    });
  }

  /**
   * Fetches a quote asynchronously and formats it for display.
   */
  public CompletableFuture<QuoteResult> fetchFormattedQuote() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        ZenQuoteDto dto = client.fetchRandomQuote();

        String quote = dto.getQ() == null ? "" : dto.getQ().trim();
        String author = dto.getA() == null ? "" : dto.getA().trim();

        if (quote.isBlank()) {
          return QuoteResult.error("Empty quote received.");
        }

        return QuoteResult.success(quote, author);
      } catch (Exception e) {
        return QuoteResult.error(e.getMessage());
      }
    }, executor);
  }

  /**
   * Small value object for the UI/controller to consume.
   */
  public static class QuoteResult {
    private final boolean success;
    private final String quote;
    private final String author;
    private final String errorMessage;

    private QuoteResult(boolean success, String quote, String author, String errorMessage) {
      this.success = success;
      this.quote = quote;
      this.author = author;
      this.errorMessage = errorMessage;
    }

    public static QuoteResult success(String quote, String author) {
      return new QuoteResult(true, quote, author, null);
    }

    public static QuoteResult error(String message) {
      return new QuoteResult(false, null, null, message);
    }

    public boolean isSuccess() {
      return success;
    }

    public String getQuote() {
      return quote;
    }

    public String getAuthor() {
      return author;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
