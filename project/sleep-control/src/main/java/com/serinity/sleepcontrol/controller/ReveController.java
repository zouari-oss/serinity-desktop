package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.service.ReveService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReveController {

    @FXML private VBox mainContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private CheckBox filterRecurrent;
    @FXML private Button btnAjouter;
    @FXML private Button btnRefresh;
    @FXML private FlowPane cardsContainer;

    // nouveaux champs stats
    @FXML private Label totalRevesLabel;
    @FXML private Label intensiteMoyLabel;
    @FXML private Label anxieteMoyLabel;
    @FXML private ProgressBar anxieteMoyBar;

    private ReveService reveService;
    private List<Reve> currentReves;

    @FXML
    public void initialize() {
        try {
            reveService = new ReveService();
            initializeFilters();
            loadAllReves();
            setupListeners();
            updateStats();
        } catch (Exception e) {
            showError("Erreur de connexion", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void initializeFilters() {
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Normal", "Cauchemar", "Lucide", "Recurrent"
        ));
        filterType.setValue("Tous");

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Plus recent", "Plus ancien",
                "Intensite (elevee)", "Intensite (faible)",
                "Titre (A-Z)", "Titre (Z-A)",
                "Anxiete (elevee)", "Anxiete (faible)"
        ));
        sortComboBox.setValue("Plus recent");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> rechercherReves(newVal));
        filterType.setOnAction(e -> appliquerFiltres());
        filterRecurrent.setOnAction(e -> appliquerFiltres());
        sortComboBox.setOnAction(e -> appliquerTri());
        btnAjouter.setOnAction(e -> ajouterReve());
        btnRefresh.setOnAction(e -> rafraichir());
    }

    @FXML
    public void loadAllReves() {
        try {
            currentReves = reveService.listerTous();
            afficherCards();
            updateStats();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les rêves");
            e.printStackTrace();
        }
    }

    private void afficherCards() {
        cardsContainer.getChildren().clear();

        if (currentReves == null || currentReves.isEmpty()) {
            Label emptyLabel = new Label("Aucun rêve enregistré");
            emptyLabel.getStyleClass().add("empty-label");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Reve reve : currentReves) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/view/fxml/reve-card.fxml")
                );
                VBox card = loader.load();

                ReveCardController cardController = loader.getController();
                cardController.setData(reve, this);

                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void rechercherReves(String critere) {
        try {
            if (critere == null || critere.trim().isEmpty()) {
                loadAllReves();
            } else {
                currentReves = reveService.rechercherDynamique(critere);
                afficherCards();
                updateStats();
            }
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors de la recherche");
            e.printStackTrace();
        }
    }

    @FXML
    private void appliquerFiltres() {
        try {
            String type = filterType.getValue();
            boolean recurrentOnly = filterRecurrent.isSelected();

            if ("Tous".equals(type) && !recurrentOnly) {
                loadAllReves();
                return;
            }

            List<Reve> filtres = reveService.listerTous();

            if (!"Tous".equals(type)) {
                filtres = reveService.filtrerParType(type);
            }

            if (recurrentOnly) {
                currentReves = filtres.stream()
                        .filter(Reve::isRecurrent)
                        .toList();
            } else {
                currentReves = filtres;
            }

            afficherCards();
            updateStats();

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
                case "Plus recent":
                    currentReves = reveService.listerTous();
                    break;
                case "Plus ancien":
                    currentReves = reveService.listerTous();
                    java.util.Collections.reverse(currentReves);
                    break;
                case "Intensite (elevee)":
                    currentReves = reveService.trierParIntensite(false);
                    break;
                case "Intensite (faible)":
                    currentReves = reveService.trierParIntensite(true);
                    break;
                case "Titre (A-Z)":
                    currentReves = reveService.trierParTitre(true);
                    break;
                case "Titre (Z-A)":
                    currentReves = reveService.trierParTitre(false);
                    break;
                case "Anxiete (elevee)":
                    currentReves = reveService.trierParAnxiete(false);
                    break;
                case "Anxiete (faible)":
                    currentReves = reveService.trierParAnxiete(true);
                    break;
            }

            afficherCards();
            updateStats();
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du tri");
            e.printStackTrace();
        }
    }

    public void voirDetailsPublic(Reve reve) {
        voirDetails(reve);
    }

    public void modifierRevePublic(Reve reve) {
        modifierReve(reve);
    }

    public void supprimerRevePublic(Reve reve) {
        supprimerReve(reve);
    }

    private void voirDetails(Reve reve) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Rêve");
        alert.setHeaderText(reve.getTitre());

        String analyse = reveService.analyserReve(reve);
        String details = String.format(
                "%s\n\n" +
                        "Description:\n%s\n\n" +
                        "Type: %s\n" +
                        "Humeur: %s\n" +
                        "Intensité: %d/10\n" +
                        "Anxiété: %d/10\n" +
                        "En couleur: %s\n" +
                        "Récurrent: %s\n\n" +
                        "Émotions: %s\n" +
                        "Symboles: %s",
                analyse,
                reve.getDescription(),
                reve.getTypeReve(),
                reve.getHumeur(),
                reve.getIntensite(),
                reve.calculerNiveauAnxiete(),
                reve.isCouleur() ? "Oui" : "Non",
                reve.isRecurrent() ? "Oui" : "Non",
                reve.getEmotions() != null ? reve.getEmotions() : "Aucune",
                reve.getSymboles() != null ? reve.getSymboles() : "Aucun"
        );

        TextArea textArea = new TextArea(details);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefRowCount(15);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void ajouterReve() {
        ouvrirFormulaire(null);
    }

    private void modifierReve(Reve reve) {
        ouvrirFormulaire(reve);
    }

    private void ouvrirFormulaire(Reve reve) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fxml/reve-form.fxml")
            );
            Parent root = loader.load();

            ReveFormController controller = loader.getController();
            controller.setReveService(reveService);

            if (reve != null) {
                controller.setReve(reve);
            }

            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle(reve == null ? "Ajouter un Rêve" : "Modifier le Rêve");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 600, 650);
            stage.setScene(scene);
            stage.setMinWidth(550);
            stage.setMinHeight(450);

            stage.showAndWait();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    private void supprimerReve(Reve reve) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce rêve ?");
        confirmation.setContentText("Titre: " + reve.getTitre());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reveService.supprimer(reve.getId());
                showSuccess("Succès", "Rêve supprimé avec succès");
                loadAllReves();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer le rêve");
                e.printStackTrace();
            }
        }
    }

    // ---- nouvelles stats graphiques ----
    private void updateStats() {
        try {
            double intensiteMoyenne = reveService.calculerIntensiteMoyenne();
            double anxieteMoyenne = reveService.calculerAnxieteMoyenne();
            int total = currentReves != null ? currentReves.size() : 0;

            totalRevesLabel.setText("Total: " + total + " rêves");
            intensiteMoyLabel.setText(String.format("Intensité moyenne: %.1f/10", intensiteMoyenne));

            anxieteMoyBar.setProgress(anxieteMoyenne / 10.0);
            anxieteMoyLabel.setText(String.format("Anxiété moyenne: %.1f/10", anxieteMoyenne));

            anxieteMoyBar.getStyleClass().removeAll("anxiete-low", "anxiete-mid", "anxiete-high");
            if (anxieteMoyenne >= 7) {
                anxieteMoyBar.getStyleClass().add("anxiete-high");
            } else if (anxieteMoyenne >= 4) {
                anxieteMoyBar.getStyleClass().add("anxiete-mid");
            } else {
                anxieteMoyBar.getStyleClass().add("anxiete-low");
            }

        } catch (SQLException e) {
            totalRevesLabel.setText("Erreur statistiques");
            intensiteMoyLabel.setText("");
            anxieteMoyLabel.setText("");
            anxieteMoyBar.setProgress(0);
            anxieteMoyBar.getStyleClass().removeAll("anxiete-low", "anxiete-mid", "anxiete-high");
        }
    }

    @FXML
    private void rafraichir() {
        searchField.clear();
        filterType.setValue("Tous");
        filterRecurrent.setSelected(false);
        sortComboBox.setValue("Plus recent");
        loadAllReves();
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
