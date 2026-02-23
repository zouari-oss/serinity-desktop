module esprit.tn.oussema_javafx {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jakarta.mail;
    requires java.desktop;
    requires kernel;
    requires layout;

    // main
    opens esprit.tn.oussema_javafx to javafx.fxml;


    // rdv
    opens esprit.tn.oussema_javafx.controllers.rdv to javafx.fxml;

    // doctor
    opens esprit.tn.oussema_javafx.controllers.doctor to javafx.fxml;

    // models
    opens esprit.tn.oussema_javafx.models to javafx.base;

    exports esprit.tn.oussema_javafx;
}
