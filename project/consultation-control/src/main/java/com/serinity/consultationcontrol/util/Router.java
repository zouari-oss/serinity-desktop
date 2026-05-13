package com.serinity.consultationcontrol.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class Router {

    public interface EmbeddedNavigator {
        void go(String fxml, String title, Consumer<Object> controllerConsumer);
    }

    private static Stage primaryStage;
    private static Scene mainScene;
    private static EmbeddedNavigator embeddedNavigator;

    public static void init(Stage stage){
        primaryStage = stage;
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }

    public static void setEmbeddedNavigator(EmbeddedNavigator navigator) {
        embeddedNavigator = navigator;
    }

    private static Stage getStage() {
        if (primaryStage != null) {
            return primaryStage;
        }
        return Window.getWindows().stream()
            .filter(w -> w instanceof Stage && w.isShowing())
            .map(w -> (Stage) w)
            .findFirst()
            .orElse(null);
    }

    public static void go(String fxml, String title){
        go(fxml, title, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> void go(String fxml, String title, Consumer<T> controllerConsumer){
        try{
            if (embeddedNavigator != null) {
                embeddedNavigator.go(
                    fxml,
                    title,
                    controllerConsumer == null ? null : controller -> controllerConsumer.accept((T) controller));
                return;
            }

            Stage stage = getStage();
            if (stage == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(Router.class.getResource(fxml));
            Parent root = loader.load();

            if(mainScene == null){
                mainScene = new Scene(root);
                if (Router.class.getResource("/styles/app.css") != null) {
                    mainScene.getStylesheets().add(Router.class.getResource("/styles/app.css").toExternalForm());
                }
                stage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
            }

            if(controllerConsumer != null){
                T controller = loader.getController();
                controllerConsumer.accept(controller);
            }

            stage.setTitle(title);
            stage.show();

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
