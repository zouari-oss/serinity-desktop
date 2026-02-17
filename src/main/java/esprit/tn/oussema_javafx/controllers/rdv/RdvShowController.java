package esprit.tn.oussema_javafx.controllers.rdv;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.RendezVous;
import esprit.tn.oussema_javafx.models.RdvStatus;
import esprit.tn.oussema_javafx.services.RendezVousService;
import esprit.tn.oussema_javafx.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class RdvShowController {

    @FXML private Label titleLabel, patientLabel, doctorLabel, dateLabel, statusLabel, motifLabel, proposedLabel;
    @FXML private VBox proposedBox;
    @FXML private Button editBtn;

    private final RendezVousService service = new RendezVousService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private RendezVous r;

    @FXML
    public void initialize(){

        int id = AppSession.getSelectedRdvId();
        r = service.findById(id);

        if(r == null){
            Router.go("/fxml/rdv/rdv_list.fxml","Mes Rendez-vous");
            return;
        }

        titleLabel.setText("Rendez-vous #" + r.getId());

        patientLabel.setText("Patient : " + r.getPatientName());
        doctorLabel.setText("Dr " + r.getDoctorName());
        dateLabel.setText(r.getDateTime()==null?"-":r.getDateTime().format(fmt));

        statusLabel.setText(formatStatus(r.getStatus()));
        statusLabel.getStyleClass().add(statusStyle(r.getStatus()));

        motifLabel.setText(safe(r.getMotif()));

        // proposition médecin
        if(r.getProposedDateTime()!=null){
            proposedBox.setVisible(true);
            proposedLabel.setText(r.getProposedDateTime().format(fmt));
        }else{
            proposedBox.setVisible(false);
        }

        // IMPORTANT : cacher modifier si confirmé
        if(r.getStatus() == RdvStatus.APPROUVE){
            editBtn.setVisible(false);
        }
    }

    private String safe(String s){
        return (s==null || s.isBlank()) ? "-" : s;
    }

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
    public void back(){
        AppSession.clearSelection();
        Router.go("/fxml/rdv/rdv_list.fxml","Mes Rendez-vous");
    }

    @FXML
    public void edit(){
        AppSession.setSelectedRdvId(r.getId());
        Router.go("/fxml/rdv/rdv_form.fxml","Edit RDV");
    }
}
