module com.serinity {

  requires javafx.graphics;
  requires transitive javafx.controls;
  requires javafx.fxml;

  requires java.sql;
  requires java.desktop;

  opens com.serinity.exercicecontrol.controller to javafx.fxml;
  opens com.serinity.exercicecontrol.model to javafx.base;

  exports com.serinity.exercicecontrol;
}
