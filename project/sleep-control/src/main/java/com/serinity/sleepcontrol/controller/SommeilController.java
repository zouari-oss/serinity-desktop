package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.SommeilService;
import com.serinity.sleepcontrol.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SommeilController {

    @FXML private VBox mainContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterQualite;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnAjouter;
    @FXML private Button btnRefresh;
    @FXML private FlowPane cardsContainer;
    @FXML private Label statsLabel;

    private SommeilService sommeilService;
    private List<Sommeil> currentSommeils;

    @FXML
    public void initialize() {
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            sommeilService = new SommeilService(connection);

            initializeFilters();
            loadAllSommeils();
            setupListeners();
            updateStats();

        } catch (Exception e) {
            showError("Erreur de connexion", "Impossible de se connecter a la base de donnees");
            e.printStackTrace();
        }
    }

    private void initializeFilters() {
        filterQualite.setItems(FXCollections.observableArrayList(
                "Toutes", "Excellente", "Bonne", "Moyenne", "Mauvaise"
        ));
        filterQualite.setValue("Toutes");

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Date (recent)", "Date (ancien)",
                "Duree (croissant)", "Duree (decroissant)",
                "Qualite (meilleure)", "Qualite (pire)",
                "Score (eleve)", "Score (faible)"
        ));
        sortComboBox.setValue("Date (recent)");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            rechercherSommeils(newVal);
        });

        filterQualite.setOnAction(e -> appliquerFiltres());
        sortComboBox.setOnAction(e -> appliquerTri());
    }

    @FXML
    public void loadAllSommeils() {
        try {
            currentSommeils = sommeilService.listerTousAvecReves();
            afficherCards();
            updateStats();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les sommeils");
            e.printStackTrace();
        }
    }

    private void afficherCards() {
        cardsContainer.getChildren().clear();

        if (currentSommeils.isEmpty()) {
            Label emptyLabel = new Label("Aucun sommeil enregistre");
            emptyLabel.getStyleClass().add("empty-label");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Sommeil sommeil : currentSommeils) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/fxml/sommeil-card.fxml")
                );
                VBox card = loader.load();

                SommeilCardController cardController = loader.getController();
                cardController.setData(sommeil, this);

                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void rechercherSommeils(String critere) {
        try {
            if (critere == null || critere.trim().isEmpty()) {
                loadAllSommeils();
            } else {
                currentSommeils = sommeilService.rechercherDynamique(critere);
                afficherCards();
            }
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors de la recherche");
            e.printStackTrace();
        }
    }

    @FXML
    private void appliquerFiltres() {
        try {
            String qualite = filterQualite.getValue();

            if ("Toutes".equals(qualite)) {
                loadAllSommeils();
            } else {
                currentSommeils = sommeilService.filtrerParQualite(qualite);
                afficherCards();
            }
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du filtrage");
            e.printStackTrace();
        }
    }

    @FXML
    private void appliquerTri() {
        try {
            String triOption = sortComboBox.getValue();

            switch (triOption) {
                case "Date (recent)":
                    currentSommeils = sommeilService.trierParDate(false);
                    break;
                case "Date (ancien)":
                    currentSommeils = sommeilService.trierParDate(true);
                    break;
                case "Duree (croissant)":
                    currentSommeils = sommeilService.trierParDuree(true);
                    break;
                case "Duree (decroissant)":
                    currentSommeils = sommeilService.trierParDuree(false);
                    break;
                case "Qualite (meilleure)":
                    currentSommeils = sommeilService.trierParQualite(false);
                    break;
                case "Qualite (pire)":
                    currentSommeils = sommeilService.trierParQualite(true);
                    break;
                case "Score (eleve)":
                    currentSommeils = sommeilService.trierParScore(false);
                    break;
                case "Score (faible)":
                    currentSommeils = sommeilService.trierParScore(true);
                    break;
            }

            afficherCards();
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du tri");
            e.printStackTrace();
        }
    }

    public void voirDetailsPublic(Sommeil sommeil) {
        voirDetails(sommeil);
    }

    public void modifierSommeilPublic(Sommeil sommeil) {
        modifierSommeil(sommeil);
    }

    public void supprimerSommeilPublic(Sommeil sommeil) {
        supprimerSommeil(sommeil);
    }

    private void voirDetails(Sommeil sommeil) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details du Sommeil");
        alert.setHeaderText("Nuit du " +
                sommeil.getDateNuit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String analyse = sommeilService.analyserQualiteSommeil(sommeil);
        String details = String.format(
                "%s\n\n" +
                        "Coucher: %s\n" +
                        "Reveil: %s\n" +
                        "Duree: %.2f heures\n" +
                        "Interruptions: %d\n" +
                        "Humeur: %s\n" +
                        "Temperature: %.1f C\n" +
                        "Niveau de bruit: %s\n" +
                        "Environnement: %s\n" +
                        "Nombre de reves: %d\n\n" +
                        "Commentaire:\n%s",
                analyse,
                sommeil.getHeureCoucher(),
                sommeil.getHeureReveil(),
                sommeil.getDureeSommeil(),
                sommeil.getInterruptions(),
                sommeil.getHumeurReveil(),
                sommeil.getTemperature(),
                sommeil.getNiveauBruit(),
                sommeil.getEnvironnement(),
                sommeil.getNombreReves(),
                sommeil.getCommentaire() != null ? sommeil.getCommentaire() : "Aucun"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    @FXML
    private void ajouterSommeil() {
        ouvrirFormulaire(null);
    }

    private void modifierSommeil(Sommeil sommeil) {
        ouvrirFormulaire(sommeil);
    }

    private void ouvrirFormulaire(Sommeil sommeil) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/sommeil-form.fxml")
            );
            Parent root = loader.load();

            SommeilFormController controller = loader.getController();
            controller.setSommeilService(sommeilService);

            if (sommeil != null) {
                controller.setSommeil(sommeil);
            }

            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle(sommeil == null ? "Ajouter un Sommeil" : "Modifier le Sommeil");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 550, 600);
            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(400);

            stage.showAndWait();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    private void supprimerSommeil(Sommeil sommeil) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce sommeil ?");
        confirmation.setContentText(
                "Cette action supprimera egalement tous les reves associes.\n" +
                        "Date: " + sommeil.getDateNuit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                sommeilService.supprimer(sommeil.getId());
                showSuccess("Succes", "Sommeil supprime avec succes");
                loadAllSommeils();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer le sommeil");
                e.printStackTrace();
            }
        }
    }

    private void updateStats() {
        try {
            double dureeMoyenne = sommeilService.calculerDureeMoyenne();
            double scoreMoyen = sommeilService.calculerScoreMoyen();
            int total = currentSommeils != null ? currentSommeils.size() : 0;

            statsLabel.setText(String.format(
                    "Total: %d nuits | Duree moyenne: %.2fh | Score moyen: %.0f/100",
                    total, dureeMoyenne, scoreMoyen
            ));
        } catch (SQLException e) {
            statsLabel.setText("Erreur lors du calcul des statistiques");
        }
    }

    @FXML
    private void rafraichir() {
        searchField.clear();
        filterQualite.setValue("Toutes");
        sortComboBox.setValue("Date (recent)");
        loadAllSommeils();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
