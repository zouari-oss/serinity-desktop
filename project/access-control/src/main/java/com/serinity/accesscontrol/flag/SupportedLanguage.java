package com.serinity.accesscontrol.flag;

import java.util.Locale;

public enum SupportedLanguage {
  DEFAULT(Locale.ENGLISH),
  EN(Locale.ENGLISH),
  FR(Locale.FRENCH);

  private final Locale locale;

  SupportedLanguage(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }
}
