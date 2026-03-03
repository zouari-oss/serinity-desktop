module com.serinity.accesscontrol {
  // --- Javafx dependencies ---
  requires transitive javafx.controls;
  requires javafx.fxml;
  requires javafx.web;
  requires javafx.graphics;

  // --- Persistence & database dependencies ---
  requires java.sql;
  requires java.naming;
  requires org.zouarioss.skinnedratorm;

  // --- Configuration dependencies ---
  requires io.github.cdimascio.dotenv.java;
  requires org.simplejavamail;
  requires org.simplejavamail.core;
  requires org.apache.logging.log4j;
  requires com.github.benmanes.caffeine;
  requires spring.security.crypto;
  requires java.desktop;
  requires java.net.http;
  requires com.microsoft.onnxruntime;
  requires org.opencv;
  requires com.fasterxml.jackson.databind;
  requires weka.stable;
  requires org.json;
  requires org.apache.pdfbox;

    // --- Module exports ---
  exports com.serinity.accesscontrol;

  // --- Reflection access (opens) ---
    opens com.serinity.accesscontrol.controller.forum to javafx.fxml;
  opens com.serinity.accesscontrol.controller to javafx.fxml;
  opens com.serinity.accesscontrol.controller.backoffice to javafx.fxml;
  opens com.serinity.accesscontrol.controller.sleepcontrol to javafx.fxml;
  opens com.serinity.accesscontrol.model to org.zouarioss.skinnedratorm;
  opens com.serinity.accesscontrol.model.base to org.zouarioss.skinnedratorm;
  opens com.serinity.accesscontrol.model.sleepcontrol to org.zouarioss.skinnedratorm;
  opens com.serinity.accesscontrol.controller.consultation to javafx.fxml;
  opens com.serinity.accesscontrol.controller.consultation.doctor to javafx.fxml;
  opens com.serinity.accesscontrol.controller.consultation.rdv to javafx.fxml;
  opens com.serinity.accesscontrol.model.consultation to javafx.base;
  opens com.serinity.accesscontrol.api.zenquotes.dto to com.fasterxml.jackson.databind;
  opens com.serinity.accesscontrol.api.zenquotes to com.fasterxml.jackson.databind;
} // `com.serinity.accesscontrol` module
