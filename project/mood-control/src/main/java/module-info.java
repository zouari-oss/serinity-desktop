module com.serinity.moodcontrol {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;
    requires org.mariadb.jdbc;
    requires io.github.cdimascio.dotenv.java;

    requires java.net.http;

    requires com.fasterxml.jackson.databind;

    requires weka.stable;

    opens com.serinity.moodcontrol.controller to javafx.fxml;

    opens com.serinity.moodcontrol.api.zenquotes.dto to com.fasterxml.jackson.databind;

    exports com.serinity.moodcontrol;
    opens com.serinity.moodcontrol.api.zenquotes to com.fasterxml.jackson.databind;
}