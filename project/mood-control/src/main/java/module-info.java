module com.serinity {
  requires transitive javafx.controls;
    requires javafx.fxml;

    opens com.serinity.moodcontrol.controller to javafx.fxml;


    exports com.serinity.moodcontrol;
}
