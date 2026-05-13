package com.serinity.consultationcontrol.controller.doctor;

import com.serinity.consultationcontrol.model.Consultation;
import com.serinity.consultationcontrol.model.RapportMedical;
import com.serinity.consultationcontrol.model.User;
import com.serinity.consultationcontrol.service.ConsultationService;
import com.serinity.consultationcontrol.service.MedicalReportPdfService;
import com.serinity.consultationcontrol.service.RapportMedicalService;
import com.serinity.consultationcontrol.service.UserLookupService;
import com.serinity.consultationcontrol.util.Router;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientReportController {

    @FXML private Label patientName;
    @FXML private Label patientPhone;
    @FXML private Label patientEmail;
    @FXML private FlowPane consultationsPane;
    @FXML private VBox emptyBox;

    private final MedicalReportPdfService pdfService = new MedicalReportPdfService();
    private final UserLookupService userService = new UserLookupService();
    private final RapportMedicalService rapportService = new RapportMedicalService();
    private final ConsultationService consultationService = new ConsultationService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String patientId;
    private RapportMedical currentRapport;

    public void setPatientId(String id){
        this.patientId = id;
        load();
    }

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml", "Mes RDV");
    }

    @FXML
    private void addConsultation(javafx.event.ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Action impossible");
        alert.setContentText("Une consultation doit etre creee depuis un rendez-vous approuve.");
        alert.showAndWait();
    }

    @FXML
    private void downloadPdf(){
        try{
            User patient = userService.findById(patientId);
            List<Consultation> consultations = consultationService.findAll()
                    .stream()
                    .filter(c -> currentRapport != null && c.getRapportId() == currentRapport.getId())
                    .toList();

            if(patient == null){
                new Alert(Alert.AlertType.ERROR, "Patient introuvable.").showAndWait();
                return;
            }

            if(consultations.isEmpty()){
                new Alert(Alert.AlertType.WARNING, "Aucune consultation a exporter.").showAndWait();
                return;
            }

            File pdf = pdfService.generate(patient, consultations);
            new Alert(Alert.AlertType.INFORMATION, "PDF genere sur le bureau.").showAndWait();
            Desktop.getDesktop().open(pdf);
        } catch(Exception e){
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur generation PDF").showAndWait();
        }
    }

    private void load(){
        User patient = userService.findById(patientId);
        if(patient == null){
            patientName.setText("Patient introuvable");
            patientPhone.setText("-");
            patientEmail.setText("-");
            return;
        }

        patientName.setText(patient.getFullName());
        patientPhone.setText(safe(patient.getPhone()));
        patientEmail.setText(safe(patient.getEmail()));

        currentRapport = findOrCreateRapport(patientId);
        List<Consultation> consultations = consultationService.findAll()
                .stream()
                .filter(c -> currentRapport != null && c.getRapportId() == currentRapport.getId())
                .toList();

        consultationsPane.getChildren().clear();
        if(consultations.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);
        for(Consultation consultation : consultations){
            consultationsPane.getChildren().add(card(consultation));
        }
    }

    private VBox card(Consultation consultation){
        VBox card = new VBox(12);
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(320);

        Label date = new Label(consultation.getDateConsultation().format(fmt));
        date.getStyleClass().add("rdv-date");

        Label diagTitle = new Label("Diagnostic");
        diagTitle.getStyleClass().add("section-title");
        Label diag = new Label(safe(consultation.getDiagnostic()));
        diag.setWrapText(true);
        diag.getStyleClass().add("rdv-motif");

        Label presTitle = new Label("Prescription");
        presTitle.getStyleClass().add("section-title");
        Label pres = new Label(safe(consultation.getPrescription()));
        pres.setWrapText(true);
        pres.getStyleClass().add("rdv-motif");

        Label notesTitle = new Label("Notes");
        notesTitle.getStyleClass().add("section-title");
        Label notes = new Label(safe(consultation.getNotes()));
        notes.setWrapText(true);
        notes.getStyleClass().add("rdv-motif");

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
            confirm.setHeaderText("Suppression consultation");
            confirm.setContentText("Voulez-vous supprimer cette consultation ?");
            confirm.showAndWait().ifPresent(result -> {
                if(result == ButtonType.OK){
                    consultationService.delete(consultation.getId());
                    load();
                }
            });
        });

        actions.getChildren().addAll(edit, delete);
        card.getChildren().addAll(date, diagTitle, diag, presTitle, pres, notesTitle, notes, actions);
        return card;
    }

    private RapportMedical findOrCreateRapport(String patientId){
        List<RapportMedical> rapports = rapportService.findAll();
        for(RapportMedical rapport : rapports){
            if(patientId.equals(rapport.getPatientId())){
                return rapport;
            }
        }

        RapportMedical rapport = new RapportMedical();
        rapport.setPatientId(patientId);
        rapport.setDateCreation(LocalDate.now());
        rapport.setResumeGeneral("Dossier medical");
        rapportService.insert(rapport);

        return rapportService.findAll()
                .stream()
                .filter(item -> patientId.equals(item.getPatientId()))
                .findFirst()
                .orElse(null);
    }

    private String safe(String value){
        return value == null || value.isBlank() ? "-" : value;
    }
}
