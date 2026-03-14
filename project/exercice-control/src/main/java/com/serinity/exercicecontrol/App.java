package com.serinity.exercicecontrol;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        // use system language
         Locale locale = Locale.getDefault();

        // force
        //Locale locale = Locale.FRENCH;

        // load translations
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

        // load FXML with bundle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Template.fxml"), bundle);
        Scene scene = new Scene(loader.load());

        stage.setTitle("SERINITY");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
