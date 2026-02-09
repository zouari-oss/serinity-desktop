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

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.flag.SupportedLanguage;
import com.serinity.accesscontrol.util.FXMLAnimationUtil;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` import(s)
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class LoginController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="faceIdImageView"
  private ImageView faceIdImageView; // Value injected by FXMLLoader

  @FXML // fx:id="languageComboBox"
  private ComboBox<String> languageComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="loginIconImageView"
  private ImageView loginIconImageView; // Value injected by FXMLLoader

  @FXML // fx:id="loginInterface"
  private AnchorPane loginInterface; // Value injected by FXMLLoader

  @FXML // fx:id="loginSideWebView"
  private WebView loginSideWebView; // Value injected by FXMLLoader

  @FXML // fx:id="loginWelcomeLabel"
  private Label loginWelcomeLabel; // Value injected by FXMLLoader

  @FXML // fx:id="password"
  private PasswordField password; // Value injected by FXMLLoader

  @FXML // fx:id="signInButton"
  private Button signInButton; // Value injected by FXMLLoader

  @FXML // fx:id="signupHyperlink"
  private Hyperlink signupHyperlink; // Value injected by FXMLLoader

  @FXML // fx:id="usernameOrEmail"
  private TextField usernameOrEmail; // Value injected by FXMLLoader

  // ##############################
  // ### SLOT HANDLER FUNCTIONS ###
  // ##############################

  @FXML
  void languageComboBoxAction() {
    I18nUtil.setLocale(
        languageComboBox.getValue().equals(SupportedLanguage.FR.getCode())
            ? SupportedLanguage.FR.getLocale() // To `fr`
            : SupportedLanguage.EN.getLocale()); // To `en`

    ((Stage) loginInterface.getScene().getWindow())
        .setScene(FXMLLoaderUtil.loadScene(
            getClass(),
            ResourceFile.LOGIN_FXML.getFileName(),
            I18nUtil.getBundle()));
  }

  @FXML
  void onLoginButtonAction(final ActionEvent event) {

  }

  @FXML
  void onSignupHyperlinkAction(final ActionEvent event) {
    FXMLAnimationUtil.slideFullScreen(
        loginSideWebView,
        loginInterface
            .getScene()
            .getWindow()
            .getWidth(),
        true);
  }

  // ################################
  // ### INITIALIZATION FUNCTIONS ###
  // ################################
  private void languageComboBoxInit() {
    languageComboBox.getItems()
        .addAll((ObservableList<String>) FXCollections.observableArrayList(
            I18nUtil.getSupportedLanguagesToString()));
    languageComboBox.setValue(I18nUtil.getLocale().getLanguage());
  }

  private void loginSideWebViewInit() {
    URL url = getClass().getResource(ResourceFile.LOGIN_SIDE_HTML.getFileName());

    if (url == null) {
      throw new IllegalStateException("Resource not found: "
          + ResourceFile.LOGIN_SIDE_HTML.getFileName());
    }

    loginSideWebView.getEngine().load(url.toExternalForm());
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert faceIdImageView != null : "fx:id=\"faceIdImageView\" was not injected: check your FXML file 'login.fxml'.";
    assert languageComboBox != null : "fx:id=\"languageComboBox\" was not injected: check your FXML file 'login.fxml'.";
    assert loginIconImageView != null
        : "fx:id=\"loginIconImageView\" was not injected: check your FXML file 'login.fxml'.";
    assert loginInterface != null : "fx:id=\"loginInterface\" was not injected: check your FXML file 'login.fxml'.";
    assert loginSideWebView != null : "fx:id=\"loginSideWebView\" was not injected: check your FXML file 'login.fxml'.";
    assert loginWelcomeLabel != null
        : "fx:id=\"loginWelcomeLabel\" was not injected: check your FXML file 'login.fxml'.";
    assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'login.fxml'.";
    assert signInButton != null : "fx:id=\"signInButton\" was not injected: check your FXML file 'login.fxml'.";
    assert signupHyperlink != null : "fx:id=\"signupHyperlink\" was not injected: check your FXML file 'login.fxml'.";
    assert usernameOrEmail != null : "fx:id=\"usernameOrEmail\" was not injected: check your FXML file 'login.fxml'.";

    // Custom initialization
    languageComboBoxInit();
    loginSideWebViewInit();
  }
} // LoginController class
