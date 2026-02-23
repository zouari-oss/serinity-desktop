package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.RdvStatus;
import esprit.tn.oussema_javafx.models.RendezVous;
import esprit.tn.oussema_javafx.services.ConsultationService;
import esprit.tn.oussema_javafx.services.MedicalReportPdfService;
import esprit.tn.oussema_javafx.services.RendezVousService;
import esprit.tn.oussema_javafx.services.UserLookupService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorRdvController {

    @FXML private FlowPane cardsPane;
    @FXML private VBox emptyBox;

    private final RendezVousService service = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final MedicalReportPdfService pdfService = new MedicalReportPdfService();
    private final UserLookupService userLookupService = new UserLookupService();

    @FXML
    public void initialize(){
        refresh();
    }

    @FXML
    public void refresh(){

        cardsPane.getChildren().clear();

        int doctorId = 2; // later AppSession.getCurrentDoctorId()

        List<RendezVous> list = service.findAllByDoctor(doctorId);

        if(list.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);

        for(RendezVous r : list){
            cardsPane.getChildren().add(createCard(r));
        }
    }

    private VBox createCard(RendezVous r){

        VBox card = new VBox();
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(340);
        card.setSpacing(12);

        // ================= HEADER =================
        HBox header = new HBox();
        header.getStyleClass().add("doctor-card-header");

        Label patient = new Label(r.getPatientName());
        patient.getStyleClass().add("doctor-card-patient");

        Label status = new Label(r.getStatus().name());
        status.getStyleClass().add("doctor-card-status");

        switch (r.getStatus()){
            case EN_ATTENTE -> status.getStyleClass().add("status-wait");
            case APPROUVE -> status.getStyleClass().add("status-ok");
            case REFUSE -> status.getStyleClass().add("status-refused");
            case MODIFICATION_PROPOSEE -> status.getStyleClass().add("status-modified");
        }

        header.getChildren().addAll(patient, new Label("     "), status);

        // ================= BODY =================
        VBox body = new VBox(8);
        body.getStyleClass().add("doctor-card-body");

        Label date = new Label("📅 " + r.getDateTime().format(fmt));
        date.getStyleClass().add("doctor-card-info");

        Label motif = new Label("Motif : " + (r.getMotif()==null?"Non précisé":r.getMotif()));
        motif.setWrapText(true);
        motif.getStyleClass().add("doctor-card-motif");

        body.getChildren().addAll(date, motif);

        // ================= ACTIONS =================
        HBox actions = new HBox(10);
        actions.getStyleClass().add("doctor-card-actions");

        Button accept = new Button("Accepter");
        accept.getStyleClass().add("btn-accept");

        Button refuse = new Button("Refuser");
        refuse.getStyleClass().add("btn-refuse");

        Button addConsultation = new Button("🩺 Ajouter consultation");
        addConsultation.getStyleClass().add("btn-consult");

        Button openReport = new Button("📁 Dossier patient");
        openReport.getStyleClass().add("btn-consult");

        // ===== CASE 1 : EN ATTENTE =====
        if(r.getStatus() == RdvStatus.EN_ATTENTE){

            actions.getChildren().addAll(accept, refuse);

            accept.setOnAction(e->{
                r.setStatus(RdvStatus.APPROUVE);
                service.update(r);
                refresh();
            });

            refuse.setOnAction(e->{
                r.setStatus(RdvStatus.REFUSE);
                service.update(r);
                refresh();
            });
        }

        // ===== CASE 2 : APPROUVE =====
        else if(r.getStatus() == RdvStatus.APPROUVE){

            boolean alreadyConsulted = consultationService.existsByRdv(r.getId());

            // patient file always accessible
            actions.getChildren().add(openReport);

            openReport.setOnAction(e->{
                Router.go(
                        "/fxml/doctor/patient_report.fxml",
                        "Dossier Patient",
                        (PatientReportController controller) ->
                                controller.setPatientId(r.getPatientId())
                );
            });

            // allow only ONE consultation per RDV
            if(!alreadyConsulted){

                actions.getChildren().add(addConsultation);

                addConsultation.setOnAction(e->{
                    Router.go(
                            "/fxml/doctor/consultation_form.fxml",
                            "Nouvelle consultation",
                            (ConsultationFormController controller) ->
                                    controller.setRdvData(r.getPatientId(), r.getId())
                    );
                });
            }
            else{
                Label done = new Label("✔ Consultation déjà effectuée");
                done.setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
                actions.getChildren().add(done);
            }
        }

        // ===== CASE 3 : REFUSE =====
        else if(r.getStatus() == RdvStatus.REFUSE){
            Label refused = new Label("Rendez-vous refusé");
            refused.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold;");
            actions.getChildren().add(refused);
        }

        card.getChildren().addAll(header, body, actions);
        return card;
    }
}
