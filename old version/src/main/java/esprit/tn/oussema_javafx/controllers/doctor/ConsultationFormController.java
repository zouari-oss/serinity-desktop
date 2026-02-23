package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.models.RapportMedical;
import esprit.tn.oussema_javafx.services.ConsultationService;
import esprit.tn.oussema_javafx.services.RapportMedicalService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
//interface y3amer el diagnosis and perscription wkol
public class ConsultationFormController {

    @FXML private TextArea diagnosticField;
    @FXML private TextArea prescriptionField;
    @FXML private TextArea notesField;

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");
    }
    private final ConsultationService consultationService = new ConsultationService();
    private final RapportMedicalService rapportService = new RapportMedicalService();

    private Consultation editing = null;

    private int patientId = -1;
    private int rdvId = -1;

    // FLAG: UI ready
    private boolean uiReady = false;

    @FXML
    public void initialize(){
        uiReady = true;
        populateIfNeeded();
    }

    // called from RDV card
    public void setRdvData(int patientId, int rdvId){
        this.patientId = patientId;
        this.rdvId = rdvId;
        populateIfNeeded();
    }

    // called when editing consultation
    public void setConsultation(Consultation c, int patientId, int rdvId){
        this.editing = c;
        this.patientId = patientId;
        this.rdvId = rdvId;
        populateIfNeeded();
    }

    private void populateIfNeeded(){

        if(!uiReady) return;
        if(editing == null) return;

        Platform.runLater(() -> {
            diagnosticField.setText(editing.getDiagnostic());
            prescriptionField.setText(editing.getPrescription());
            notesField.setText(editing.getNotes());
        });
    }

    @FXML
    private void save(){

        if(patientId == -1 || rdvId == -1){
            new Alert(Alert.AlertType.ERROR,
                    "Erreur: consultation non liée à un rendez-vous").show();
            return;
        }

        RapportMedical rapport = findOrCreateRapport(patientId);

        // ADD
        if(editing == null){

            Consultation c = new Consultation();
            c.setDoctorId(2);
            c.setRapportId(rapport.getId());
            c.setRendezVousId(rdvId);
            c.setDateConsultation(LocalDateTime.now());
            c.setDiagnostic(diagnosticField.getText());
            c.setPrescription(prescriptionField.getText());
            c.setNotes(notesField.getText());

            consultationService.insert(c);
        }
        // EDIT
        else{
            editing.setDiagnostic(diagnosticField.getText());
            editing.setPrescription(prescriptionField.getText());
            editing.setNotes(notesField.getText());

            consultationService.update(editing);
        }

        Router.go(
                "/fxml/doctor/patient_report.fxml",
                "Dossier Patient",
                (PatientReportController controller) ->
                        controller.setPatientId(patientId)
        );
    }

    private RapportMedical findOrCreateRapport(int patientId){

        List<RapportMedical> list = rapportService.findAll();

        for(RapportMedical r : list){
            if(r.getPatientId()==patientId)
                return r;
        }

        RapportMedical r = new RapportMedical();
        r.setPatientId(patientId);
        r.setDateCreation(LocalDate.now());
        r.setResumeGeneral("Dossier médical du patient");

        rapportService.insert(r);

        return rapportService.findAll()
                .stream()
                .filter(x->x.getPatientId()==patientId)
                .findFirst()
                .orElse(null);
    }
}
