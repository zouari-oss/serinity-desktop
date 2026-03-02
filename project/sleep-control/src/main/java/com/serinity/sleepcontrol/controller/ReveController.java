package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.service.ReveService;
import com.serinity.sleepcontrol.service.SommeilService;
import com.serinity.sleepcontrol.utils.PowerBIConfig;
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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReveController {

    @FXML private VBox             mainContainer;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private CheckBox         filterRecurrent;
    @FXML private Button           btnAjouter;
    @FXML private Button           btnRefresh;
    @FXML private FlowPane         cardsContainer;

    // ✅ KPI
    @FXML private Label            totalRevesLabel;
    @FXML private Label            intensiteMoyLabel;
    @FXML private Label            anxieteMoyLabel;
    @FXML private ProgressBar      anxieteMoyBar;

    private ReveService    reveService;
    private SommeilService sommeilService;
    private List<Reve>     currentReves;

    @FXML
    public void initialize() {
        try {
            reveService    = new ReveService();
            sommeilService = new SommeilService();

            initializeFilters();
            setupListeners();

            loadAllReves();          // charge les cards
            updateGlobalStats();     // ✅ KPI global (tous les rêves)
        } catch (Exception e) {
            showError("Erreur de connexion", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void initializeFilters() {
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Normal", "Cauchemar", "Lucide", "Récurrent"
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
        filterType.setOnAction(e      -> appliquerFiltres());
        filterRecurrent.setOnAction(e -> appliquerFiltres());
        sortComboBox.setOnAction(e    -> appliquerTri());
    }

    // ✅ Power BI
    @FXML
    private void ouvrirStatistiques() {
        ouvrirLien(PowerBIConfig.ADMIN_REVE_MANAGE_URL, "Statistiques Rêve");
        // ou lecture: PowerBIConfig.REVE_URL
    }

    private void ouvrirLien(String url, String nom) {
        try {
            if (url == null || url.isBlank() || url.startsWith("COLLE_ICI")) {
                showError("Lien manquant", "Colle le lien Power BI pour " + nom + " dans PowerBIConfig.");
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showError("Erreur", "Impossible d’ouvrir le navigateur sur cette machine.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d’ouvrir Power BI (" + nom + ").");
        }
    }

    @FXML
    public void loadAllReves() {
        try {
            currentReves = reveService.listerTous();
            afficherCards();
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/reve-card.fxml"));
                VBox card = loader.load();
                ReveCardController cardController = loader.getController();
                cardController.setData(reve, this);
                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Erreur carte : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ✅ KPI GLOBAL: calcule sur TOUS les rêves (pas currentReves)
    private void updateGlobalStats() {
        try {
            List<Reve> all = reveService.listerTous();
            int total = all.size();

            if (total == 0) {
                totalRevesLabel.setText("Total: 0 rêves");
                intensiteMoyLabel.setText("Intensité moyenne: --/10");
                anxieteMoyLabel.setText("Anxiété moyenne: --/10");
                anxieteMoyBar.setProgress(0);
                return;
            }

            double intensiteMoy = all.stream().mapToInt(Reve::getIntensite).average().orElse(0);
            double anxieteMoy   = all.stream().mapToInt(Reve::calculerNiveauAnxiete).average().orElse(0);

            totalRevesLabel.setText("Total: " + total + " rêves");
            intensiteMoyLabel.setText(String.format("Intensité moyenne: %.1f/10", intensiteMoy));
            anxieteMoyLabel.setText(String.format("Anxiété moyenne: %.1f/10", anxieteMoy));
            anxieteMoyBar.setProgress(Math.max(0, Math.min(1, anxieteMoy / 10.0)));

        } catch (SQLException e) {
            totalRevesLabel.setText("Total: --");
            intensiteMoyLabel.setText("Intensité moyenne: --/10");
            anxieteMoyLabel.setText("Anxiété moyenne: --/10");
            anxieteMoyBar.setProgress(0);
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirAnalyseIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/reve-analyse.fxml"));
            Parent root = loader.load();
            ReveAnalyseController ctrl = loader.getController();
            ctrl.setReveService(reveService);
            ctrl.demarrerAnalyseGlobale();

            Stage stage = new Stage();
            stage.setTitle("Analyse IA des Rêves");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 750, 680));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyserRevePublic(Reve reve) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/reve-analyse.fxml"));
            Parent root = loader.load();
            ReveAnalyseController ctrl = loader.getController();
            ctrl.setReveService(reveService);
            ctrl.setReveUnique(reve);

            Stage stage = new Stage();
            stage.setTitle("Analyse IA — " + reve.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 750, 680));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void voirDetailsPublic(Reve reve)   { voirDetails(reve); }
    public void modifierRevePublic(Reve reve)  { modifierReve(reve); }
    public void supprimerRevePublic(Reve reve) { supprimerReve(reve); }

    private void voirDetails(Reve reve) {
        String analyse = reveService.analyserReve(reve);

        String details = String.format(
                "%s\n\nDescription:\n%s\n\nType: %s\nHumeur: %s\nIntensité: %d/10\nAnxiété: %d/10\nEn couleur: %s\nRécurrent: %s\n\nÉmotions: %s\nSymboles: %s",
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

        TextArea ta = new TextArea(details);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefRowCount(15);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Rêve");
        alert.setHeaderText(reve.getTitre());
        alert.getDialogPane().setContent(ta);
        alert.showAndWait();
    }

    private void rechercherReves(String critere) {
        try {
            if (critere == null || critere.trim().isEmpty()) {
                currentReves = reveService.listerTous();
            } else {
                currentReves = reveService.rechercherDynamique(critere);
            }
            afficherCards();
            updateGlobalStats(); // ✅ KPI global reste correct
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

            List<Reve> base = "Tous".equals(type)
                    ? reveService.listerTous()
                    : reveService.filtrerParType(type);

            currentReves = recurrentOnly
                    ? base.stream().filter(Reve::isRecurrent).toList()
                    : base;

            afficherCards();
            updateGlobalStats(); // ✅ KPI global
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du filtrage");
            e.printStackTrace();
        }
    }

    @FXML
    private void appliquerTri() {
        try {
            switch (sortComboBox.getValue()) {
                case "Plus recent"        -> currentReves = reveService.listerTous();
                case "Plus ancien"        -> {
                    currentReves = reveService.listerTous();
                    java.util.Collections.reverse(currentReves);
                }
                case "Intensite (elevee)" -> currentReves = reveService.trierParIntensite(false);
                case "Intensite (faible)" -> currentReves = reveService.trierParIntensite(true);
                case "Titre (A-Z)"        -> currentReves = reveService.trierParTitre(true);
                case "Titre (Z-A)"        -> currentReves = reveService.trierParTitre(false);
                case "Anxiete (elevee)"   -> currentReves = reveService.trierParAnxiete(false);
                case "Anxiete (faible)"   -> currentReves = reveService.trierParAnxiete(true);
            }

            afficherCards();
            updateGlobalStats(); // ✅ KPI global
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du tri");
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouterReve() { ouvrirFormulaire(null); }

    private void modifierReve(Reve reve) { ouvrirFormulaire(reve); }

    private void ouvrirFormulaire(Reve reve) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/reve-form.fxml"));
            Parent root = loader.load();

            ReveFormController controller = loader.getController();
            controller.setReveService(reveService);
            controller.setSommeilService(sommeilService);
            controller.setParentController(this);

            if (reve != null) controller.setReve(reve);

            Stage stage = new Stage();
            stage.setTitle(reve == null ? "Ajouter un Rêve" : "Modifier le Rêve");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 600, 650));
            stage.setMinWidth(550);
            stage.setMinHeight(450);
            stage.showAndWait();

            loadAllReves();
            updateGlobalStats();
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
                updateGlobalStats();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer le rêve");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void rafraichir() {
        searchField.clear();
        filterType.setValue("Tous");
        filterRecurrent.setSelected(false);
        sortComboBox.setValue("Plus recent");
        loadAllReves();
        updateGlobalStats();
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