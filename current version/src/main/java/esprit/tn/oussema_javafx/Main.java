package esprit.tn.oussema_javafx;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Overridemd
    
    public void start(Stage stage) {

        // configuration principale de la fenêtre
        stage.setTitle("TheDoctor");
        stage.setWidth(1100);
        stage.setHeight(700);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        // initialisation du Router (TRÈS IMPORTANT)
        Router.init(stage);

        // page de démarrage (tu pourras changer vers rdv_list ensuite)
       // Router.go("/fxml/doctor/doctor_list.fxml", "TheDoctor - Doctors");
     Router.go("/fxml/doctor/doctor_rdv_list.fxml", "TheDoctor - Doctors");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
