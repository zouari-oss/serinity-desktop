package esprit.tn.oussema_javafx.controllers.rdv;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.RendezVous;
import esprit.tn.oussema_javafx.models.RdvStatus;
import esprit.tn.oussema_javafx.services.RendezVousService;
import esprit.tn.oussema_javafx.utils.AppSession;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import esprit.tn.oussema_javafx.services.GoogleCalendarLinkService;
import javafx.util.Duration;

public class RdvListController {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private VBox emptyBox;
    @FXML
    private Button aiButton;
    private final RendezVousService service = new RendezVousService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize(){
        refresh();

        // recherche dynamique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
    }

    @FXML
    public void refresh(){

        int patientId = AppSession.getCurrentUserId();
        List<RendezVous> list = service.findAllByPatient(patientId);

        String q = (searchField.getText()==null) ? "" : searchField.getText().trim().toLowerCase();

        cardsPane.getChildren().clear();

        for(RendezVous r : list){

            if(!q.isEmpty()){
                String hay = (r.getDoctorName()+" "+safe(r.getMotif())+" "+r.getStatus()).toLowerCase();
                if(!hay.contains(q)) continue;
            }

            cardsPane.getChildren().add(card(r));
        }

        emptyBox.setVisible(cardsPane.getChildren().isEmpty());
    }

    private VBox card(RendezVous r){

        VBox box = new VBox(14);
        box.getStyleClass().add("rdv-item");
        box.setPrefWidth(320);

        /* ===== HEADER ===== */
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label doctor = new Label("Dr " + r.getDoctorName());
        doctor.getStyleClass().add("rdv-doctor");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(formatStatus(r.getStatus()));
        status.getStyleClass().addAll("rdv-status", statusStyle(r.getStatus()));

        header.getChildren().addAll(doctor, spacer, status);

        /* ===== DATE ===== */
        Label date = new Label("📅 " + r.getDateTime().format(fmt));
        date.getStyleClass().add("rdv-date");

        /* ===== MOTIF ===== */
        Label motif = new Label(safe(r.getMotif()));
        motif.getStyleClass().add("rdv-motif");
        motif.setWrapText(true);

        /* message si modification */
        if(r.getStatus() == RdvStatus.MODIFICATION_PROPOSEE){
            Label info = new Label("Le médecin propose un nouvel horaire.");
            info.setStyle("-fx-text-fill:#0c5460; -fx-font-size:12px;");
            box.getChildren().add(info);
        }

        /* ===== ACTIONS ===== */
        HBox actions = new HBox(10);

        Button show = new Button("Détails");
        show.getStyleClass().add("secondary-btn");

        show.setOnAction(e->{
            AppSession.setSelectedRdvId(r.getId());
            Router.go("/fxml/rdv/rdv_show.fxml","RDV Details");
        });

        actions.getChildren().add(show);

        /* ===== RULE: RDV APPROUVE = PAS D'ANNULATION ===== */
        if(r.getStatus() != RdvStatus.APPROUVE){

            Button delete = new Button("Annuler");
            delete.getStyleClass().add("danger-btn");

            delete.setOnAction(e->{
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setHeaderText("Annuler le rendez-vous ?");
                confirm.setContentText("Voulez-vous vraiment annuler ce rendez-vous ?");

                confirm.showAndWait().ifPresent(res -> {
                    if(res == ButtonType.OK){
                        service.delete(r.getId());
                        refresh();
                    }
                });
            });

            actions.getChildren().add(delete);
        }
        Button googleBtn = new Button("📅 Ajouter à Google");
        googleBtn.getStyleClass().add("primary-btn");

        googleBtn.setOnAction(e -> {

            GoogleCalendarLinkService.openEvent(
                    "Rendez-vous médical avec Dr " + r.getDoctorName(),
                    safe(r.getMotif()),
                    "Cabinet médical",
                    r.getDateTime(),
                    r.getDateTime().plusMinutes(30)
            );

        });

        actions.getChildren().add(googleBtn);
        box.getChildren().addAll(header, date, motif, actions);
        return box;
    }

    private String safe(String s){
        return (s==null || s.isBlank()) ? "Motif non précisé" : s;
    }

    /* ===== Traduction des statuts ===== */

    private String formatStatus(RdvStatus status){
        switch (status){
            case EN_ATTENTE: return "En attente";
            case APPROUVE: return "Confirmé";
            case REFUSE: return "Refusé";
            case MODIFICATION_PROPOSEE: return "Modification proposée";
            default: return status.name();
        }
    }

    private String statusStyle(RdvStatus status){
        switch (status){
            case APPROUVE: return "status-ok";
            case EN_ATTENTE: return "status-wait";
            case REFUSE: return "status-refused";
            case MODIFICATION_PROPOSEE: return "status-modified";
            default: return "";
        }
    }

    @FXML
    public void goNew(){
        AppSession.clearSelection();
        Router.go("/fxml/rdv/rdv_form.fxml","New RDV");
    }
    @FXML
    public void openAI(){
        // click bounce effect
        ScaleTransition click = new ScaleTransition(Duration.millis(120), aiButton);
        click.setToX(0.9);
        click.setToY(0.9);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();

        // petite vibration
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), aiButton);
        shake.setFromX(-3);
        shake.setToX(3);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();

        Router.go("/fxml/ai/ai_assistant.fxml","Assistant IA");
    }
    @FXML
    public void goDoctors(){
        Router.go("/fxml/doctor/doctor_list.fxml","Médecins");
    }
}
