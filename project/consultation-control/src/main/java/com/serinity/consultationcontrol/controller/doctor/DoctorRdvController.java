package com.serinity.consultationcontrol.controller.doctor;

import com.serinity.consultationcontrol.model.RdvStatus;
import com.serinity.consultationcontrol.model.RendezVous;
import com.serinity.consultationcontrol.service.ConsultationService;
import com.serinity.consultationcontrol.service.MedicalReportPdfService;
import com.serinity.consultationcontrol.service.RendezVousService;
import com.serinity.consultationcontrol.service.UserLookupService;
import com.serinity.consultationcontrol.util.AppSession;
import com.serinity.consultationcontrol.util.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public void goConsultations(ActionEvent event) {
        Router.go("/fxml/doctor/mesPatient.fxml", "Mes Patients");
    }

    public void goDashboard(ActionEvent event) {
        Router.go("/fxml/doctor/dashboard.fxml", "Doctor Dashboard");
    }

    @FXML
    private void goPatients(){
        Router.go("/fxml/doctor/mesPatient.fxml", "Mes Patients");
    }

    @FXML
    private void openAI(){
        System.out.println("AI Assistant");
    }

    @FXML
    private void logout(){
        System.out.println("Logout");
    }

    @FXML
    public void refresh(){
        cardsPane.getChildren().clear();

        String doctorId = AppSession.getCurrentUserId();
        List<RendezVous> list = service.findAllByDoctor(doctorId);

        if(list.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);
        for(RendezVous rendezVous : list){
            cardsPane.getChildren().add(createCard(rendezVous));
        }
    }

    private VBox createCard(RendezVous rendezVous){
        VBox card = new VBox();
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(340);
        card.setSpacing(12);

        HBox header = new HBox();
        header.getStyleClass().add("doctor-card-header");

        Label patient = new Label(rendezVous.getPatientName());
        patient.getStyleClass().add("doctor-name");

        Label status = new Label(rendezVous.getStatus().name());
        status.getStyleClass().add("doctor-card-status");

        switch (rendezVous.getStatus()){
            case EN_ATTENTE -> status.getStyleClass().add("status-wait");
            case APPROUVE -> status.getStyleClass().add("status-ok");
            case REFUSE -> status.getStyleClass().add("status-refused");
            case MODIFICATION_PROPOSEE -> status.getStyleClass().add("status-modified");
        }

        header.getChildren().addAll(patient, new Label("     "), status);

        VBox body = new VBox(8);
        body.getStyleClass().add("doctor-card-body");

        Label date = new Label(rendezVous.getDateTime().format(fmt));
        date.getStyleClass().add("rdv-date");

        Label motif = new Label("Motif : " + (rendezVous.getMotif() == null ? "Non precise" : rendezVous.getMotif()));
        motif.setWrapText(true);
        motif.getStyleClass().add("rdv-motif");
        body.getChildren().addAll(date, motif);

        HBox actions = new HBox(10);
        actions.getStyleClass().add("doctor-card-actions");

        Button accept = new Button("Accepter");
        accept.getStyleClass().add("btn-accept");

        Button refuse = new Button("Refuser");
        refuse.getStyleClass().add("btn-refuse");

        Button addConsultation = new Button("Ajouter consultation");
        addConsultation.getStyleClass().add("btn-consult");

        Button openReport = new Button("Dossier patient");
        openReport.getStyleClass().add("btn-consult");

        if(rendezVous.getStatus() == RdvStatus.EN_ATTENTE){
            actions.getChildren().addAll(accept, refuse);

            accept.setOnAction(e -> {
                rendezVous.setStatus(RdvStatus.APPROUVE);
                service.update(rendezVous);
                refresh();
            });

            refuse.setOnAction(e -> {
                rendezVous.setStatus(RdvStatus.REFUSE);
                service.update(rendezVous);
                refresh();
            });
        } else if(rendezVous.getStatus() == RdvStatus.APPROUVE){
            boolean alreadyConsulted = consultationService.existsByRdv(rendezVous.getId());
            actions.getChildren().add(openReport);

            openReport.setOnAction(e -> Router.go(
                    "/fxml/doctor/patient_report.fxml",
                    "Dossier Patient",
                    (PatientReportController controller) -> controller.setPatientId(rendezVous.getPatientId())
            ));

            if(!alreadyConsulted){
                actions.getChildren().add(addConsultation);
                addConsultation.setOnAction(e -> Router.go(
                        "/fxml/doctor/consultation_form.fxml",
                        "Nouvelle consultation",
                        (ConsultationFormController controller) ->
                                controller.setRdvData(rendezVous.getPatientId(), rendezVous.getId())
                ));
            } else {
                Label done = new Label("Consultation deja effectuee");
                done.getStyleClass().add("doctor-verified");
                actions.getChildren().add(done);
            }
        } else if(rendezVous.getStatus() == RdvStatus.REFUSE){
            Label refused = new Label("Rendez-vous refuse");
            refused.getStyleClass().addAll("doctor-card-status", "status-refused");
            actions.getChildren().add(refused);
        }

        card.getChildren().addAll(header, body, actions);
        return card;
    }
}
