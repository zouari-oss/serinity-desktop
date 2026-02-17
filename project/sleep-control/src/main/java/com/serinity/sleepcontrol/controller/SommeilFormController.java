package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.SommeilService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contrôleur pour le formulaire de sommeil avec validation complète
 */
public class SommeilFormController {

    @FXML private DatePicker dateNuitPicker;
    @FXML private Spinner<Integer> heureCoucherHeure;
    @FXML private Spinner<Integer> heureCoucherMinute;
    @FXML private Spinner<Integer> heureReveilHeure;
    @FXML private Spinner<Integer> heureReveilMinute;
    @FXML private ComboBox<String> qualiteCombo;
    @FXML private TextArea commentaireArea;
    @FXML private Spinner<Integer> interruptionsSpinner;
    @FXML private ComboBox<String> humeurCombo;
    @FXML private ComboBox<String> environnementCombo;
    @FXML private Spinner<Double> temperatureSpinner;
    @FXML private ComboBox<String> bruitCombo;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private SommeilService sommeilService;
    private Sommeil sommeil;
    private SommeilController parentController;

    @FXML
    public void initialize() {
        // Initialiser les ComboBox
        qualiteCombo.setItems(FXCollections.observableArrayList(
                "Excellente", "Bonne", "Moyenne", "Mauvaise"
        ));
        qualiteCombo.setPromptText("Sélectionner...");

        humeurCombo.setItems(FXCollections.observableArrayList(
                "Énergisé", "Reposé", "Neutre", "Fatigué", "Épuisé"
        ));
        humeurCombo.setPromptText("Sélectionner...");

        environnementCombo.setItems(FXCollections.observableArrayList(
                "Calme", "Confortable", "Normal", "Bruyant", "Inconfortable"
        ));
        environnementCombo.setPromptText("Sélectionner...");

        bruitCombo.setItems(FXCollections.observableArrayList(
                "Silencieux", "Léger", "Modéré", "Fort"
        ));
        bruitCombo.setPromptText("Sélectionner...");

        // Initialiser les Spinners
        heureCoucherHeure.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 22));
        heureCoucherMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        heureReveilHeure.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 7));
        heureReveilMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        interruptionsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        temperatureSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10.0, 35.0, 20.0, 0.5));

        // Date par défaut: hier
        dateNuitPicker.setValue(LocalDate.now().minusDays(1));

        // Bloquer les dates futures dans le calendrier
        configurerDatePicker();

        // Rendre les Spinners éditables
        heureCoucherHeure.setEditable(true);
        heureCoucherMinute.setEditable(true);
        heureReveilHeure.setEditable(true);
        heureReveilMinute.setEditable(true);
        interruptionsSpinner.setEditable(true);
        temperatureSpinner.setEditable(true);

        // Validation en temps réel
        setupValidation();
    }

    /**
     * Configure le DatePicker pour bloquer les dates futures
     */
    private void configurerDatePicker() {
        dateNuitPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);

                        // Désactiver toutes les dates après aujourd'hui
                        if (date.isAfter(LocalDate.now())) {
                            setDisable(true);
                            getStyleClass().add("date-cell-disabled");
                        }
                    }
                };
            }
        });
    }

    /**
     * Configure la validation en temps réel
     */
    private void setupValidation() {
        // Date
        dateNuitPicker.valueProperty().addListener((obs, old, newVal) -> validateDate());

        // ComboBox
        qualiteCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(qualiteCombo));
        humeurCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(humeurCombo));
        environnementCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(environnementCombo));
        bruitCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(bruitCombo));

        // Validation initiale
        validateDate();
    }

    /**
     * Valide la date
     */
    private void validateDate() {
        LocalDate date = dateNuitPicker.getValue();

        // Retirer les classes d'erreur
        dateNuitPicker.getStyleClass().removeAll("date-picker-error");

        if (date == null || date.isAfter(LocalDate.now())) {
            dateNuitPicker.getStyleClass().add("date-picker-error");
        }
    }

    /**
     * Valide un ComboBox
     */
    private void validateComboBox(ComboBox<String> combo) {
        // Retirer les classes d'erreur
        combo.getStyleClass().removeAll("combo-box-error");

        if (combo.getValue() == null) {
            combo.getStyleClass().add("combo-box-error");
        }
    }

    public void setSommeilService(SommeilService service) {
        this.sommeilService = service;
    }

    public void setParentController(SommeilController parent) {
        this.parentController = parent;
    }

    /**
     * Charge les données pour modification
     */
    public void setSommeil(Sommeil sommeil) {
        this.sommeil = sommeil;

        dateNuitPicker.setValue(sommeil.getDateNuit());

        heureCoucherHeure.getValueFactory().setValue(sommeil.getHeureCoucher().getHour());
        heureCoucherMinute.getValueFactory().setValue(sommeil.getHeureCoucher().getMinute());

        heureReveilHeure.getValueFactory().setValue(sommeil.getHeureReveil().getHour());
        heureReveilMinute.getValueFactory().setValue(sommeil.getHeureReveil().getMinute());

        qualiteCombo.setValue(sommeil.getQualite());
        commentaireArea.setText(sommeil.getCommentaire());
        interruptionsSpinner.getValueFactory().setValue(sommeil.getInterruptions());
        humeurCombo.setValue(sommeil.getHumeurReveil());
        environnementCombo.setValue(sommeil.getEnvironnement());
        temperatureSpinner.getValueFactory().setValue(sommeil.getTemperature());
        bruitCombo.setValue(sommeil.getNiveauBruit());
    }

    /**
     * Sauvegarde le sommeil
     */
    @FXML
    private void sauvegarder() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            if (sommeil == null) {
                sommeil = new Sommeil();
            }

            // Récupération des valeurs
            LocalDate date = dateNuitPicker.getValue();
            LocalTime heureCoucher = LocalTime.of(
                    heureCoucherHeure.getValue(),
                    heureCoucherMinute.getValue()
            );
            LocalTime heureReveil = LocalTime.of(
                    heureReveilHeure.getValue(),
                    heureReveilMinute.getValue()
            );

            sommeil.setDateNuit(date);
            sommeil.setHeureCoucher(heureCoucher);
            sommeil.setHeureReveil(heureReveil);
            sommeil.setQualite(qualiteCombo.getValue());
            sommeil.setCommentaire(commentaireArea.getText());
            sommeil.setInterruptions(interruptionsSpinner.getValue());
            sommeil.setHumeurReveil(humeurCombo.getValue());
            sommeil.setEnvironnement(environnementCombo.getValue());
            sommeil.setTemperature(temperatureSpinner.getValue());
            sommeil.setNiveauBruit(bruitCombo.getValue());

            // Sauvegarder
            if (sommeil.getId() == 0) {
                sommeilService.creer(sommeil);
                showSuccess("Sommeil ajouté avec succès!");
            } else {
                sommeilService.modifier(sommeil);
                showSuccess("Sommeil modifié avec succès!");
            }

            if (parentController != null) {
                parentController.loadAllSommeils();
            }

            fermer();

        } catch (SQLException e) {
            showError("Erreur de sauvegarde", "Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide le formulaire avec messages détaillés
     */
    private boolean validerFormulaire() {
        StringBuilder errors = new StringBuilder();

        // 1. Date
        LocalDate date = dateNuitPicker.getValue();
        if (date == null) {
            errors.append("- La date de nuit est obligatoire\n");
        } else if (date.isAfter(LocalDate.now())) {
            errors.append("- La date ne peut pas être dans le futur\n");
        }

        // 2. Heures
        LocalTime heureCoucher = null;
        LocalTime heureReveil = null;

        try {
            heureCoucher = LocalTime.of(
                    heureCoucherHeure.getValue(),
                    heureCoucherMinute.getValue()
            );
        } catch (Exception e) {
            errors.append("- L'heure de coucher est invalide\n");
        }

        try {
            heureReveil = LocalTime.of(
                    heureReveilHeure.getValue(),
                    heureReveilMinute.getValue()
            );
        } catch (Exception e) {
            errors.append("- L'heure de réveil est invalide\n");
        }

        // 3. Validation logique des heures
        if (heureCoucher != null && heureReveil != null) {
            if (heureCoucher.equals(heureReveil)) {
                errors.append("- L'heure de coucher et de réveil ne peuvent pas être identiques\n");
            }

            // Vérification de durée raisonnable
            long minutes;
            if (heureReveil.isBefore(heureCoucher)) {
                // Nuit à cheval (ex: coucher 23h, réveil 6h)
                minutes = java.time.Duration.between(heureCoucher, LocalTime.MAX).toMinutes()
                        + java.time.Duration.between(LocalTime.MIN, heureReveil).toMinutes();
            } else {
                minutes = java.time.Duration.between(heureCoucher, heureReveil).toMinutes();
            }

            double heures = minutes / 60.0;
            if (heures < 1) {
                errors.append("- La durée du sommeil est trop courte (moins d'1 heure)\n");
            } else if (heures > 20) {
                errors.append("- La durée du sommeil est trop longue (plus de 20 heures)\n");
            }
        }

        // 4. Qualité
        if (qualiteCombo.getValue() == null) {
            errors.append("- La qualité du sommeil est obligatoire\n");
        }

        // 5. Humeur
        if (humeurCombo.getValue() == null) {
            errors.append("- L'humeur au réveil est obligatoire\n");
        }

        // 6. Environnement
        if (environnementCombo.getValue() == null) {
            errors.append("- L'environnement est obligatoire\n");
        }

        // 7. Niveau de bruit
        if (bruitCombo.getValue() == null) {
            errors.append("- Le niveau de bruit est obligatoire\n");
        }

        // 8. Interruptions
        int interruptions = interruptionsSpinner.getValue();
        if (interruptions < 0 || interruptions > 20) {
            errors.append("- Le nombre d'interruptions doit être entre 0 et 20\n");
        }

        // 9. Température
        double temperature = temperatureSpinner.getValue();
        if (temperature < 10 || temperature > 35) {
            errors.append("- La température doit être entre 10 et 35 degrés Celsius\n");
        }

        // 10. Commentaire (optionnel mais limite de longueur)
        String commentaire = commentaireArea.getText();
        if (commentaire != null && commentaire.length() > 500) {
            errors.append("- Le commentaire ne doit pas dépasser 500 caractères\n");
        }

        // Afficher les erreurs
        if (errors.length() > 0) {
            showError("Formulaire invalide", "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Annule et ferme
     */
    @FXML
    private void annuler() {
        fermer();
    }

    /**
     * Ferme la fenêtre
     */
    private void fermer() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
