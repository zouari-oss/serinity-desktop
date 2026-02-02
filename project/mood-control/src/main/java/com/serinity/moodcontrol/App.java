package com.serinity.moodcontrol;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.NodeOrientation;

public class App extends Application {

    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws Exception {

        // 🇫force French
         //final Locale locale = Locale.FRENCH;

        //  default system locale
         final Locale locale = Locale.getDefault();

        // Safa9os
        //final Locale locale = new Locale("ar");

        // load translations
        final ResourceBundle bundle =
                ResourceBundle.getBundle("i18n.messages", locale);

        //  load FXML with bundle
        final FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fxml/Template.fxml"), bundle);

        final Parent root = loader.load();

        // ymin lel ysar
        //if ("ar".equals(locale.getLanguage())) {
        //    root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        //}

        final Scene scene = new Scene(root);

        stage.setTitle("SERINITY");
        stage.setScene(scene);
        stage.show();
    }
}
