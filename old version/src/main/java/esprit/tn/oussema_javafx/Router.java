package esprit.tn.oussema_javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    // NORMAL NAVIGATION
    public static void go(String fxml, String title){
        go(fxml,title,null);
    }

    // NAVIGATION WITH DATA (🔥 IMPORTANT)
    public static <T> void go(String fxml, String title, Consumer<T> controllerConsumer){
        try{
            FXMLLoader loader = new FXMLLoader(Router.class.getResource(fxml));
            Parent root = loader.load();

            if(mainScene == null){
                mainScene = new Scene(root);
                mainScene.getStylesheets().add(
                        Router.class.getResource("/css/app.css").toExternalForm()
                );
                primaryStage.setScene(mainScene);
            }else{
                mainScene.setRoot(root);
            }

            // IMPORTANT: get controller AFTER root is set
            if(controllerConsumer != null){
                T controller = loader.getController();
                controllerConsumer.accept(controller);
            }

            primaryStage.setTitle(title);
            primaryStage.show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
