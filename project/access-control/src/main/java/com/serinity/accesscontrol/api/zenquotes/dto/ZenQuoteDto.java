package com.serinity.accesscontrol.api.zenquotes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for ZenQuotes response objects.
 * ZenQuotes uses keys:
 * - q : quote text
 * - a : author
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZenQuoteDto {

  private String q;
  private String a;

  public ZenQuoteDto() {
  }

  public ZenQuoteDto(String q, String a) {
    this.q = q;
    this.a = a;
  }

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public String getA() {
    return a;
  }

  public void setA(String a) {
    this.a = a;
  }
}
