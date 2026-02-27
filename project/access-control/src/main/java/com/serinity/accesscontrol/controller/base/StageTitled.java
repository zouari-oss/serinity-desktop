// `StageTitled` package name
package com.serinity.accesscontrol.controller.base;

/**
 * Marker interface for controllers that represent a top-level "scene" and
 * want the main window title to update automatically when they are pushed onto
 * or popped back into view.
 *
 * <p>
 * Implement this interface and return an i18n key from
 * {@link #getSceneTitleKey()}. The {@link StackNavigable} navigation hooks
 * will call {@link com.serinity.accesscontrol.util.I18nUtil#getValue(String)}
 * with the returned key and apply the result to the stage title.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/base/StageTitled.java">
 *        StageTitled.java
 *        </a>
 */
public interface StageTitled {

  /**
   * Returns the i18n message key for this scene's stage title
   * (e.g. {@code "app.scene.title.sign_in"}).
   */
  String getSceneTitleKey();
} // StageTitled interface
