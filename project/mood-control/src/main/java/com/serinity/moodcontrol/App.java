package com.serinity.moodcontrol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // use system language
        //Locale locale = Locale.getDefault();

        //  force
         Locale locale = Locale.FRENCH;

        // 1) load translations
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

        //System.out.println("FR footer = " + bundle.getString("footer.text.simple"));


        // 3) load FXML with bundle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Template.fxml"), bundle);
        Scene scene = new Scene(loader.load());

        // title: keep hardcoded if you don't want app name translated
        stage.setTitle("SERINITY");
        // or: stage.setTitle(bundle.getString("app.name"));

        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
