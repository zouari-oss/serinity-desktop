package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.ReveService;
import com.serinity.sleepcontrol.service.SommeilService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReveFormController {

    // ─── Champs FXML ────────────────────────────────────────────────────────────

    @FXML private VBox mainContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label headerIcon;

    @FXML private ComboBox<Sommeil> sommeilCombo;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Label descCharCountLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> humeurCombo;
    @FXML private Slider intensiteSlider;
    @FXML private Label intensiteLabel;
    @FXML private CheckBox couleurCheck;
    @FXML private CheckBox recurrentCheck;
    @FXML private TextField emotionsField;
    @FXML private TextField symbolesField;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ─── Champs internes ────────────────────────────────────────────────────────

    private ReveService reveService;
    private SommeilService sommeilService;
    private Reve reve;
    private ReveController parentController;

    private static final int MAX_DESCRIPTION = 2000;
    private static final int MAX_TITRE       = 100;
    private static final int MAX_OPTIONAL    = 200;

    // ─── Groupes de classes CSS pour reset propre ────────────────────────────────

    private static final List<String> VALIDATION_CLASSES = List.of(
            "field-default", "field-valid-purple", "field-valid-blue",
            "field-valid-green", "field-valid-orange", "field-error", "field-warn"
    );

    private static final List<String> INTENSITE_CLASSES = List.of(
            "intensite-low", "intensite-medium", "intensite-high"
    );

    // ─── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        initComboBoxes();
        initSlider();
        initDescriptionCounter();
        setupButtonEffects();
        applyEntranceAnimation();
        couleurCheck.setSelected(true);
    }

    private void initComboBoxes() {
        typeCombo.setItems(FXCollections.observableArrayList(
                "💭 Normal", "😱 Cauchemar", "🌟 Lucide", "🔁 Récurrent"
        ));
        typeCombo.setPromptText("Sélectionner...");

        humeurCombo.setItems(FXCollections.observableArrayList(
                "😄 Joyeux", "😢 Triste", "😰 Anxieux", "😐 Neutre",
                "🤩 Excité", "😨 Effrayé", "😌 Paisible"
        ));
        humeurCombo.setPromptText("Sélectionner...");
    }

    private void initSlider() {
        intensiteSlider.setMin(1);
        intensiteSlider.setMax(10);
        intensiteSlider.setValue(5);
        intensiteSlider.setMajorTickUnit(1);
        intensiteSlider.setMinorTickCount(0);
        intensiteSlider.setShowTickLabels(true);
        intensiteSlider.setShowTickMarks(true);
        intensiteSlider.setSnapToTicks(true);
        intensiteLabel.setText("5");

        intensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int val = newVal.intValue();
            intensiteLabel.setText(String.valueOf(val));

            // Swap de classe CSS selon l'intensité
            intensiteLabel.getStyleClass().removeAll(INTENSITE_CLASSES);
            if (val <= 3)      intensiteLabel.getStyleClass().add("intensite-low");
            else if (val <= 6) intensiteLabel.getStyleClass().add("intensite-medium");
            else               intensiteLabel.getStyleClass().add("intensite-high");
        });
    }

    private void initDescriptionCounter() {
        updateDescCharCount(0);
        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            int length = newVal == null ? 0 : newVal.length();
            updateDescCharCount(length);
            if (length > MAX_DESCRIPTION) {
                descriptionArea.setText(newVal.substring(0, MAX_DESCRIPTION));
                descriptionArea.positionCaret(MAX_DESCRIPTION);
            }
        });
    }

    private void updateDescCharCount(int length) {
        if (descCharCountLabel == null) return;
        descCharCountLabel.setText(length + " / " + MAX_DESCRIPTION + " caractères");
        descCharCountLabel.getStyleClass().removeAll("char-count", "char-count-warning");
        descCharCountLabel.getStyleClass().add(
                length > MAX_DESCRIPTION * 0.9 ? "char-count-warning" : "char-count"
        );
    }

    private void chargerSommeils() throws SQLException {
        List<Sommeil> sommeils = sommeilService.listerTous();
        sommeilCombo.setItems(FXCollections.observableArrayList(sommeils));

        sommeilCombo.setConverter(new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(Sommeil s) {
                if (s == null) return "";
                return "🌙 " + s.getDateNuit().format(fmt) + "  —  " + s.getQualite();
            }

            @Override
            public Sommeil fromString(String string) { return null; }
        });

        if (!sommeils.isEmpty()) sommeilCombo.setValue(sommeils.get(0));
    }

    // ─── Validation visuelle (via CSS classes uniquement) ───────────────────────

    private void setupValidation() {
        sommeilCombo.valueProperty().addListener((obs, old, newVal) ->
                setFieldState(sommeilCombo, newVal != null ? "field-valid-blue" : "field-error")
        );
        titreField.textProperty().addListener((obs, old, newVal) ->
                validateTitreVisual(newVal));
        descriptionArea.textProperty().addListener((obs, old, newVal) ->
                validateDescriptionVisual(newVal));
        typeCombo.valueProperty().addListener((obs, old, newVal) ->
                setFieldState(typeCombo, newVal != null ? "field-valid-purple" : "field-error")
        );
        humeurCombo.valueProperty().addListener((obs, old, newVal) ->
                setFieldState(humeurCombo, newVal != null ? "field-valid-green" : "field-error")
        );
        emotionsField.textProperty().addListener((obs, old, newVal) ->
                validateOptionalField(emotionsField, newVal));
        symbolesField.textProperty().addListener((obs, old, newVal) ->
                validateOptionalField(symbolesField, newVal));
    }

    /** Retire toutes les classes de validation et applique la nouvelle. */
    private void setFieldState(Control field, String cssClass) {
        field.getStyleClass().removeAll(VALIDATION_CLASSES);
        field.getStyleClass().add(cssClass);
    }

    private void validateTitreVisual(String text) {
        if (text == null || text.trim().isEmpty())
            setFieldState(titreField, "field-error");
        else if (compterLettres(text.trim()) < 3 || text.trim().length() > MAX_TITRE)
            setFieldState(titreField, "field-warn");
        else
            setFieldState(titreField, "field-valid-purple");
    }

    private void validateDescriptionVisual(String text) {
        if (text == null || text.trim().isEmpty())
            setFieldState(descriptionArea, "field-error");
        else if (compterLettres(text.trim()) < 5 || text.trim().length() > MAX_DESCRIPTION)
            setFieldState(descriptionArea, "field-warn");
        else
            setFieldState(descriptionArea, "field-valid-purple");
    }

    private void validateOptionalField(TextField field, String text) {
        if (text == null || text.trim().isEmpty())
            setFieldState(field, "field-default");
        else if (compterLettres(text.trim()) < 4 || text.length() > MAX_OPTIONAL)
            setFieldState(field, "field-warn");
        else
            setFieldState(field, "field-valid-orange");
    }

    // ─── Effets visuels ─────────────────────────────────────────────────────────

    private void setupButtonEffects() {
        // Couleurs hover/pressed → CSS  |  Scale → Java (CSS ne peut pas animer)
        addScaleEffect(btnSave);
        addScaleEffect(btnCancel);
    }

    private void addScaleEffect(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void applyEntranceAnimation() {
        if (mainContainer == null) return;
        mainContainer.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), mainContainer);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ─── API publique ────────────────────────────────────────────────────────────

    public void setReveService(ReveService service) {
        this.reveService = service;
    }

    public void setSommeilService(SommeilService service) {
        this.sommeilService = service;
        try {
            chargerSommeils();
            setupValidation();
        } catch (SQLException e) {
            showStyledError("Erreur de chargement",
                    "Impossible de charger les nuits.\n" + e.getMessage());
        }
    }

    public void setParentController(ReveController parent) {
        this.parentController = parent;
    }

    public void setReve(Reve reve) {
        this.reve = reve;

        if (titleLabel != null)    titleLabel.setText("Modifier le Rêve");
        if (subtitleLabel != null) subtitleLabel.setText("Mettez à jour les détails de votre rêve");
        if (headerIcon != null)    headerIcon.setText("✏️");
        if (btnSave != null)       btnSave.setText("✓ Modifier");

        titreField.setText(reve.getTitre() != null ? reve.getTitre() : "");
        descriptionArea.setText(reve.getDescription() != null ? reve.getDescription() : "");
        typeCombo.setValue(reve.getTypeReve());
        humeurCombo.setValue(reve.getHumeur());
        intensiteSlider.setValue(reve.getIntensite());
        couleurCheck.setSelected(reve.isCouleur());
        recurrentCheck.setSelected(reve.isRecurrent());
        emotionsField.setText(reve.getEmotions() != null ? reve.getEmotions() : "");
        symbolesField.setText(reve.getSymboles() != null ? reve.getSymboles() : "");

        try {
            Sommeil sommeil = sommeilService.trouverParId(reve.getSommeilId());
            if (sommeil != null) sommeilCombo.setValue(sommeil);
        } catch (SQLException e) {
            showStyledError("Erreur", "Impossible de charger le sommeil associé.\n" + e.getMessage());
        }
    }

    // ─── Actions FXML ────────────────────────────────────────────────────────────

    @FXML
    private void sauvegarder() {
        if (!validerFormulaire()) return;

        try {
            if (reve == null) reve = new Reve();

            Sommeil s = sommeilCombo.getValue();
            reve.setSommeilId(s.getId());
            reve.setTitre(titreField.getText().trim());
            reve.setDescription(descriptionArea.getText().trim());
            reve.setTypeReve(typeCombo.getValue());
            reve.setHumeur(humeurCombo.getValue());
            reve.setIntensite((int) intensiteSlider.getValue());
            reve.setCouleur(couleurCheck.isSelected());
            reve.setRecurrent(recurrentCheck.isSelected());

            String emotions = emotionsField.getText().trim();
            reve.setEmotions(emotions.isEmpty() ? null : emotions);

            String symboles = symbolesField.getText().trim();
            reve.setSymboles(symboles.isEmpty() ? null : symboles);

            if (reve.getId() == 0) {
                reveService.creer(reve);
                showStyledSuccess("💭 Rêve enregistré !", "Votre rêve a été sauvegardé.");
            } else {
                reveService.modifier(reve);
                showStyledSuccess("✏️ Rêve modifié !", "Les détails ont été mis à jour.");
            }

            if (parentController != null) parentController.loadAllReves();
            fermer();

        } catch (SQLException e) {
            showStyledError("Erreur BD", "Impossible de sauvegarder.\n" + e.getMessage());
        } catch (Exception e) {
            showStyledError("Erreur inattendue", e.getMessage());
        }
    }

    @FXML
    private void annuler() { fermer(); }

    // ─── Validation métier ───────────────────────────────────────────────────────

    private boolean validerFormulaire() {
        StringBuilder errors = new StringBuilder();

        if (sommeilCombo.getValue() == null)
            errors.append("🛌  Sélectionnez une nuit de sommeil\n");

        String titre = titreField.getText();
        if (titre == null || titre.trim().isEmpty())
            errors.append("✏️  Le titre est obligatoire\n");
        else {
            if (compterLettres(titre.trim()) < 3)
                errors.append("✏️  Le titre doit contenir au moins 3 lettres\n");
            if (titre.trim().length() > MAX_TITRE)
                errors.append("✏️  Le titre ne doit pas dépasser " + MAX_TITRE + " caractères\n");
        }

        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty())
            errors.append("📖  La description est obligatoire\n");
        else {
            if (compterLettres(description.trim()) < 5)
                errors.append("📖  La description doit contenir au moins 5 lettres\n");
            if (description.trim().length() > MAX_DESCRIPTION)
                errors.append("📖  La description ne doit pas dépasser " + MAX_DESCRIPTION + " caractères\n");
        }

        if (typeCombo.getValue() == null)  errors.append("🎭  Le type de rêve est obligatoire\n");
        if (humeurCombo.getValue() == null) errors.append("😊  L'humeur ressentie est obligatoire\n");

        int intensite = (int) intensiteSlider.getValue();
        if (intensite < 1 || intensite > 10)
            errors.append("⚡  L'intensité doit être entre 1 et 10\n");

        String emotions = emotionsField.getText();
        if (emotions != null && !emotions.trim().isEmpty()) {
            if (compterLettres(emotions.trim()) < 4)
                errors.append("💫  Les émotions doivent contenir au moins 4 lettres\n");
            if (emotions.length() > MAX_OPTIONAL)
                errors.append("💫  Émotions : max " + MAX_OPTIONAL + " caractères\n");
        }

        String symboles = symbolesField.getText();
        if (symboles != null && !symboles.trim().isEmpty()) {
            if (compterLettres(symboles.trim()) < 4)
                errors.append("🔮  Les symboles doivent contenir au moins 4 lettres\n");
            if (symboles.length() > MAX_OPTIONAL)
                errors.append("🔮  Symboles : max " + MAX_OPTIONAL + " caractères\n");
        }

        if (errors.length() > 0) {
            showStyledError("Formulaire invalide", "Veuillez corriger :\n\n" + errors);
            return false;
        }
        return true;
    }

    // ─── Utilitaires ────────────────────────────────────────────────────────────

    private int compterLettres(String texte) {
        if (texte == null) return 0;
        int count = 0;
        for (char c : texte.toCharArray()) if (Character.isLetter(c)) count++;
        return count;
    }

    private void fermer() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        FadeTransition ft = new FadeTransition(Duration.millis(200), btnCancel.getScene().getRoot());
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> stage.close());
        ft.play();
    }

    private void showStyledError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌  " + title);
        alert.setContentText(message);
        applyAlertStyle(alert, "alert-error");
        alert.showAndWait();
    }

    private void showStyledSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(title);
        alert.setContentText(message);
        applyAlertStyle(alert, "alert-success");
        alert.showAndWait();
    }

    /** Injecte le CSS dans la DialogPane de l'alerte (fenêtre séparée). */
    private void applyAlertStyle(Alert alert, String cssClass) {
        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/css/reve-form.css").toExternalForm()
        );
        dp.getStyleClass().add(cssClass);
    }
}
