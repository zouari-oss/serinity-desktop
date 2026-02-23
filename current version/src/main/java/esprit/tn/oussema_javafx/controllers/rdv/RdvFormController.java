package esprit.tn.oussema_javafx.controllers.rdv;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.RendezVous;
import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.services.EmailService;
import esprit.tn.oussema_javafx.services.EmailTemplate;
import esprit.tn.oussema_javafx.services.RendezVousService;
import esprit.tn.oussema_javafx.services.UserLookupService;
import esprit.tn.oussema_javafx.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import esprit.tn.oussema_javafx.services.ProfanityService;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RdvFormController {

    @FXML private TextField motifField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_list.fxml","Doctors");
    }
    private final RendezVousService service = new RendezVousService();
    private final UserLookupService userService = new UserLookupService();
    private final EmailService emailService = new EmailService();

    @FXML
    public void save(){

        if(motifField.getText().isBlank()){
            alert("Veuillez entrer un motif.");
            return;
        }

        if(datePicker.getValue()==null){
            alert("Choisissez une date.");
            return;
        }

        LocalTime t;
        try{
            t = LocalTime.parse(timeField.getText());
        }catch(Exception e){
            alert("Format heure HH:mm");
            return;
        }

        RendezVous r = new RendezVous();

        r.setPatientId(AppSession.getCurrentUserId());
        r.setDoctorId(AppSession.getSelectedDoctorId());
        r.setMotif(motifField.getText());
        String original = descriptionArea.getText();
        String filtered = ProfanityService.cleanText(original);

// si l'API a modifié le texte → on informe l'utilisateur
        if(!original.equals(filtered)){
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setHeaderText("Message modifié automatiquement");
            warn.setContentText("Certains mots inappropriés ont été remplacés par ****.");
            warn.showAndWait();
        }

        r.setDescription(filtered);        r.setDateTime(LocalDateTime.of(datePicker.getValue(), t));

        service.insert(r);

        alert("Rendez-vous envoyé au médecin ✔");
    //    User patient = userService.findById(AppSession.getCurrentUserId());
      //  User doctor = userService.findById(AppSession.getSelectedDoctorId());

        User doctor = userService.findById(1);
        User patient = userService.findById(1);
// construire le mail
        String html = EmailTemplate.rdvReceived(patient, doctor, r.getDateTime());

// envoyer email
        emailService.sendEmail(
                patient.getEmail(),
                "Votre demande de rendez-vous a été reçue",
                html
        );

        Router.go("/fxml/rdv/rdv_list.fxml","Mes rendez-vous");
    }

    private void alert(String msg){
        new Alert(Alert.AlertType.INFORMATION,msg,ButtonType.OK).showAndWait();
    }
}
