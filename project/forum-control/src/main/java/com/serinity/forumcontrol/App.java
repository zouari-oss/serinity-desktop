package com.serinity.forumcontrol;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {
    private static Locale forcedLocale = null;
    public static void setForcedLocale(Locale locale) {
        forcedLocale = locale;
    }
    public static Locale getEffectiveLocale() {
        return (forcedLocale != null) ? forcedLocale : Locale.getDefault();
    }

    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws Exception {

        final Locale locale = getEffectiveLocale();

        final ResourceBundle bundle =
                ResourceBundle.getBundle("i18n.messages", locale);

        final FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fxml/Template.fxml"), bundle);

        final Parent root = loader.load();

        // ymin lel ysar
        // if ("ar".equals(locale.getLanguage())) {
        //     root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        // }

        final Scene scene = new Scene(root);

        stage.setTitle("SERINITY");
        stage.setScene(scene);
        stage.show();
    }

}
