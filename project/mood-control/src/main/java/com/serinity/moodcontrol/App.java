package com.serinity.moodcontrol;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class App extends Application {
    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws Exception {
        // force to use french
        final Locale locale = Locale.FRENCH;

        // 1) load translations
        final ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);





        // 3) load FXML with bundle
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Template.fxml"), bundle);
        final Scene scene = new Scene(loader.load());



        stage.setTitle("SERINITY");
        stage.setScene(scene);
        stage.show();
    }
}
