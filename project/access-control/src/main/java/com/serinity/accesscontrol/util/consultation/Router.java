package com.serinity.accesscontrol.util.consultation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class Router {

    private static Stage primaryStage;
    private static Scene mainScene;

    public static void init(Stage stage){
        primaryStage = stage;
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }

    private static Stage getStage() {
        if (primaryStage != null) return primaryStage;
        return Window.getWindows().stream()
            .filter(w -> w instanceof Stage && w.isShowing())
            .map(w -> (Stage) w)
            .findFirst()
            .orElse(null);
    }

    // NORMAL NAVIGATION
    public static void go(String fxml, String title){
        go(fxml,title,null);
    }

    // NAVIGATION WITH DATA (🔥 IMPORTANT)
    public static <T> void go(String fxml, String title, Consumer<T> controllerConsumer){
        try{
            Stage stage = getStage();
            if (stage == null) return;

            FXMLLoader loader = new FXMLLoader(Router.class.getResource(fxml));
            Parent root = loader.load();

            if(mainScene == null){
                mainScene = new Scene(root);
                String css = Router.class.getResource("/styles/app.css") != null
                    ? Router.class.getResource("/styles/app.css").toExternalForm() : null;
                if (css != null) mainScene.getStylesheets().add(css);
                stage.setScene(mainScene);
            }else{
                mainScene.setRoot(root);
            }

            // IMPORTANT: get controller AFTER root is set
            if(controllerConsumer != null){
                T controller = loader.getController();
                controllerConsumer.accept(controller);
            }

            stage.setTitle(title);
            stage.show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
