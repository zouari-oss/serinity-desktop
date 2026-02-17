package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.ExerciseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ExerciseListController {

    @FXML private Label lblCount;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<Integer> cbLevel;
    @FXML private FlowPane cardsPane;

    private final ExerciseService exerciseService = new ExerciseService();

    // cache local pour filtrage UI
    private List<Exercise> allExercises = new ArrayList<>();

    @FXML
    public void initialize() {
        // Init combos
        cbType.setItems(FXCollections.observableArrayList("Tous"));
        cbType.getSelectionModel().selectFirst();

        cbLevel.setItems(FXCollections.observableArrayList());
        cbLevel.getItems().add(null); // null = Tous
        cbLevel.getItems().addAll(1, 2, 3, 4, 5);
        cbLevel.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Integer v) { return v == null ? "Tous" : String.valueOf(v); }
            @Override public Integer fromString(String s) { return (s == null || s.equalsIgnoreCase("Tous")) ? null : Integer.valueOf(s); }
        });
        cbLevel.getSelectionModel().selectFirst();

        refresh();
    }

    // ===================== Actions FXML =====================

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onApplyFilters() {
        applyFiltersAndRender();
    }

    @FXML
    private void onClearFilters() {
        cbType.getSelectionModel().selectFirst();   // Tous
        cbLevel.getSelectionModel().selectFirst();  // Tous (null)
        applyFiltersAndRender();
    }

    @FXML
    private void onAddExercise() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            // après sauvegarde: revenir à la liste et refresh
            ctrl.setModeCreateReturnToList(this::refresh);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout.");
        }
    }

    // ===================== Public API (appelée par ExerciseCardController) =====================


    public void openEdit(Exercise ex) {
        if (ex == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeEditReturnToList(ex, this::refresh);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la modification.");
        }
    }


    public void openDetails(Exercise ex) {
        if (ex == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseDetails.fxml"));
            Parent root = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(ex);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les détails.");
        }
    }
    public void deleteExercise(Exercise ex) {
        if (ex == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'exercice : " + ex.getTitle() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            exerciseService.deleteExercise(ex.getId());
            refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur");
            a.setHeaderText(null);
            a.setContentText("Impossible de supprimer.\n" + e.getMessage());
            a.showAndWait();
        }
    }



    public void refresh() {
        try {
            allExercises = exerciseService.getAllExercises();

            // alimenter combo Type dynamiquement (Tous + distinct types)
            Set<String> types = allExercises.stream()
                    .map(Exercise::getType)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));

            String selectedType = cbType.getValue();
            cbType.getItems().setAll("Tous");
            cbType.getItems().addAll(types);

            // garder sélection si possible
            if (selectedType != null && cbType.getItems().contains(selectedType)) {
                cbType.setValue(selectedType);
            } else {
                cbType.getSelectionModel().selectFirst();
            }

            applyFiltersAndRender();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD", "Impossible de charger les exercices.\n" + e.getMessage());
        }
    }

    private void applyFiltersAndRender() {
        String type = cbType.getValue();
        Integer level = cbLevel.getValue();

        List<Exercise> filtered = allExercises.stream()
                .filter(ex -> {
                    boolean okType = (type == null || type.equalsIgnoreCase("Tous"))
                            || (ex.getType() != null && ex.getType().equalsIgnoreCase(type));
                    boolean okLevel = (level == null) || ex.getLevel() == level;
                    return okType && okLevel;
                })
                .collect(Collectors.toList());

        lblCount.setText("(" + filtered.size() + ")");
        renderCards(filtered);
    }

    private void renderCards(List<Exercise> list) {
        cardsPane.getChildren().clear();

        for (Exercise ex : list) {
            cardsPane.getChildren().add(loadExerciseCard(ex));
        }
    }

    private Parent loadExerciseCard(Exercise ex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseCard.fxml"));
            Parent root = loader.load();

            ExerciseCardController ctrl = loader.getController();
            ctrl.setData(ex, this);

            return root;
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement ExerciseCard.fxml", e);
        }
    }



    private void setContent(Parent page) {
        StackPane host = (StackPane) cardsPane.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
