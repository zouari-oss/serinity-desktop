/**
 * module-info.java
 *
 * Module declaration for the `access-controle` module
 *
 * <p>
 * Defines module dependencies, exported packages, and reflective access
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-02
 *
 *        <a href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/module-info.java"
 *        target="_blank">module-info.java</a>
 */

module com.serinity.accesscontrol {
  // --- Javafx dependencies ---
  requires transitive javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  // --- Persistence & database dependencies ---
  requires java.sql;
  requires java.naming;
  requires skinnedratorm;

  // --- Configuration dependencies ---
  requires io.github.cdimascio.dotenv.java;

  // --- Module exports ---
  exports com.serinity.accesscontrol;

  // --- Reflection access (opens) ---
  opens com.serinity.accesscontrol.controller to javafx.fxml;
  opens com.serinity.accesscontrol.model to skinnedratorm;
  opens com.serinity.accesscontrol.model.base to skinnedratorm;
} // `com.serinity.accesscontrol` module
