package com.serinity.exercicecontrol.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

public final class PageNavigator {

    private PageNavigator() {}

    public static <T> void openInTemplate(Node anyNodeInScene, String fxmlPath, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(PageNavigator.class.getResource(fxmlPath));
            Parent page = loader.load();

            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            if (controllerSetup != null) controllerSetup.accept(controller);

            StackPane host = (StackPane) anyNodeInScene.getScene().lookup("#contentHost");
            if (host == null) {
                host = (StackPane) anyNodeInScene.getScene().lookup("#contentHostStackPane");
            }
            if (host == null) {
                throw new IllegalStateException("contentHost/contentHostStackPane introuvable. Vérifie le shell FXML.");
            }
            host.getChildren().setAll(page);

        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement: " + fxmlPath, e);
        }
    }
}
