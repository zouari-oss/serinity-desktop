// `BaseController` package name
package com.serinity.accesscontrol.controller.base;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.RootController;

/**
 * Base controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/base/BaseController.java">
 *        BaseController.java
 *        </a>
 */
public abstract class BaseController {
  protected RootController rootController;

  public void setRootController(RootController rootController) {
    this.rootController = rootController;
  }
} // BaseController abstract class
