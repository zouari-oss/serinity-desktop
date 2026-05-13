package com.serinity.consultationcontrol.controller.rdv;

import com.serinity.consultationcontrol.model.RdvStatus;
import com.serinity.consultationcontrol.model.RendezVous;
import com.serinity.consultationcontrol.service.GoogleCalendarLinkService;
import com.serinity.consultationcontrol.service.RendezVousService;
import com.serinity.consultationcontrol.util.AppSession;
import com.serinity.consultationcontrol.util.Router;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RdvListController {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private VBox emptyBox;
    @FXML private Button aiButton;

    private final RendezVousService service = new RendezVousService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize(){
        refresh();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
    }

    @FXML
    public void refresh(){
        String patientId = AppSession.getCurrentUserId();
        List<RendezVous> list = service.findAllByPatient(patientId);
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        cardsPane.getChildren().clear();
        for(RendezVous rendezVous : list){
            if(!query.isEmpty()){
                String haystack = (rendezVous.getDoctorName() + " " + safe(rendezVous.getMotif()) + " " + rendezVous.getStatus())
                        .toLowerCase();
                if(!haystack.contains(query)){
                    continue;
                }
            }
            cardsPane.getChildren().add(card(rendezVous));
        }

        emptyBox.setVisible(cardsPane.getChildren().isEmpty());
    }

    @FXML
    public void goNew(){
        if(AppSession.getSelectedDoctorId() == null || AppSession.getSelectedDoctorId().isBlank()){
            Router.go("/fxml/doctor/doctor_list.fxml", "Medecins");
            return;
        }

        Router.go("/fxml/rdv/rdv_form.fxml", "New RDV");
    }

    @FXML
    public void openAI(){
        ScaleTransition click = new ScaleTransition(Duration.millis(120), aiButton);
        click.setToX(0.9);
        click.setToY(0.9);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), aiButton);
        shake.setFromX(-3);
        shake.setToX(3);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();

        Router.go("/fxml/ai/ai_assistant.fxml", "Assistant IA");
    }

    @FXML
    public void goDoctors(){
        Router.go("/fxml/doctor/doctor_list.fxml", "Medecins");
    }

    private VBox card(RendezVous rendezVous){
        VBox box = new VBox(14);
        box.getStyleClass().add("rdv-item");
        box.setPrefWidth(320);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label doctor = new Label("Dr " + rendezVous.getDoctorName());
        doctor.getStyleClass().add("rdv-doctor");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(formatStatus(rendezVous.getStatus()));
        status.getStyleClass().addAll("rdv-status", statusStyle(rendezVous.getStatus()));
        header.getChildren().addAll(doctor, spacer, status);

        Label date = new Label(rendezVous.getDateTime().format(fmt));
        date.getStyleClass().add("rdv-date");

        Label motif = new Label(safe(rendezVous.getMotif()));
        motif.getStyleClass().add("rdv-motif");
        motif.setWrapText(true);

        HBox actions = new HBox(10);
        Button show = new Button("Details");
        show.getStyleClass().add("secondary-btn");
        show.setOnAction(e -> {
            AppSession.setSelectedRdvId(rendezVous.getId());
            Router.go("/fxml/rdv/rdv_show.fxml", "RDV Details");
        });
        actions.getChildren().add(show);

        if(rendezVous.getStatus() != RdvStatus.APPROUVE){
            Button delete = new Button("Annuler");
            delete.getStyleClass().add("danger-btn");
            delete.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setHeaderText("Annuler le rendez-vous ?");
                confirm.setContentText("Voulez-vous vraiment annuler ce rendez-vous ?");
                confirm.showAndWait().ifPresent(result -> {
                    if(result == ButtonType.OK){
                        service.delete(rendezVous.getId());
                        refresh();
                    }
                });
            });
            actions.getChildren().add(delete);
        }

        Button googleBtn = new Button("Ajouter a Google");
        googleBtn.getStyleClass().add("primary-btn");
        googleBtn.setOnAction(e -> GoogleCalendarLinkService.openEvent(
                "Rendez-vous medical avec Dr " + rendezVous.getDoctorName(),
                safe(rendezVous.getMotif()),
                "Cabinet medical",
                rendezVous.getDateTime(),
                rendezVous.getDateTime().plusMinutes(30)
        ));
        actions.getChildren().add(googleBtn);

        if(rendezVous.getStatus() == RdvStatus.MODIFICATION_PROPOSEE){
            Label info = new Label("Le medecin propose un nouvel horaire.");
            info.setStyle("-fx-text-fill:#0c5460; -fx-font-size:12px;");
            box.getChildren().add(info);
        }

        box.getChildren().addAll(header, date, motif, actions);
        return box;
    }

    private String safe(String value){
        return value == null || value.isBlank() ? "Motif non precise" : value;
    }

    private String formatStatus(RdvStatus status){
        return switch (status){
            case EN_ATTENTE -> "En attente";
            case APPROUVE -> "Confirme";
            case REFUSE -> "Refuse";
            case MODIFICATION_PROPOSEE -> "Modification proposee";
        };
    }

    private String statusStyle(RdvStatus status){
        return switch (status){
            case APPROUVE -> "status-ok";
            case EN_ATTENTE -> "status-wait";
            case REFUSE -> "status-refused";
            case MODIFICATION_PROPOSEE -> "status-modified";
        };
    }
}
