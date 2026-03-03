package com.serinity.accesscontrol.controller.consultation.doctor;

import com.serinity.accesscontrol.model.consultation.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PatientCardController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label visitLabel;

    public void setData(User u){
        nameLabel.setText(u.getFullName());
        emailLabel.setText("📧 " + u.getEmail());
        phoneLabel.setText("📞 " + u.getPhone());
        addressLabel.setText("📍 " + u.getAddress());

        visitLabel.setText("Patient enregistré");
    }
}