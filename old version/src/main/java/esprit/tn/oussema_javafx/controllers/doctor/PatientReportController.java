package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.models.RapportMedical;
import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.services.ConsultationService;
import esprit.tn.oussema_javafx.services.MedicalReportPdfService;
import esprit.tn.oussema_javafx.services.RapportMedicalService;
import esprit.tn.oussema_javafx.services.UserLookupService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
//interface doctor y3amer feha el consultation
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

    private int patientId;
    private RapportMedical currentRapport;

    public void setPatientId(int id){
        this.patientId = id;
        load();
    }

    private void load(){

        User patient = userService.findById(patientId);
        if(patient == null){
            patientName.setText("Patient introuvable");
            return;
        }

        patientName.setText("👤 " + patient.getFullName());
        patientPhone.setText("📞 " + patient.getPhone());
        patientEmail.setText("✉ " + patient.getEmail());

        currentRapport = findOrCreateRapport(patientId);

        List<Consultation> list = consultationService.findAll()
                .stream()
                .filter(c -> c.getRapportId() == currentRapport.getId())
                .toList();

        consultationsPane.getChildren().clear();

        if(list.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);

        for(Consultation c : list){
            consultationsPane.getChildren().add(card(c));
        }
    }
    @FXML
    private void downloadPdf(){

        try{

            User patient = userService.findById(patientId);

            List<Consultation> list = consultationService.findAll()
                    .stream()
                    .filter(c -> c.getRapportId() == currentRapport.getId())
                    .toList();

            if(list.isEmpty()){
                new Alert(Alert.AlertType.WARNING,
                        "Aucune consultation à exporter.").showAndWait();
                return;
            }

            File pdf = pdfService.generate(patient, list);

            new Alert(Alert.AlertType.INFORMATION,
                    "PDF généré sur le bureau ✔").showAndWait();

            Desktop.getDesktop().open(pdf);

        }catch(Exception e){
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur génération PDF").showAndWait();
        }
    }

    private VBox card(Consultation c){

        VBox card = new VBox(12);
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(320);

        // HEADER
        Label date = new Label("🗓 " + c.getDateConsultation().format(fmt));
        date.setStyle("-fx-font-weight:bold; -fx-text-fill:#0d6efd;");

        // diagnostic
        Label diagTitle = new Label("Diagnostic");
        diagTitle.setStyle("-fx-font-weight:bold;");

        Label diag = new Label(safe(c.getDiagnostic()));
        diag.setWrapText(true);

        // prescription
        Label presTitle = new Label("Prescription");
        presTitle.setStyle("-fx-font-weight:bold;");

        Label pres = new Label(safe(c.getPrescription()));
        pres.setWrapText(true);

        // notes
        Label notesTitle = new Label("Notes");
        notesTitle.setStyle("-fx-font-weight:bold;");

        Label notes = new Label(safe(c.getNotes()));
        notes.setWrapText(true);

        // ACTIONS
        HBox actions = new HBox(10);

        Button edit = new Button("✏ Modifier");
        Button delete = new Button("🗑 Supprimer");

        edit.getStyleClass().add("btn-consult");
        delete.getStyleClass().add("btn-refuse");

        // edit
        edit.setOnAction(e -> {
            Router.go(
                    "/fxml/doctor/consultation_form.fxml",
                    "Modifier consultation",
                    (ConsultationFormController controller) ->
                            controller.setConsultation(c, patientId, c.getRendezVousId())
            );
        });

        // delete
        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Suppression consultation");
            confirm.setContentText("Voulez-vous supprimer cette consultation ?");

            confirm.showAndWait().ifPresent(r -> {
                if(r == ButtonType.OK){
                    consultationService.delete(c.getId());
                    load();
                }
            });
        });

        actions.getChildren().addAll(edit, delete);

        card.getChildren().addAll(
                date,
                diagTitle, diag,
                presTitle, pres,
                notesTitle, notes,
                actions
        );

        return card;
    }

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");
    }

    private RapportMedical findOrCreateRapport(int patientId){

        List<RapportMedical> list = rapportService.findAll();

        for(RapportMedical r : list){
            if(r.getPatientId()==patientId)
                return r;
        }

        RapportMedical r = new RapportMedical();
        r.setPatientId(patientId);
        r.setDateCreation(java.time.LocalDate.now());
        r.setResumeGeneral("Dossier médical");

        rapportService.insert(r);

        return rapportService.findAll()
                .stream()
                .filter(x->x.getPatientId()==patientId)
                .findFirst()
                .orElse(null);
    }

    private String safe(String s){
        return s == null || s.isBlank() ? "-" : s;
    }

    @FXML
    private void addConsultation(javafx.event.ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Action impossible");
        alert.setContentText("Une consultation doit être créée depuis un rendez-vous approuvé.");
        alert.showAndWait();
    }

}
