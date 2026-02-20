module com.serinity.accesscontrol {
  // --- Javafx dependencies ---
  requires transitive javafx.controls;
  requires javafx.fxml;
  requires javafx.web;

  // --- Persistence & database dependencies ---
  requires java.sql;
  requires java.naming;
  requires org.zouarioss.skinnedratorm;
  requires org.simplejavamail;
  requires org.simplejavamail.core;
  requires org.apache.logging.log4j;

  // --- Configuration dependencies ---
  requires io.github.cdimascio.dotenv.java;

  // --- Module exports ---
  exports com.serinity.accesscontrol;

  // --- Reflection access (opens) ---
  opens com.serinity.accesscontrol.controller to javafx.fxml;
  opens com.serinity.accesscontrol.model to org.zouarioss.skinnedratorm;
  opens com.serinity.accesscontrol.model.base to org.zouarioss.skinnedratorm;
} // `com.serinity.accesscontrol` module
