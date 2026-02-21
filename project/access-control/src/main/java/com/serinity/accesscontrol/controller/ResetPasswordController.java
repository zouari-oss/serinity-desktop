// `ResetPasswordController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

import com.serinity.accesscontrol.dto.ServiceResult;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.service.UserService;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * `reset-password.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/ResetPasswordController.java">
 *        ResetPasswordController.java
 *        </a>
 */
public final class ResetPasswordController extends com.serinity.accesscontrol.controller.base.BaseController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="CodePasswordField"
  private PasswordField CodePasswordField; // Value injected by FXMLLoader

  @FXML // fx:id="EmailTextField"
  private TextField EmailTextField; // Value injected by FXMLLoader

  @FXML // fx:id="NewPasswordField"
  private PasswordField NewPasswordField; // Value injected by FXMLLoader

  @FXML // fx:id="goBackImageView"
  private ImageView goBackImageView; // Value injected by FXMLLoader

  @FXML // fx:id="loginInterface"
  private AnchorPane loginInterface; // Value injected by FXMLLoader

  @FXML // fx:id="sendMailButtonImageView"
  private ImageView sendMailButtonImageView; // Value injected by FXMLLoader

  @FXML
  void onGoBackAction(ActionEvent event) {
    rootController.pop();
  }

  @FXML
  void onResetPasswordButtonAction(ActionEvent event) {
    ServiceResult<Void> serviceResult = UserService.confirmResetMail(EmailTextField.getText(),
        CodePasswordField.getText(),
        NewPasswordField.getText());

    if (serviceResult.isSuccess()) {
      rootController.pop();
    }
  }

  @FXML
  void onSendMailButtonAction(ActionEvent event) {
    rootController.showStatusMessage("Reset code sent! Check your email.", MessageStatus.SUCCESS);
    ServiceResult<Void> serviceResult = UserService.sendResetMail(EmailTextField.getText());
    if (serviceResult.isSuccess()) {
      CodePasswordField.setDisable(false);
      NewPasswordField.setDisable(false);
      // sen.setDisable(false);
      EmailTextField.setDisable(true);
      rootController.showStatusMessage("Reset code sent! Check your email.", MessageStatus.SUCCESS);
    } else {
      rootController.showStatusMessage("Failed to send reset email. Try again.", MessageStatus.ERROR);
    }
  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert CodePasswordField != null
        : "fx:id=\"CodePasswordField\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert EmailTextField != null
        : "fx:id=\"EmailTextField\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert NewPasswordField != null
        : "fx:id=\"NewPasswordField\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert goBackImageView != null
        : "fx:id=\"goBackImageView\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert loginInterface != null
        : "fx:id=\"loginInterface\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert sendMailButtonImageView != null
        : "fx:id=\"sendMailButtonImageView\" was not injected: check your FXML file 'reset-password.fxml'.";
  }
} // ResetPasswordController final class
