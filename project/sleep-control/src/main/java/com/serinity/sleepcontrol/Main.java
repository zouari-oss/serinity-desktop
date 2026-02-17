package com.serinity.sleepcontrol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/fxml/main-template.fxml")
        );

        primaryStage.setTitle("Sleep Control");
        primaryStage.setScene(new Scene(root, 1000, 650));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
