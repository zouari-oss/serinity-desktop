// `ResetPasswordController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StageTitled;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.dto.ServiceResult;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.service.UserService;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

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
public final class ResetPasswordController implements StackNavigable, StatusMessageProvider, StageTitled {

  @Override
  public String getSceneTitleKey() {
    return "app.scene.title.reset_password";
  }


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

  @FXML // fx:id="resetPasswordButton"
  private Button resetPasswordButton; // Value injected by FXMLLoader

  @FXML // fx:id="sendMailButton"
  private Button sendMailButton; // Value injected by FXMLLoader

  @FXML // fx:id="sendMailButtonImageView"
  private ImageView sendMailButtonImageView; // Value injected by FXMLLoader

  private StackPane stackHost; // Will be injected by RootController

  private StatusMessageProvider statusProvider; // Delegate to RootController

  public void setStatusProvider(final StatusMessageProvider provider) {
    this.statusProvider = provider;
  }

  @Override
  public StackPane getStackHost() {
    return stackHost;
  }

  @Override
  public void setStackHost(final StackPane host) {
    this.stackHost = host;
  }

  @Override
  public void showStatusMessage(final String message, final MessageStatus status) {
    if (statusProvider != null) {
      statusProvider.showStatusMessage(message, status);
    }
  }

  @FXML
  void onGoBackAction(final ActionEvent event) {
    pop();
  }

  @FXML
  void onSendMailButtonAction(final ActionEvent event) {
    final ServiceResult<Void> serviceResult = UserService.sendResetMail(EmailTextField.getText());
    if (serviceResult.isSuccess()) {
      EmailTextField.setDisable(true);
      sendMailButton.setDisable(true);
      CodePasswordField.setDisable(false);
      NewPasswordField.setDisable(false);
      resetPasswordButton.setDisable(false);
      showStatusMessage(serviceResult.getMessage(), MessageStatus.SUCCESS);

    } else {
      showStatusMessage(serviceResult.getMessage(), MessageStatus.ERROR);
    }
  }

  @FXML
  void onResetPasswordButtonAction(final ActionEvent event) {
    final ServiceResult<Void> serviceResult = UserService.confirmResetMail(EmailTextField.getText(),
        CodePasswordField.getText(),
        NewPasswordField.getText());

    if (serviceResult.isSuccess()) {
      showStatusMessage(serviceResult.getMessage(), MessageStatus.SUCCESS);
      pop();

    } else {
      showStatusMessage(serviceResult.getMessage(), MessageStatus.ERROR);
    }
  }

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
    assert resetPasswordButton != null
        : "fx:id=\"resetPasswordButton\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert sendMailButton != null
        : "fx:id=\"sendMailButton\" was not injected: check your FXML file 'reset-password.fxml'.";
    assert sendMailButtonImageView != null
        : "fx:id=\"sendMailButtonImageView\" was not injected: check your FXML file 'reset-password.fxml'.";
  }
} // ResetPasswordController final class
