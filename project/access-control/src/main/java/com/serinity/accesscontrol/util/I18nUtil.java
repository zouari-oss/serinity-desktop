// `I18nUtil` package name
package com.serinity.accesscontrol.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.serinity.accesscontrol.flag.SupportedLanguage;

/**
 * Centralised internationalisation (i18n) utility for the access-control
 * module.
 *
 * <p>
 * Holds the active {@link Locale} and exposes the corresponding
 * {@link ResourceBundle} loaded from {@code i18n/messages}.  All controllers
 * and FXML loaders obtain the bundle through this class so that a locale
 * change is immediately reflected across the whole module.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 */
public final class I18nUtil {

  private static final String BUNDLE_BASE = "i18n/messages";

  private static Locale locale = Locale.ENGLISH;
  private static ResourceBundle bundle = loadBundle(locale);

  private I18nUtil() {
  }

  // -------------------------------------------------------------------------
  // Locale management
  // -------------------------------------------------------------------------

  /**
   * Sets the active locale and reloads the resource bundle.
   *
   * @param newLocale the locale to activate; must not be {@code null}
   */
  public static void setLocale(final Locale newLocale) {
    locale = newLocale;
    bundle = loadBundle(newLocale);
  }

  /** Returns the currently active {@link Locale}. */
  public static Locale getLocale() {
    return locale;
  }

  // -------------------------------------------------------------------------
  // Bundle access
  // -------------------------------------------------------------------------

  /** Returns the currently active {@link ResourceBundle}. */
  public static ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * Looks up a key in the active bundle.
   *
   * @param key the message key
   * @return the localised string, or the key itself if not found
   */
  public static String getValue(final String key) {
    try {
      return bundle.getString(key);
    } catch (final MissingResourceException e) {
      return key;
    }
  }

  // -------------------------------------------------------------------------
  // Language list helpers
  // -------------------------------------------------------------------------

  /** Returns all supported language codes as strings. */
  public static List<String> getSupportedLanguagesToString() {
    return Arrays.stream(SupportedLanguage.values())
        .map(SupportedLanguage::getCode)
        .toList();
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private static ResourceBundle loadBundle(final Locale loc) {
    return ResourceBundle.getBundle(BUNDLE_BASE, loc);
  }
} // I18nUtil final class
