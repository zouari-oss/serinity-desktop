/**
 * ClassName.java
 *
 * Short description of this class or file.
 *
 * <p>Detailed explanation of the class, its responsibilities, and usage.</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-29
 *
 * <a href="https://simplelocalize.io/blog/posts/java-internationalization" target="_blank">Java 24: Internationalization</a>
 */

// `I18nUtil` package name
package com.serinity.accesscontrol.util;

// `java` imports
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// `serinity` imports
import com.serinity.accesscontrol.flag.MessageKey;
import com.serinity.accesscontrol.flag.PropertyBundle;
import com.serinity.accesscontrol.flag.SupportedLanguage;

public final class I18nUtil {
  private static Locale currentLocale = Locale.getDefault();

  private static ResourceBundle bundle = ResourceBundle.getBundle(
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

  public static void applySupportedLocale() {
    if (!getSupportedLanguages().contains(currentLocale)) {
      setLocale(SupportedLanguage.DEFAULT.getLocale());
    }
  }
} // I18nUtil class
