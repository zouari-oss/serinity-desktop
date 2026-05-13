package com.serinity.consultationcontrol.controller.rdv;

import com.serinity.consultationcontrol.model.RendezVous;
import com.serinity.consultationcontrol.model.User;
import com.serinity.consultationcontrol.service.EmailService;
import com.serinity.consultationcontrol.service.EmailTemplate;
import com.serinity.consultationcontrol.service.ProfanityService;
import com.serinity.consultationcontrol.service.RendezVousService;
import com.serinity.consultationcontrol.service.UserLookupService;
import com.serinity.consultationcontrol.util.AppSession;
import com.serinity.consultationcontrol.util.Router;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class RdvFormController {

    @FXML private TextField motifField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;

    private final RendezVousService service = new RendezVousService();
    private final UserLookupService userService = new UserLookupService();
    private final EmailService emailService = new EmailService();

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_list.fxml", "Doctors");
    }

    @FXML
    public void save(){
        String patientId = AppSession.getCurrentUserId();
        String doctorId = AppSession.getSelectedDoctorId();

        if(patientId == null || patientId.isBlank()){
            alert("Aucun utilisateur connecte.");
            return;
        }

        if(doctorId == null || doctorId.isBlank()){
            alert("Veuillez selectionner un medecin.");
            return;
        }

        if(motifField.getText().isBlank()){
            alert("Veuillez entrer un motif.");
            return;
        }

        if(datePicker.getValue() == null){
            alert("Choisissez une date.");
            return;
        }

        LocalTime time;
        try{
            time = LocalTime.parse(timeField.getText());
        } catch(Exception e){
            alert("Format heure HH:mm");
            return;
        }

        RendezVous rendezVous = new RendezVous();
        rendezVous.setPatientId(patientId);
        rendezVous.setDoctorId(doctorId);
        rendezVous.setMotif(motifField.getText());

        String original = descriptionArea.getText();
        String filtered = ProfanityService.cleanText(original);
        if(!original.equals(filtered)){
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setHeaderText("Message modifie automatiquement");
            warn.setContentText("Certains mots inappropries ont ete remplaces par ****.");
            warn.showAndWait();
        }

        rendezVous.setDescription(filtered);
        rendezVous.setDateTime(LocalDateTime.of(datePicker.getValue(), time));

        try{
            if(!service.insert(rendezVous)){
                alert("Le rendez-vous n'a pas pu etre enregistre.");
                return;
            }
        } catch(RuntimeException e){
            alert("Erreur lors de l'enregistrement du rendez-vous : " + e.getMessage());
            return;
        }

        User doctor = userService.findById(doctorId);
        User patient = userService.findById(patientId);
        if(doctor != null && patient != null){
            String html = EmailTemplate.rdvReceived(patient, doctor, rendezVous.getDateTime());
            emailService.sendEmail(
                    patient.getEmail(),
                    "Votre demande de rendez-vous a ete recue",
                    html
            );
        }

        alert("Rendez-vous envoye au medecin.");
        Router.go("/fxml/rdv/rdv_list.fxml", "Mes rendez-vous");
    }

    private void alert(String msg){
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
