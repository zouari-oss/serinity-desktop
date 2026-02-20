// `PropertyBundle` package name
package com.serinity.accesscontrol.flag;

/**
 * Bundles filename manager (enum)
 *
 * <p>
 * see `i18n/*`
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-30
 *
 *        <pre>{@code
 * // Example usage
 * final static ResourceBundle bundle = ResourceBundle.getBundle(
 *     PropertyBundle.DEFAULT_MESSAGES_BUNDLE.getBaseName(),
 *     currentLocale);
 * }</pre>
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/PropertyBundle.java">
 *        PropertyBundle.java
 *        </a>
 */
public enum PropertyBundle {
  SUPPORTED_LANGUAGES_BUNDLE("i18n.supported-languages"),
  DEFAULT_MESSAGES_BUNDLE("i18n.messages"),
  ENGLISH_MESSAGES_BUNDLE("i18n.messages"),
  FRENCH_MESSAGES_BUNDLE("i18n.messages_fr");

  private final String baseName;

  PropertyBundle(final String baseName) {
    this.baseName = baseName;
  }

  public final String getBaseName() {
    return baseName;
  }
} // PropertyBundle enum
