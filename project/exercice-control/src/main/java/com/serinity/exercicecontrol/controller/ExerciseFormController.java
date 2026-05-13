package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.ExerciseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExerciseFormController {

    @FXML private Label lblHeader;

    @FXML private TextField txtTitle;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<Integer> cbLevel;

    @FXML private TextField txtDuration;

    @FXML private TextArea txtDescription;
    @FXML private TextArea txtBenefits;
    @FXML private TextArea txtTips;
    @FXML private TextField txtTheme;
    @FXML private TextArea txtGuidedInstructions;
    @FXML private CheckBox chkIsActive;

    @FXML private Button btnSave;

    private final ExerciseService exerciseService = new ExerciseService();

    private Exercise editing;
    private Runnable onDoneRefresh;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "respiration", "méditation", "relaxation", "yoga", "plein_conscience"
        ));

        cbLevel.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cbLevel.setValue(1);
    }

    public void setModeCreateReturnToList(Runnable onDoneRefresh) {
        this.editing = null;
        this.onDoneRefresh = onDoneRefresh;

        lblHeader.setText("Ajouter un exercice");
        btnSave.setText("Ajouter");
        clearFields();
    }

    public void setModeEditReturnToList(Exercise ex, Runnable onDoneRefresh) {
        this.editing = ex;
        this.onDoneRefresh = onDoneRefresh;

        lblHeader.setText("Modifier l'exercice");
        btnSave.setText("Enregistrer");

        if (ex != null) {
            txtTitle.setText(nullToEmpty(ex.getTitle()));
            cbType.setValue(ex.getType());
            cbLevel.setValue(ex.getLevel());
            txtDuration.setText(String.valueOf(ex.getDurationMinutes()));
            txtDescription.setText(nullToEmpty(ex.getDescription()));
            txtBenefits.setText(nullToEmpty(ex.getBenefits()));
            txtTips.setText(nullToEmpty(ex.getTips()));
            txtTheme.setText(nullToEmpty(ex.getTheme()));
            txtGuidedInstructions.setText(guidedInstructionsToTextarea(ex.getGuidedInstructions()));
            chkIsActive.setSelected(ex.isActive());
        }
    }

    @FXML
    private void onSave() {
        try {
            Exercise ex = new Exercise();

            if (editing != null) {
                ex.setId(editing.getId());
            }

            ex.setTitle(txtTitle.getText());
            ex.setType(cbType.getValue());
            ex.setLevel(cbLevel.getValue() == null ? 1 : cbLevel.getValue());

            int dur = Integer.parseInt(txtDuration.getText().trim());
            ex.setDurationMinutes(dur);

            ex.setDescription(txtDescription.getText());
            ex.setBenefits(txtBenefits.getText());
            ex.setTips(txtTips.getText());
            ex.setTheme(txtTheme.getText());
            ex.setGuidedInstructions(serializeGuidedInstructions(txtGuidedInstructions.getText()));
            ex.setIsActive(chkIsActive.isSelected());

            if (editing == null) {
                exerciseService.addExercise(ex);
                showInfo("Ajout", "Exercice ajouté avec succès.");
            } else {
                exerciseService.updateExercise(ex);
                showInfo("Mise à jour", "Exercice modifié avec succès.");
            }

            if (onDoneRefresh != null) {
                onDoneRefresh.run();
            }
            goBackToList();

        } catch (NumberFormatException e) {
            showError("Erreur", "Durée invalide. Mets un nombre (minutes).");
        } catch (IllegalArgumentException e) {
            showError("Erreur validation", e.getMessage());
        } catch (SQLException e) {
            showError("Erreur BD", e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        goBackToList();
    }

    @FXML
    private void onCancel() {
        goBackToList();
    }

    private void goBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseList.fxml"));
            Parent root = loader.load();
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à la liste.");
        }
    }

    private void setContent(Parent page) {
        StackPane host = (StackPane) btnSave.getScene().lookup("#contentHost");
        if (host == null) {
            host = (StackPane) btnSave.getScene().lookup("#contentHostStackPane");
        }
        if (host == null) {
            throw new IllegalStateException("contentHost/contentHostStackPane introuvable. Vérifie le shell FXML.");
        }
        host.getChildren().setAll(page);
    }

    private void clearFields() {
        txtTitle.clear();
        cbType.getSelectionModel().clearSelection();
        cbLevel.setValue(1);
        txtDuration.clear();
        txtDescription.clear();
        txtBenefits.clear();
        txtTips.clear();
        txtTheme.clear();
        txtGuidedInstructions.clear();
        chkIsActive.setSelected(true);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String serializeGuidedInstructions(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }

        String[] lines = rawText.split("\\R");
        StringBuilder steps = new StringBuilder("[");
        int stepIndex = 1;
        boolean hasSteps = false;

        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (hasSteps) {
                steps.append(',');
            }

            steps.append("{\"title\":\"")
                    .append(escapeJson("Étape " + stepIndex++))
                    .append("\",\"description\":\"")
                    .append(escapeJson(trimmed))
                    .append("\"}");
            hasSteps = true;
        }

        if (!hasSteps) {
            return null;
        }

        steps.append(']');
        return steps.toString();
    }

    private String guidedInstructionsToTextarea(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return "";
        }

        List<String> lines = extractDescriptions(storedValue);
        if (lines.isEmpty()) {
            return "";
        }

        return String.join(System.lineSeparator(), lines);
    }

    private List<String> extractDescriptions(String json) {
        List<String> lines = new ArrayList<>();
        int searchFrom = 0;

        while (searchFrom < json.length()) {
            int keyIndex = json.indexOf("\"description\"", searchFrom);
            if (keyIndex < 0) {
                break;
            }

            int colonIndex = json.indexOf(':', keyIndex + "\"description\"".length());
            if (colonIndex < 0) {
                break;
            }

            int valueStart = skipWhitespace(json, colonIndex + 1);
            if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
                searchFrom = colonIndex + 1;
                continue;
            }

            ParsedJsonString parsed = readJsonString(json, valueStart);
            if (parsed == null) {
                return new ArrayList<>();
            }

            String description = parsed.value().trim();
            if (!description.isEmpty()) {
                lines.add(description);
            }
            searchFrom = parsed.nextIndex();
        }

        return lines;
    }

    private int skipWhitespace(String text, int index) {
        int cursor = index;
        while (cursor < text.length() && Character.isWhitespace(text.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private ParsedJsonString readJsonString(String text, int openingQuoteIndex) {
        if (openingQuoteIndex >= text.length() || text.charAt(openingQuoteIndex) != '"') {
            return null;
        }

        StringBuilder value = new StringBuilder();
        int cursor = openingQuoteIndex + 1;

        while (cursor < text.length()) {
            char current = text.charAt(cursor);
            if (current == '"') {
                return new ParsedJsonString(value.toString(), cursor + 1);
            }

            if (current == '\\') {
                if (cursor + 1 >= text.length()) {
                    return null;
                }

                char escaped = text.charAt(cursor + 1);
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        value.append(escaped);
                        cursor += 2;
                        break;
                    case 'b':
                        value.append('\b');
                        cursor += 2;
                        break;
                    case 'f':
                        value.append('\f');
                        cursor += 2;
                        break;
                    case 'n':
                        value.append('\n');
                        cursor += 2;
                        break;
                    case 'r':
                        value.append('\r');
                        cursor += 2;
                        break;
                    case 't':
                        value.append('\t');
                        cursor += 2;
                        break;
                    case 'u':
                        if (cursor + 5 >= text.length()) {
                            return null;
                        }
                        String hex = text.substring(cursor + 2, cursor + 6);
                        try {
                            value.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            return null;
                        }
                        cursor += 6;
                        break;
                    default:
                        return null;
                }
                continue;
            }

            value.append(current);
            cursor++;
        }

        return null;
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (current < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) current));
                    } else {
                        escaped.append(current);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private record ParsedJsonString(String value, int nextIndex) {
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
