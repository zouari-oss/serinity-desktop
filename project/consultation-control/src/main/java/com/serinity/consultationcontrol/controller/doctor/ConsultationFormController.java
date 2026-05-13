package com.serinity.consultationcontrol.controller.doctor;

import com.serinity.consultationcontrol.model.Consultation;
import com.serinity.consultationcontrol.model.RapportMedical;
import com.serinity.consultationcontrol.service.ConsultationService;
import com.serinity.consultationcontrol.service.RapportMedicalService;
import com.serinity.consultationcontrol.util.AppSession;
import com.serinity.consultationcontrol.util.Router;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultationFormController {

    @FXML private TextArea diagnosticField;
    @FXML private TextArea prescriptionField;
    @FXML private TextArea notesField;

    private final ConsultationService consultationService = new ConsultationService();
    private final RapportMedicalService rapportService = new RapportMedicalService();

    private Consultation editing;
    private String patientId;
    private int rdvId = -1;
    private boolean uiReady = false;

    @FXML
    public void initialize(){
        uiReady = true;
        populateIfNeeded();
    }

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml", "Mes RDV");
    }

    public void setRdvData(String patientId, int rdvId){
        this.patientId = patientId;
        this.rdvId = rdvId;
        populateIfNeeded();
    }

    public void setConsultation(Consultation consultation, String patientId, int rdvId){
        this.editing = consultation;
        this.patientId = patientId;
        this.rdvId = rdvId;
        populateIfNeeded();
    }

    private void populateIfNeeded(){
        if(!uiReady || editing == null){
            return;
        }

        Platform.runLater(() -> {
            diagnosticField.setText(editing.getDiagnostic());
            prescriptionField.setText(editing.getPrescription());
            notesField.setText(editing.getNotes());
        });
    }

    @FXML
    private void save(){
        if(patientId == null || patientId.isBlank() || rdvId == -1){
            new Alert(Alert.AlertType.ERROR, "Erreur: consultation non liee a un rendez-vous").show();
            return;
        }

        String doctorId = AppSession.getCurrentUserId();
        if(doctorId == null || doctorId.isBlank()){
            new Alert(Alert.AlertType.ERROR, "Aucun medecin connecte.").show();
            return;
        }

        RapportMedical rapport = findOrCreateRapport(patientId);
        if(rapport == null){
            new Alert(Alert.AlertType.ERROR, "Impossible de charger le dossier patient.").show();
            return;
        }

        if(editing == null){
            Consultation consultation = new Consultation();
            consultation.setDoctorId(doctorId);
            consultation.setRapportId(rapport.getId());
            consultation.setRendezVousId(rdvId);
            consultation.setDateConsultation(LocalDateTime.now());
            consultation.setDiagnostic(diagnosticField.getText());
            consultation.setPrescription(prescriptionField.getText());
            consultation.setNotes(notesField.getText());
            consultationService.insert(consultation);
        } else {
            editing.setDiagnostic(diagnosticField.getText());
            editing.setPrescription(prescriptionField.getText());
            editing.setNotes(notesField.getText());
            consultationService.update(editing);
        }

        Router.go(
                "/fxml/doctor/patient_report.fxml",
                "Dossier Patient",
                (PatientReportController controller) -> controller.setPatientId(patientId)
        );
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
        rapport.setResumeGeneral("Dossier medical du patient");
        rapportService.insert(rapport);

        return rapportService.findAll()
                .stream()
                .filter(item -> patientId.equals(item.getPatientId()))
                .findFirst()
                .orElse(null);
    }
}
