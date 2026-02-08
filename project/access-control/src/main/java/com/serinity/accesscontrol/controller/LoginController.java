/**
 * LoginController.java
 *
 * `login.fxml` controller class
 *
 * <p>none</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-02
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/LoginController.java" 
 * target="_blank">
 * LoginController.java
 * </a>
 */

// `LoginController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class LoginController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="faceIdImageView"
  private ImageView faceIdImageView; // Value injected by FXMLLoader

  @FXML // fx:id="languageMenuButton"
  private MenuButton languageMenuButton; // Value injected by FXMLLoader

  @FXML // fx:id="loginButton"
  private Button loginButton; // Value injected by FXMLLoader

  @FXML // fx:id="login_interface"
  private AnchorPane login_interface; // Value injected by FXMLLoader

  @FXML // fx:id="password"
  private PasswordField password; // Value injected by FXMLLoader

  @FXML // fx:id="signupHyperlink"
  private Hyperlink signupHyperlink; // Value injected by FXMLLoader

  @FXML // fx:id="usernameOrEmail"
  private TextField usernameOrEmail; // Value injected by FXMLLoader

  @FXML
  void onLanguageMenuButtonaction(final ActionEvent event) {

  }

  @FXML
  void onLoginButtonAction(final ActionEvent event) {

  }

  @FXML
  void onSignupHyperlinkAction(final ActionEvent event) {

  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert faceIdImageView != null : "fx:id=\"faceIdImageView\" was not injected: check your FXML file 'login.fxml'.";
    assert languageMenuButton != null
        : "fx:id=\"languageMenuButton\" was not injected: check your FXML file 'login.fxml'.";
    assert loginButton != null : "fx:id=\"loginButton\" was not injected: check your FXML file 'login.fxml'.";
    assert login_interface != null : "fx:id=\"login_interface\" was not injected: check your FXML file 'login.fxml'.";
    assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'login.fxml'.";
    assert signupHyperlink != null : "fx:id=\"signupHyperlink\" was not injected: check your FXML file 'login.fxml'.";
    assert usernameOrEmail != null : "fx:id=\"usernameOrEmail\" was not injected: check your FXML file 'login.fxml'.";
  }
} // LoginController class
