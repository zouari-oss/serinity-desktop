package com.serinity.accesscontrol.api.zenquotes;

import com.serinity.accesscontrol.api.http.ApiClient;
import com.serinity.accesscontrol.api.zenquotes.dto.ZenQuoteDto;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client for ZenQuotes API.
 * Endpoint: https://zenquotes.io/api/random
 *
 * Response example:
 * [
 * { "q": "Quote text", "a": "Author", ... }
 * ]
 */
public class ZenQuotesClient extends ApiClient {

  private static final String RANDOM_URL = "https://zenquotes.io/api/random";

  public ZenQuotesClient() {
    super();
  }

  public ZenQuoteDto fetchRandomQuote() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(RANDOM_URL))
        .GET()
        .build();

    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("ZenQuotes HTTP " + response.statusCode());
    }

    ZenQuoteDto[] quotes = mapper.readValue(response.body(), ZenQuoteDto[].class);
    if (quotes == null || quotes.length == 0) {
      throw new IllegalStateException("ZenQuotes returned empty response");
    }

    return quotes[0];
  }
}
