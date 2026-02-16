module com.serinity {
  requires transitive javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.mariadb.jdbc;


    opens com.serinity.exercicecontrol.controller to javafx.fxml;


    exports com.serinity.exercicecontrol;
}
