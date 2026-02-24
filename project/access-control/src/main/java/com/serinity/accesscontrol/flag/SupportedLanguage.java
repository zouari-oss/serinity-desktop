// `SupportedLanguage` package name
package com.serinity.accesscontrol.flag;

import java.util.Locale;

/**
 * Supported UI languages for the Serinity application.
 *
 * <p>
 * Each constant maps a language code to a {@link Locale} used when loading
 * the i18n {@link java.util.ResourceBundle}.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 */
public enum SupportedLanguage {
  EN("en", Locale.ENGLISH),
  FR("fr", Locale.FRENCH);

  private final String code;
  private final Locale locale;

  SupportedLanguage(final String code, final Locale locale) {
    this.code = code;
    this.locale = locale;
  }

  public String getCode() {
    return code;
  }

  public Locale getLocale() {
    return locale;
  }
} // SupportedLanguage enum
