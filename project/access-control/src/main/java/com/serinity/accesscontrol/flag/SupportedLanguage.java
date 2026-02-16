// `SupportedLanguage` package name
package com.serinity.accesscontrol.flag;

// `java` import(s)
import java.util.Locale;

/**
 * Supported Locale(s) manager (enum)
 *
 * <p>
 * see `i18n/*`
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-30
 *
 *        <pre>
 * {@code
 * // Example usage
 * private static Locale DEFAULT_LOCALE = SupportedLanguage.DEFAULT.getLocale();
 * }</pre>
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/SupportedLanguage.java">
 *        SupportedLanguage.java
 *        </a>
 */
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

  public String getCode() {
    return locale.getLanguage();
  }
} // SupportedLanguage enum
