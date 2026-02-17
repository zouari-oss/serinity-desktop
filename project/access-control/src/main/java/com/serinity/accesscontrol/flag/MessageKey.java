
// `MessageKey` package name
package com.serinity.accesscontrol.flag;

public enum MessageKey {
  GREETING("greeting"),
  LOGIN("login"),
  EXIT("exit"),
  LANGUAGES("languages");

  private final String value;

  MessageKey(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
} // MessageKey enum
