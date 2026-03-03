// `I18nUtil` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.MessageKey;
import com.serinity.accesscontrol.flag.PropertyBundle;

/**
 * Utility class for application internationalization (i18n)
 *
 * <p>
 * Provides helper methods to manage the current {@link java.util.Locale},
 * load message {@link java.util.ResourceBundle ResourceBundles} based on
 * {@link com.serinity.accesscontrol.flag.PropertyBundle}, retrieve
 * localized messages, and ensure that the active locale is one of the
 * configured supported languages.
 * </p>
 *
 * <pre>
 * {@code
 * I18nUtil.applySupportedLocale();
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-29
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/I18nUtil.java">
 *        I18nUtil.java
 *        </a>
 */
public final class I18nUtil {
  /*
   * NOTE: Make `currentLocale` and `bundle` volatile to ensure thread-safe
   * visibility.
   *
   * <a
   * href=
   * "https://github.com/zouari-oss/serinity-desktop/pull/1#discussion_r2741228161">
   * discussion_r2741228161
   * </a>
   */
  private static volatile Locale currentLocale = Locale.getDefault();
  private static volatile ResourceBundle bundle = ResourceBundle.getBundle(
      PropertyBundle.DEFAULT_MESSAGES_BUNDLE.getBaseName(),
      currentLocale);

  /**
   * Returns the localized string for the given message key.
   *
   * @param key the i18n message key (e.g., {@code "greeting"})
   * @return the localized string value
   */
  public static String getValue(final String key) {
    return bundle.getString(key);
  }

  /**
   * Changes the active locale and reloads the resource bundle accordingly.
   *
   * @param locale the new {@link Locale} to apply
   */
  public static void setLocale(final Locale locale) {
    currentLocale = locale;

    bundle = ResourceBundle.getBundle(
        PropertyBundle.DEFAULT_MESSAGES_BUNDLE.getBaseName(),
        currentLocale);
  }

  /**
   * Returns the currently active {@link Locale}.
   *
   * @return the current locale
   */
  public static Locale getLocale() {
    return currentLocale;
  }

  /**
   * Returns the currently loaded {@link ResourceBundle}.
   *
   * @return the active resource bundle
   */
  public static ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * Returns the list of supported {@link Locale} instances loaded from
   * the {@code supported-languages} property bundle.
   *
   * @return list of supported locales
   */
  public static List<Locale> getSupportedLanguages() {
    final ResourceBundle config = ResourceBundle.getBundle(PropertyBundle.SUPPORTED_LANGUAGES_BUNDLE.getBaseName());

    return List.of(config.getString(MessageKey.LANGUAGES.getValue()).split(","))
        .stream()
        .map(String::trim)
        .map(Locale::forLanguageTag)
        .collect(Collectors.toList());
  }

  /**
   * Returns the list of supported language tags (e.g., {@code "en"},
   * {@code "fr"})
   * as plain strings, suitable for populating UI combo-boxes.
   *
   * @return list of supported language tag strings
   */
  public static List<String> getSupportedLanguagesToString() {
    final ResourceBundle config = ResourceBundle.getBundle(PropertyBundle.SUPPORTED_LANGUAGES_BUNDLE.getBaseName());

    return List.of(config.getString(MessageKey.LANGUAGES.getValue()).split(","))
        .stream()
        .map(String::trim)
        .collect(Collectors.toList());
  }

  /*
   * <a href=
   * "https://github.com/zouari-oss/serinity-desktop/pull/1#discussion_r2741228244">
   * discussion_r2741228244
   * </a>
   */
  /**
   * Applies the best matching supported locale to the current runtime locale.
   *
   * <p>
   * First tries an exact match; if none is found, falls back to matching
   * by language tag only (e.g., {@code en_US} → {@code en}).
   * If no match is found at all, the current locale remains unchanged.
   * </p>
   */
  public static void applySupportedLocale() {
    final List<Locale> supportedLocales = getSupportedLanguages();

    // First, check for an exact locale match.
    if (supportedLocales.contains(currentLocale)) {
      return;
    }

    // Next, try to match by language only (e.g., "en_US" matches supported "en").
    final String currentLanguage = currentLocale.getLanguage();
    for (final Locale supportedLocale : supportedLocales) {
      if (supportedLocale.getLanguage().equals(currentLanguage)) {
        setLocale(supportedLocale);
        return;
      }
    }
  }

  private I18nUtil() {
  }
} // I18nUtil class
