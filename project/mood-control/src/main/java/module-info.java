module com.serinity {
  requires transitive javafx.controls;
  requires javafx.fxml;

  requires java.sql;
  requires org.mariadb.jdbc;
  requires io.github.cdimascio.dotenv.java;

  requires javafx.graphics;




    opens com.serinity.moodcontrol.controller to javafx.fxml;

  exports com.serinity.moodcontrol;
}
