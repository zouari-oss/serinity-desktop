/**
 * I18nUtil.java
 *
 * Utility class for application internationalization (i18n).
 *
 * <p>
 * Provides helper methods to manage the current {@link java.util.Locale},
 * load message {@link java.util.ResourceBundle ResourceBundles} based on
 * {@link com.serinity.accesscontrol.flag.PropertyBundle}, retrieve
 * localized messages, and ensure that the active locale is one of the
 * configured supported languages.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-29
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/I18nUtil.java"
 * target="_blank">
 * I18nUtil.java
 * </a>
 */

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
import com.serinity.accesscontrol.flag.SupportedLanguage;

/**
 * Internationalization app manager
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
 */
public final class I18nUtil {
  // NOTE: Make `currentLocale` and `bundle` volatile to ensure thread-safe
  // visibility.
  // See
  // <a
  // href="https://github.com/zouari-oss/serinity-desktop/pull/1#discussion_r2741228161"
  // target="_blank">
  // discussion_r2741228161
  // </a>
  private static volatile Locale currentLocale = Locale.getDefault();
  private static volatile ResourceBundle bundle = ResourceBundle.getBundle(
      PropertyBundle.DEFAULT_MESSAGES_BUNDLE.getBaseName(),
      currentLocale);

  private I18nUtil() {
  }

  public static String get(String key) {
    return bundle.getString(key);
  }

  public static void setLocale(Locale locale) {
    currentLocale = locale;

    bundle = ResourceBundle.getBundle(
        PropertyBundle.DEFAULT_MESSAGES_BUNDLE.getBaseName(),
        currentLocale);
  }

  public static Locale getLocale() {
    return currentLocale;
  }

  public static ResourceBundle getBundle() {
    return bundle;
  }

  public static List<Locale> getSupportedLanguages() {
    ResourceBundle config = ResourceBundle.getBundle(PropertyBundle.SUPPORTED_LANGUAGES_BUNDLE.getBaseName());

    return List.of(config.getString(MessageKey.LANGUAGES.getValue()).split(","))
        .stream()
        .map(String::trim)
        .map(Locale::forLanguageTag)
        .collect(Collectors.toList());
  }

  // INFO:
  // See
  // <a href=
  // "https://github.com/zouari-oss/serinity-desktop/pull/1#discussion_r2741228244"
  // target="_blank">
  // discussion_r2741228244
  // </a>
  public static void applySupportedLocale() {
    List<Locale> supportedLocales = getSupportedLanguages();

    // First, check for an exact locale match.
    if (supportedLocales.contains(currentLocale)) {
      return;
    }

    // Next, try to match by language only (e.g., "en_US" matches supported "en").
    String currentLanguage = currentLocale.getLanguage();
    for (Locale supportedLocale : supportedLocales) {
      if (supportedLocale.getLanguage().equals(currentLanguage)) {
        setLocale(supportedLocale);
        return;
      }
    }

    // Fallback to the configured default supported language.
    setLocale(SupportedLanguage.DEFAULT.getLocale());
  }
} // I18nUtil class
