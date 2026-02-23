package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.dao.SommeilDao;
import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.ReveService;
import com.serinity.sleepcontrol.service.SommeilService;
import com.serinity.sleepcontrol.utils.MyDataBase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReveFormController {

    @FXML private ComboBox<Sommeil> sommeilCombo;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
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

    private ReveService reveService;
    private SommeilService sommeilService;
    private Reve reve;
    private ReveController parentController;

    @FXML
    public void initialize() {
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            sommeilService = new SommeilService((SommeilDao) connection);

            typeCombo.setItems(FXCollections.observableArrayList(
                    "Normal", "Cauchemar", "Lucide", "Récurrent"
            ));
            typeCombo.setPromptText("Sélectionner...");

            humeurCombo.setItems(FXCollections.observableArrayList(
                    "Joyeux", "Triste", "Anxieux", "Neutre", "Excité", "Effrayé", "Paisible"
            ));
            humeurCombo.setPromptText("Sélectionner...");

            chargerSommeils();

            intensiteSlider.setMin(1);
            intensiteSlider.setMax(10);
            intensiteSlider.setValue(5);
            intensiteSlider.setMajorTickUnit(1);
            intensiteSlider.setMinorTickCount(0);
            intensiteSlider.setShowTickLabels(true);
            intensiteSlider.setShowTickMarks(true);
            intensiteSlider.setSnapToTicks(true);

            intensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                intensiteLabel.setText(String.valueOf(newVal.intValue()));
            });

            intensiteLabel.setText("5");

            couleurCheck.setSelected(true);

            setupValidation();

        } catch (Exception e) {
            showError("Erreur", "Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupValidation() {
        sommeilCombo.valueProperty().addListener((obs, old, newVal) -> validateSommeil());
        titreField.textProperty().addListener((obs, old, newVal) -> validateTitre());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateDescription());
        typeCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(typeCombo));
        humeurCombo.valueProperty().addListener((obs, old, newVal) -> validateComboBox(humeurCombo));
        emotionsField.textProperty().addListener((obs, old, newVal) -> validateEmotions());
        symbolesField.textProperty().addListener((obs, old, newVal) -> validateSymboles());

        validateSommeil();
    }

    private int compterLettres(String texte) {
        if (texte == null) return 0;

        int count = 0;
        for (char c : texte.toCharArray()) {
            if (Character.isLetter(c)) {
                count++;
            }
        }
        return count;
    }

    private void validateSommeil() {
        if (sommeilCombo.getValue() == null) {
            sommeilCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            sommeilCombo.setStyle("");
        }
    }

    private void validateTitre() {
        String text = titreField.getText();

        if (text == null || text.trim().isEmpty()) {
            titreField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            int nbLettres = compterLettres(text.trim());

            if (nbLettres < 3) {
                titreField.setStyle("-fx-border-color: orange; -fx-border-width: 2;");
            } else if (text.trim().length() > 100) {
                titreField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                titreField.setStyle("");
            }
        }
    }

    private void validateDescription() {
        String text = descriptionArea.getText();

        if (text == null || text.trim().isEmpty()) {
            descriptionArea.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            int nbLettres = compterLettres(text.trim());

            if (nbLettres < 5) {
                descriptionArea.setStyle("-fx-border-color: orange; -fx-border-width: 2;");
            } else if (text.trim().length() > 2000) {
                descriptionArea.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                descriptionArea.setStyle("");
            }
        }
    }

    private void validateComboBox(ComboBox<String> combo) {
        if (combo.getValue() == null) {
            combo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            combo.setStyle("");
        }
    }

    private void validateEmotions() {
        String text = emotionsField.getText();

        if (text == null || text.trim().isEmpty()) {
            emotionsField.setStyle("");
        } else {
            int nbLettres = compterLettres(text.trim());

            if (nbLettres < 4) {
                emotionsField.setStyle("-fx-border-color: orange; -fx-border-width: 2;");
            } else if (text.length() > 200) {
                emotionsField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                emotionsField.setStyle("");
            }
        }
    }

    private void validateSymboles() {
        String text = symbolesField.getText();

        if (text == null || text.trim().isEmpty()) {
            symbolesField.setStyle("");
        } else {
            int nbLettres = compterLettres(text.trim());

            if (nbLettres < 4) {
                symbolesField.setStyle("-fx-border-color: orange; -fx-border-width: 2;");
            } else if (text.length() > 200) {
                symbolesField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                symbolesField.setStyle("");
            }
        }
    }

    private void chargerSommeils() throws SQLException {
        List<Sommeil> sommeils = sommeilService.listerTous();

        sommeilCombo.setItems(FXCollections.observableArrayList(sommeils));

        sommeilCombo.setConverter(new javafx.util.StringConverter<Sommeil>() {
            @Override
            public String toString(Sommeil sommeil) {
                if (sommeil == null) return "";
                return String.format("%s - %s",
                        sommeil.getDateNuit().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        sommeil.getQualite()
                );
            }

            @Override
            public Sommeil fromString(String string) {
                return null;
            }
        });

        if (!sommeils.isEmpty()) {
            sommeilCombo.setValue(sommeils.get(0));
        }
    }

    public void setReveService(ReveService service) {
        this.reveService = service;
    }

    public void setParentController(ReveController parent) {
        this.parentController = parent;
    }

    public void setReve(Reve reve) {
        this.reve = reve;

        titreField.setText(reve.getTitre());
        descriptionArea.setText(reve.getDescription());
        typeCombo.setValue(reve.getTypeReve());
        humeurCombo.setValue(reve.getHumeur());
        intensiteSlider.setValue(reve.getIntensite());
        couleurCheck.setSelected(reve.isCouleur());
        recurrentCheck.setSelected(reve.isRecurrent());
        emotionsField.setText(reve.getEmotions());
        symbolesField.setText(reve.getSymboles());

        try {
            Sommeil sommeil = sommeilService.trouverParId(reve.getSommeilId());
            if (sommeil != null) {
                sommeilCombo.setValue(sommeil);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sauvegarder() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            if (reve == null) {
                reve = new Reve();
            }

            Sommeil sommeilSelectionne = sommeilCombo.getValue();
            reve.setSommeilId(sommeilSelectionne.getId());
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
                showSuccess("Rêve ajouté avec succès!");
            } else {
                reveService.modifier(reve);
                showSuccess("Rêve modifié avec succès!");
            }

            if (parentController != null) {
                parentController.loadAllReves();
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

    private boolean validerFormulaire() {
        StringBuilder errors = new StringBuilder();

        if (sommeilCombo.getValue() == null) {
            errors.append("- Vous devez sélectionner un sommeil\n");
        }

        String titre = titreField.getText();
        if (titre == null || titre.trim().isEmpty()) {
            errors.append("- Le titre est obligatoire\n");
        } else {
            int nbLettres = compterLettres(titre.trim());
            if (nbLettres < 3) {
                errors.append("- Le titre doit contenir au moins 3 lettres\n");
            } else if (titre.trim().length() > 100) {
                errors.append("- Le titre ne doit pas dépasser 100 caractères\n");
            }
        }

        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            errors.append("- La description est obligatoire\n");
        } else {
            int nbLettres = compterLettres(description.trim());
            if (nbLettres < 5) {
                errors.append("- La description doit contenir au moins 5 lettres\n");
            } else if (description.trim().length() > 2000) {
                errors.append("- La description ne doit pas dépasser 2000 caractères\n");
            }
        }

        if (typeCombo.getValue() == null) {
            errors.append("- Le type de rêve est obligatoire\n");
        }

        if (humeurCombo.getValue() == null) {
            errors.append("- L'humeur ressentie est obligatoire\n");
        }

        int intensite = (int) intensiteSlider.getValue();
        if (intensite < 1 || intensite > 10) {
            errors.append("- L'intensité doit être entre 1 et 10\n");
        }

        String emotions = emotionsField.getText();
        if (emotions != null && !emotions.trim().isEmpty()) {
            int nbLettres = compterLettres(emotions.trim());
            if (nbLettres < 4) {
                errors.append("- Les émotions doivent contenir au moins 4 lettres\n");
            } else if (emotions.length() > 200) {
                errors.append("- Les émotions ne doivent pas dépasser 200 caractères\n");
            }
        }

        String symboles = symbolesField.getText();
        if (symboles != null && !symboles.trim().isEmpty()) {
            int nbLettres = compterLettres(symboles.trim());
            if (nbLettres < 4) {
                errors.append("- Les symboles doivent contenir au moins 4 lettres\n");
            } else if (symboles.length() > 200) {
                errors.append("- Les symboles ne doivent pas dépasser 200 caractères\n");
            }
        }

        if (errors.length() > 0) {
            showError("Formulaire invalide", "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        fermer();
    }

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
