package com.serinity.accesscontrol.api.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Shared HTTP + JSON utilities for external APIs (ZenQuotes, CallMeBot, etc.).
 */
public class ApiClient {

  protected final HttpClient http;
  protected final ObjectMapper mapper;

  public ApiClient() {
    this.http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    this.mapper = new ObjectMapper();
  }
}
