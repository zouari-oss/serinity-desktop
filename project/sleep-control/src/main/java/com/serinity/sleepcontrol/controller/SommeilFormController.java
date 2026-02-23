package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.SommeilService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class SommeilFormController {

    // ─── Champs FXML ────────────────────────────────────────────────────────────

    @FXML private ScrollPane rootScrollPane;
    @FXML private VBox mainContainer;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label headerIcon;

    @FXML private DatePicker dateNuitPicker;

    @FXML private Spinner<Integer> heureCoucherHeure;
    @FXML private Spinner<Integer> heureCoucherMinute;
    @FXML private Spinner<Integer> heureReveilHeure;
    @FXML private Spinner<Integer> heureReveilMinute;

    @FXML private ComboBox<String> qualiteCombo;
    @FXML private ComboBox<String> humeurCombo;
    @FXML private ComboBox<String> environnementCombo;
    @FXML private ComboBox<String> bruitCombo;

    @FXML private Spinner<Integer> interruptionsSpinner;
    @FXML private Spinner<Double> temperatureSpinner;

    @FXML private TextArea commentaireArea;
    @FXML private Label charCountLabel;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ─── Champs internes ────────────────────────────────────────────────────────

    private SommeilService sommeilService;
    private Sommeil sommeil;
    private SommeilController parentController;

    private static final int MAX_COMMENTAIRE = 500;

    // ─── Couleurs du thème ──────────────────────────────────────────────────────

    private static final String COLOR_BLUE        = "#4a90e2";
    private static final String COLOR_GREEN        = "#27ae60";
    private static final String COLOR_ORANGE       = "#e67e22";
    private static final String COLOR_PURPLE       = "#9b59b6";
    private static final String COLOR_ERROR        = "#e74c3c";
    private static final String COLOR_ERROR_BG     = "#fdf2f2";
    private static final String COLOR_SUCCESS_BG   = "#f0fff4";
    private static final String COLOR_SUCCESS      = "#27ae60";

    // ─── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        initComboBoxes();
        initSpinners();
        initDatePicker();
        initCommentaireCounter();
        setupValidation();
        setupButtonEffects();
        applyEntranceAnimation();
    }

    private void initComboBoxes() {
        qualiteCombo.setItems(FXCollections.observableArrayList(
                "⭐⭐⭐⭐ Excellente",
                "⭐⭐⭐ Bonne",
                "⭐⭐ Moyenne",
                "⭐ Mauvaise"
        ));
        qualiteCombo.setPromptText("Sélectionner...");

        humeurCombo.setItems(FXCollections.observableArrayList(
                "⚡ Énergisé",
                "😌 Reposé",
                "😐 Neutre",
                "😴 Fatigué",
                "😵 Épuisé"
        ));
        humeurCombo.setPromptText("Sélectionner...");

        environnementCombo.setItems(FXCollections.observableArrayList(
                "🌿 Calme",
                "😊 Confortable",
                "🏠 Normal",
                "📢 Bruyant",
                "😣 Inconfortable"
        ));
        environnementCombo.setPromptText("Sélectionner...");

        bruitCombo.setItems(FXCollections.observableArrayList(
                "🔇 Silencieux",
                "🔉 Léger",
                "🔉 Modéré",
                "🔊 Fort"
        ));
        bruitCombo.setPromptText("Sélectionner...");
    }

    private void initSpinners() {
        heureCoucherHeure.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 22));
        heureCoucherMinute.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        heureReveilHeure.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 7));
        heureReveilMinute.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        interruptionsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        temperatureSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(10.0, 35.0, 20.0, 0.5));

        // Rendre tous les spinners éditables
        heureCoucherHeure.setEditable(true);
        heureCoucherMinute.setEditable(true);
        heureReveilHeure.setEditable(true);
        heureReveilMinute.setEditable(true);
        interruptionsSpinner.setEditable(true);
        temperatureSpinner.setEditable(true);

        // Committer la valeur en tapant dans un spinner
        addSpinnerCommitOnEdit(heureCoucherHeure);
        addSpinnerCommitOnEdit(heureCoucherMinute);
        addSpinnerCommitOnEdit(heureReveilHeure);
        addSpinnerCommitOnEdit(heureReveilMinute);
        addSpinnerCommitOnEdit(interruptionsSpinner);
    }

    /** Force le spinner à appliquer la valeur saisie au clavier. */
    private <T> void addSpinnerCommitOnEdit(Spinner<T> spinner) {
        spinner.getEditor().setOnAction(e -> {
            SpinnerValueFactory<T> factory = spinner.getValueFactory();
            StringConverter<T> converter = factory.getConverter();
            try {
                T newValue = converter.fromString(spinner.getEditor().getText());
                factory.setValue(newValue);
            } catch (Exception ignored) {
                spinner.getEditor().setText(
                        factory.getConverter().toString(factory.getValue()));
            }
        });
    }

    private void initDatePicker() {
        dateNuitPicker.setValue(LocalDate.now().minusDays(1));

        // Désactiver les dates futures
        dateNuitPicker.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (date.isAfter(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #ccc;");
                        }
                    }
                };
            }
        });
    }

    /** Initialise le compteur de caractères pour le commentaire. */
    private void initCommentaireCounter() {
        updateCharCount(0);
        commentaireArea.textProperty().addListener((obs, old, newVal) -> {
            int length = newVal == null ? 0 : newVal.length();
            updateCharCount(length);

            // Limiter à MAX_COMMENTAIRE caractères
            if (length > MAX_COMMENTAIRE) {
                commentaireArea.setText(newVal.substring(0, MAX_COMMENTAIRE));
                commentaireArea.positionCaret(MAX_COMMENTAIRE);
            }
        });
    }

    private void updateCharCount(int length) {
        if (charCountLabel != null) {
            charCountLabel.setText(length + " / " + MAX_COMMENTAIRE + " caractères");
            if (length > MAX_COMMENTAIRE * 0.9) {
                charCountLabel.setStyle("-fx-text-fill: " + COLOR_ERROR + "; -fx-font-size: 11px;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-style: italic;");
            }
        }
    }

    // ─── Validation visuelle ────────────────────────────────────────────────────

    private void setupValidation() {
        dateNuitPicker.valueProperty().addListener((obs, old, newVal) -> validateField(
                dateNuitPicker,
                newVal != null && !newVal.isAfter(LocalDate.now()),
                "-fx-border-color: " + COLOR_BLUE + "; -fx-border-radius: 8;",
                "-fx-border-color: " + COLOR_ERROR + "; -fx-border-width: 2; -fx-border-radius: 8;"
        ));

        qualiteCombo.valueProperty().addListener((obs, old, newVal) -> validateField(
                qualiteCombo, newVal != null,
                "-fx-border-color: " + COLOR_GREEN + "; -fx-border-radius: 8;",
                "-fx-border-color: " + COLOR_ERROR + "; -fx-border-width: 2; -fx-border-radius: 8;"
        ));

        humeurCombo.valueProperty().addListener((obs, old, newVal) -> validateField(
                humeurCombo, newVal != null,
                "-fx-border-color: " + COLOR_GREEN + "; -fx-border-radius: 8;",
                "-fx-border-color: " + COLOR_ERROR + "; -fx-border-width: 2; -fx-border-radius: 8;"
        ));

        environnementCombo.valueProperty().addListener((obs, old, newVal) -> validateField(
                environnementCombo, newVal != null,
                "-fx-border-color: " + COLOR_ORANGE + "; -fx-border-radius: 8;",
                "-fx-border-color: " + COLOR_ERROR + "; -fx-border-width: 2; -fx-border-radius: 8;"
        ));

        bruitCombo.valueProperty().addListener((obs, old, newVal) -> validateField(
                bruitCombo, newVal != null,
                "-fx-border-color: " + COLOR_ORANGE + "; -fx-border-radius: 8;",
                "-fx-border-color: " + COLOR_ERROR + "; -fx-border-width: 2; -fx-border-radius: 8;"
        ));
    }

    private void validateField(Control field, boolean isValid,
                               String validStyle, String errorStyle) {
        field.setStyle(isValid ? validStyle : errorStyle);
    }

    // ─── Effets visuels sur les boutons ─────────────────────────────────────────

    private void setupButtonEffects() {
        addHoverEffect(btnSave,
                "-fx-background-color: linear-gradient(to right, #229954, #1e8449); " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;",
                "-fx-background-color: linear-gradient(to right, #27ae60, #229954); " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        addHoverEffect(btnCancel,
                "-fx-background-color: #bdc3c7; -fx-text-fill: #5d6d7e; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;",
                "-fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 25; -fx-cursor: hand;"
        );

        // Ombre sur le bouton sauvegarder
        DropShadow saveShadow = new DropShadow();
        saveShadow.setColor(Color.web("#27ae6066"));
        saveShadow.setRadius(10);
        saveShadow.setOffsetY(3);
        btnSave.setEffect(saveShadow);
    }

    private void addHoverEffect(Button btn, String hoverStyle, String normalStyle) {
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(normalStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    /** Animation d'apparition du formulaire au chargement. */
    private void applyEntranceAnimation() {
        if (mainContainer != null) {
            mainContainer.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(400), mainContainer);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    // ─── API publique ────────────────────────────────────────────────────────────

    public void setSommeilService(SommeilService service) {
        this.sommeilService = service;
    }

    public void setParentController(SommeilController parent) {
        this.parentController = parent;
    }

    /**
     * Pré-remplit le formulaire en mode modification.
     * Met aussi à jour le titre et l'icône de l'en-tête.
     */
    public void setSommeil(Sommeil sommeil) {
        this.sommeil = sommeil;

        // Mettre à jour le titre en mode "Modifier"
        if (titleLabel != null) {
            titleLabel.setText("Modifier le Sommeil");
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText("Modifiez les détails de votre nuit");
        }
        if (headerIcon != null) {
            headerIcon.setText("✏️");
        }
        if (btnSave != null) {
            btnSave.setText("✓ Modifier");
        }

        // Remplissage des champs
        dateNuitPicker.setValue(sommeil.getDateNuit());

        heureCoucherHeure.getValueFactory().setValue(sommeil.getHeureCoucher().getHour());
        heureCoucherMinute.getValueFactory().setValue(sommeil.getHeureCoucher().getMinute());

        heureReveilHeure.getValueFactory().setValue(sommeil.getHeureReveil().getHour());
        heureReveilMinute.getValueFactory().setValue(sommeil.getHeureReveil().getMinute());

        qualiteCombo.setValue(sommeil.getQualite());
        commentaireArea.setText(sommeil.getCommentaire() != null ? sommeil.getCommentaire() : "");
        interruptionsSpinner.getValueFactory().setValue(sommeil.getInterruptions());
        humeurCombo.setValue(sommeil.getHumeurReveil());
        environnementCombo.setValue(sommeil.getEnvironnement());
        temperatureSpinner.getValueFactory().setValue(sommeil.getTemperature());
        bruitCombo.setValue(sommeil.getNiveauBruit());
    }

    // ─── Actions FXML ────────────────────────────────────────────────────────────

    @FXML
    private void sauvegarder() {
        if (!validerFormulaire()) return;

        try {
            if (sommeil == null) sommeil = new Sommeil();

            sommeil.setDateNuit(dateNuitPicker.getValue());
            sommeil.setHeureCoucher(LocalTime.of(
                    heureCoucherHeure.getValue(), heureCoucherMinute.getValue()));
            sommeil.setHeureReveil(LocalTime.of(
                    heureReveilHeure.getValue(), heureReveilMinute.getValue()));
            sommeil.setQualite(qualiteCombo.getValue());
            sommeil.setCommentaire(commentaireArea.getText());
            sommeil.setInterruptions(interruptionsSpinner.getValue());
            sommeil.setHumeurReveil(humeurCombo.getValue());
            sommeil.setEnvironnement(environnementCombo.getValue());
            sommeil.setTemperature(temperatureSpinner.getValue());
            sommeil.setNiveauBruit(bruitCombo.getValue());

            boolean isNew = (sommeil.getId() == 0);
            if (isNew) {
                sommeilService.creer(sommeil);
                showStyledSuccess("🌙 Ajout réussi !", "Votre nuit a été enregistrée avec succès.");
            } else {
                sommeilService.modifier(sommeil);
                showStyledSuccess("✏️ Modification réussie !", "Les données ont été mises à jour.");
            }

            if (parentController != null) {
                parentController.loadAllSommeils();
            }
            fermer();

        } catch (SQLException e) {
            showStyledError("Erreur de base de données",
                    "Impossible de sauvegarder.\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showStyledError("Erreur inattendue",
                    "Une erreur s'est produite.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    // ─── Validation métier ───────────────────────────────────────────────────────

    private boolean validerFormulaire() {
        StringBuilder errors = new StringBuilder();

        // Date
        LocalDate date = dateNuitPicker.getValue();
        if (date == null) {
            errors.append("📅  La date de nuit est obligatoire\n");
        } else if (date.isAfter(LocalDate.now())) {
            errors.append("📅  La date ne peut pas être dans le futur\n");
        }

        // Heures
        LocalTime heureCoucher = null;
        LocalTime heureReveil  = null;

        try {
            heureCoucher = LocalTime.of(
                    heureCoucherHeure.getValue(), heureCoucherMinute.getValue());
        } catch (Exception e) {
            errors.append("🛏️  L'heure de coucher est invalide\n");
        }

        try {
            heureReveil = LocalTime.of(
                    heureReveilHeure.getValue(), heureReveilMinute.getValue());
        } catch (Exception e) {
            errors.append("⏰  L'heure de réveil est invalide\n");
        }

        if (heureCoucher != null && heureReveil != null) {
            if (heureCoucher.equals(heureReveil)) {
                errors.append("⏱️  L'heure de coucher et de réveil ne peuvent pas être identiques\n");
            } else {
                long minutes;
                if (heureReveil.isBefore(heureCoucher)) {
                    minutes = java.time.Duration.between(heureCoucher, LocalTime.MAX).toMinutes()
                            + java.time.Duration.between(LocalTime.MIN, heureReveil).toMinutes();
                } else {
                    minutes = java.time.Duration.between(heureCoucher, heureReveil).toMinutes();
                }
                double heures = minutes / 60.0;
                if (heures < 1) {
                    errors.append("⏱️  Durée trop courte (moins d'1 heure)\n");
                } else if (heures > 20) {
                    errors.append("⏱️  Durée trop longue (plus de 20 heures)\n");
                }
            }
        }

        // ComboBoxes obligatoires
        if (qualiteCombo.getValue() == null)
            errors.append("⭐  La qualité du sommeil est obligatoire\n");
        if (humeurCombo.getValue() == null)
            errors.append("😊  L'humeur au réveil est obligatoire\n");
        if (environnementCombo.getValue() == null)
            errors.append("🛋️  L'environnement est obligatoire\n");
        if (bruitCombo.getValue() == null)
            errors.append("🔊  Le niveau de bruit est obligatoire\n");

        // Interruptions
        int interruptions = interruptionsSpinner.getValue();
        if (interruptions < 0 || interruptions > 20)
            errors.append("🔄  Les interruptions doivent être entre 0 et 20\n");

        // Température
        double temperature = temperatureSpinner.getValue();
        if (temperature < 10 || temperature > 35)
            errors.append("🌡️  La température doit être entre 10 et 35 °C\n");

        // Commentaire
        String commentaire = commentaireArea.getText();
        if (commentaire != null && commentaire.length() > MAX_COMMENTAIRE)
            errors.append("📝  Le commentaire ne doit pas dépasser " + MAX_COMMENTAIRE + " caractères\n");

        if (errors.length() > 0) {
            showStyledError("Formulaire invalide",
                    "Veuillez corriger :\n\n" + errors);
            return false;
        }

        return true;
    }

    // ─── Utilitaires UI ─────────────────────────────────────────────────────────

    private void fermer() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();

        // Animation de fermeture
        FadeTransition ft = new FadeTransition(Duration.millis(200),
                btnCancel.getScene().getRoot());
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> stage.close());
        ft.play();
    }

    /** Alerte d'erreur avec icône et style rouge. */
    private void showStyledError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌  " + title);
        alert.setContentText(message);
        styleAlert(alert, COLOR_ERROR_BG, COLOR_ERROR);
        alert.showAndWait();
    }

    /** Alerte de succès avec icône et style vert. */
    private void showStyledSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert, COLOR_SUCCESS_BG, COLOR_SUCCESS);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert, String bgColor, String accentColor) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: " + accentColor + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
        dp.lookup(".header-panel")
                .setStyle("-fx-background-color: " + bgColor + ";");
        dp.lookup(".content.label")
                .setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
    }
}
