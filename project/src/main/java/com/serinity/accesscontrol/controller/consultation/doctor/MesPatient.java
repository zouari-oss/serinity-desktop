package com.serinity.accesscontrol.controller.consultation.doctor;

import com.serinity.accesscontrol.util.consultation.Router;
import com.serinity.accesscontrol.model.consultation.User;
import com.serinity.accesscontrol.service.consultation.RendezVousService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MesPatient {

    @FXML
    private FlowPane patientsPane;

    private final RendezVousService service = new RendezVousService();

    private final int doctorId = 2; // TODO: replace with logged doctor

    @FXML
    public void initialize(){
        loadPatients();
    }

    private void loadPatients(){
        try{
            List<User> patients = service.findPatientsByDoctor(doctorId);

            for(User u : patients){

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/doctor/patient_card.fxml")
                );

                VBox card = loader.load();

                PatientCardController controller = loader.getController();
                controller.setData(u);

                patientsPane.getChildren().add(card);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void goRdv(ActionEvent event) {
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");

    }

    public void goConsultations(ActionEvent event) {
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");

    }

    public void goDashboard(ActionEvent event) {
        Router.go("/fxml/doctor/dashboard.fxml","Mes RDV");

    }
}