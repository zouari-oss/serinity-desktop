module com.serinity {
  requires transitive javafx.controls;
  requires javafx.fxml;

  requires java.sql;
  requires org.mariadb.jdbc;
  requires io.github.cdimascio.dotenv.java;

  requires javafx.graphics;
    requires java.desktop;
    requires javafx.base;
    requires org.json;
    requires java.net.http;


    opens com.serinity.forumcontrol.Controllers to javafx.fxml;

  exports com.serinity.forumcontrol;
}
