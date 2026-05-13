package com.serinity.consultationcontrol.controller.doctor;

import com.serinity.consultationcontrol.model.Consultation;
import com.serinity.consultationcontrol.service.ConsultationService;
import com.serinity.consultationcontrol.util.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultationListController {

    @FXML private FlowPane cardsPane;
    @FXML private VBox emptyBox;

    private final ConsultationService service = new ConsultationService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int rapportId = -1;
    private String patientId;

    public void setData(int rapportId, String patientId){
        this.rapportId = rapportId;
        this.patientId = patientId;
        load();
    }

    @FXML
    private void back(ActionEvent event){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml", "Mes RDV");
    }

    @FXML
    private void addConsultation(ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Action impossible");
        alert.setContentText("Une consultation doit etre creee depuis un rendez-vous approuve.");
        alert.showAndWait();
    }

    private void load(){
        cardsPane.getChildren().clear();
        List<Consultation> consultations = service.findAll()
                .stream()
                .filter(c -> c.getRapportId() == rapportId)
                .toList();

        if(consultations.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);
        for(Consultation consultation : consultations){
            cardsPane.getChildren().add(card(consultation));
        }
    }

    private VBox card(Consultation consultation){
        VBox card = new VBox(10);
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(320);

        Label date = new Label(consultation.getDateConsultation().format(fmt));
        date.getStyleClass().add("rdv-date");
        Label diag = new Label("Diagnostic : " + safe(consultation.getDiagnostic()));
        Label pres = new Label("Prescription : " + safe(consultation.getPrescription()));
        Label notes = new Label("Notes : " + safe(consultation.getNotes()));
        diag.getStyleClass().add("rdv-motif");
        pres.getStyleClass().add("rdv-motif");
        notes.getStyleClass().add("rdv-motif");

        diag.setWrapText(true);
        pres.setWrapText(true);
        notes.setWrapText(true);

        HBox actions = new HBox(10);
        Button edit = new Button("Modifier");
        Button delete = new Button("Supprimer");
        edit.getStyleClass().add("btn-consult");
        delete.getStyleClass().add("btn-refuse");

        edit.setOnAction(e -> Router.go(
                "/fxml/doctor/consultation_form.fxml",
                "Modifier consultation",
                (ConsultationFormController controller) ->
                        controller.setConsultation(consultation, patientId, consultation.getRendezVousId())
        ));

        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer consultation ?");
            confirm.setContentText("Action irreversible");
            confirm.showAndWait().ifPresent(result -> {
                if(result == ButtonType.OK){
                    service.delete(consultation.getId());
                    load();
                }
            });
        });

        actions.getChildren().addAll(edit, delete);
        card.getChildren().addAll(date, diag, pres, notes, actions);
        return card;
    }

    private String safe(String value){
        return value == null || value.isBlank() ? "-" : value;
    }
}
