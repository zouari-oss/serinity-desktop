module com.serinity {

  requires javafx.graphics;
  requires transitive javafx.controls;
  requires javafx.fxml;

  requires java.net.http;
  requires java.sql;
  requires java.desktop;
  requires javafx.media;

  requires com.google.gson;
  requires jdk.httpserver; // pour le callback OAuth local (Google Calendar)

  opens com.serinity.exercicecontrol.controller to javafx.fxml;
  opens com.serinity.exercicecontrol.model to javafx.base;
  exports com.serinity.exercicecontrol.service.performance;
  opens com.serinity.exercicecontrol.service.performance to com.google.gson;
  opens com.serinity.exercicecontrol.service.ai to com.google.gson;
  opens com.serinity.exercicecontrol.controller.admin to javafx.fxml;
  exports com.serinity.exercicecontrol;
}