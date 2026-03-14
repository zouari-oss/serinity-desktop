// `Gender` package name
package com.serinity.accesscontrol.flag;

// `serinity` import(s)
import com.serinity.accesscontrol.util.I18nUtil;

/**
 * Represents the user gender
 *
 * <p>
 * This enum is used to define the user gender in the app
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/Gender.java">
 *      Gender.java
 *      </a>
 */
public enum Gender { // God made Adam and Eve, not Adam and Steve :)
  /** Male gender value. */
  MALE("user.gender.male"),
  /** Female gender value. */
  FEMALE("user.gender.female");

  private final String key;

  private Gender(final String key) {
    this.key = key;
  }

  /**
   * Returns the i18n key for this gender.
   *
   * @return i18n key associated with the gender
   */
  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return I18nUtil.getValue(key);
  }
} // Gender enum
