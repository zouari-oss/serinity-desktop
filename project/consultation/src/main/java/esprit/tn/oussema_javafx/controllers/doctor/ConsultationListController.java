package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.services.ConsultationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private int patientId = -1;

    public void setData(int rapportId, int patientId){
        this.rapportId = rapportId;
        this.patientId = patientId;
        load();
    }

    private void load(){

        cardsPane.getChildren().clear();

        List<Consultation> list = service.findAll()
                .stream()
                .filter(c -> c.getRapportId() == rapportId)
                .toList();

        if(list.isEmpty()){
            emptyBox.setVisible(true);
            return;
        }

        emptyBox.setVisible(false);

        for(Consultation c : list){
            cardsPane.getChildren().add(card(c));
        }
    }

    private VBox card(Consultation c){

        VBox card = new VBox(10);
        card.getStyleClass().add("doctor-card");

        Label date = new Label("📅 " + c.getDateConsultation().format(fmt));
        Label diag = new Label("Diagnostic : " + safe(c.getDiagnostic()));
        Label pres = new Label("Prescription : " + safe(c.getPrescription()));
        Label notes = new Label("Notes : " + safe(c.getNotes()));

        diag.setWrapText(true);
        pres.setWrapText(true);
        notes.setWrapText(true);

        HBox actions = new HBox(10);

        Button edit = new Button("✏ Modifier");
        Button delete = new Button("🗑 Supprimer");

        edit.setOnAction(e -> {
            Router.go(
                    "/fxml/doctor/consultation_form.fxml",
                    "Modifier consultation",
                    (ConsultationFormController controller) ->
                            controller.setConsultation(c, patientId, c.getRendezVousId())
            );
        });

        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer consultation ?");
            confirm.setContentText("Action irréversible");

            confirm.showAndWait().ifPresent(r -> {
                if(r == ButtonType.OK){
                    service.delete(c.getId());
                    load();
                }
            });
        });


        actions.getChildren().addAll(edit, delete);
        card.getChildren().addAll(date, diag, pres, notes, actions);

        return card;
    }

    private String safe(String s){
        return s == null || s.isBlank() ? "-" : s;
    }
    @FXML
    private void back(ActionEvent event){
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");
    }

    @FXML
    private void addConsultation(ActionEvent event){

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Action impossible");
        alert.setContentText("Une consultation doit être créée depuis un rendez-vous approuvé.");
        alert.showAndWait();
    }

}
