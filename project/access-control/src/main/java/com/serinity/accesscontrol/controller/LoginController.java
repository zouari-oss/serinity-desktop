// `LoginController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.flag.SupportedLanguage;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.service.UserService;
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

/**
 * `login.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-02
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/LoginController.java">
 *        LoginController.java
 *        </a>
 */
public class LoginController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="faceIdImageView"
  private ImageView faceIdImageView; // Value injected by FXMLLoader

  @FXML // fx:id="footerLabel"
  private Label footerLabel; // Value injected by FXMLLoader

  @FXML // fx:id="forgetPasswordHyperlink"
  private Hyperlink forgetPasswordHyperlink; // Value injected by FXMLLoader

  @FXML // fx:id="languageComboBox"
  private ComboBox<String> languageComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="loginIconImageView"
  private ImageView loginIconImageView; // Value injected by FXMLLoader

  @FXML // fx:id="loginIconImageView1"
  private ImageView loginIconImageView1; // Value injected by FXMLLoader

  @FXML // fx:id="loginInterface"
  private AnchorPane loginInterface; // Value injected by FXMLLoader

  @FXML // fx:id="loginSideWebView"
  private WebView loginSideWebView; // Value injected by FXMLLoader

  @FXML // fx:id="loginWelcomeLabel1"
  private Label loginWelcomeLabel1; // Value injected by FXMLLoader

  @FXML // fx:id="password"
  private PasswordField password; // Value injected by FXMLLoader

  @FXML // fx:id="signInButton"
  private Button signInButton; // Value injected by FXMLLoader

  @FXML // fx:id="signInHyperlink"
  private Hyperlink signInHyperlink; // Value injected by FXMLLoader

  @FXML // fx:id="signUpButton"
  private Button signUpButton; // Value injected by FXMLLoader

  @FXML // fx:id="signUpConfirmPasswordField"
  private PasswordField signUpConfirmPasswordField; // Value injected by FXMLLoader

  @FXML // fx:id="signUpHyperlink"
  private Hyperlink signUpHyperlink; // Value injected by FXMLLoader

  @FXML // fx:id="signUpPasswordField"
  private PasswordField signUpPasswordField; // Value injected by FXMLLoader

  @FXML // fx:id="singnUpEmailTextField"
  private TextField singnUpEmailTextField; // Value injected by FXMLLoader

  @FXML // fx:id="signUpUserRoleComboBox"
  private ComboBox<UserRole> signUpUserRoleComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="usernameOrEmail"
  private TextField usernameOrEmail; // Value injected by FXMLLoader

  // ##############################
  // ### SLOT HANDLER FUNCTIONS ###
  // ##############################

  @FXML
  void onForgetPasswordHyperlinkAction(ActionEvent event) {
    final Stage stage = (Stage) forgetPasswordHyperlink.getScene().getWindow();
    stage.setScene(FXMLLoaderUtil.loadScene(
        this.getClass(),
        ResourceFile.RESET_PASSWORD_FXML.getFileName(),
        I18nUtil.getBundle()));
  }

  @FXML
  void onLanguageComboBoxAction(final ActionEvent event) {
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
  void onSignInButtonAction(final ActionEvent event) {
    UserService.signIn(
        usernameOrEmail.getText(),
        password.getText());
  }

  @FXML
  void onSignInHyperlinkAction(final ActionEvent event) {
    FXMLAnimationUtil.slideFullScreen(
        loginSideWebView,
        loginInterface
            .getScene()
            .getWindow()
            .getWidth(),
        false);
  }

  @FXML
  void onSignUpButtonAction(final ActionEvent event) {
    UserService.signUp( // TODO: I need to return a message from `signUp`
        singnUpEmailTextField.getText(),
        signUpPasswordField.getText(),
        signUpConfirmPasswordField.getText(),
        signUpUserRoleComboBox.getValue());

    // Sign-up success
    final Stage stage = (Stage) signUpUserRoleComboBox.getScene().getWindow();
    stage.setScene(FXMLLoaderUtil.loadScene(
        this.getClass(),
        ResourceFile.DASHBOARD_FXML.getFileName(),
        I18nUtil.getBundle()));
  }

  @FXML
  void onSignUpHyperlinkAction(final ActionEvent event) {
    FXMLAnimationUtil.slideFullScreen(
        loginSideWebView,
        loginInterface
            .getScene()
            .getWindow()
            .getWidth(),
        true);
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert faceIdImageView != null : "fx:id=\"faceIdImageView\" was not injected: check your FXML file 'login.fxml'.";
    assert footerLabel != null : "fx:id=\"footerLabel\" was not injected: check your FXML file 'login.fxml'.";
    assert forgetPasswordHyperlink != null
        : "fx:id=\"forgetPasswordHyperlink\" was not injected: check your FXML file 'login.fxml'.";
    assert languageComboBox != null : "fx:id=\"languageComboBox\" was not injected: check your FXML file 'login.fxml'.";
    assert loginInterface != null : "fx:id=\"loginInterface\" was not injected: check your FXML file 'login.fxml'.";
    assert loginSideWebView != null : "fx:id=\"loginSideWebView\" was not injected: check your FXML file 'login.fxml'.";
    assert loginWelcomeLabel1 != null
        : "fx:id=\"loginWelcomeLabel1\" was not injected: check your FXML file 'login.fxml'.";
    assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'login.fxml'.";
    assert signInButton != null : "fx:id=\"signInButton\" was not injected: check your FXML file 'login.fxml'.";
    assert signInHyperlink != null : "fx:id=\"signInHyperlink\" was not injected: check your FXML file 'login.fxml'.";
    assert signUpButton != null : "fx:id=\"signUpButton\" was not injected: check your FXML file 'login.fxml'.";
    assert signUpConfirmPasswordField != null
        : "fx:id=\"signUpConfirmPasswordField\" was not injected: check your FXML file 'login.fxml'.";
    assert signUpHyperlink != null : "fx:id=\"signUpHyperlink\" was not injected: check your FXML file 'login.fxml'.";
    assert signUpPasswordField != null
        : "fx:id=\"signUpPasswordField\" was not injected: check your FXML file 'login.fxml'.";
    assert singnUpEmailTextField != null
        : "fx:id=\"singnUpEmailTextField\" was not injected: check your FXML file 'login.fxml'.";
    assert signUpUserRoleComboBox != null
        : "fx:id=\"userRoleComboBox\" was not injected: check your FXML file 'login.fxml'.";
    assert usernameOrEmail != null : "fx:id=\"usernameOrEmail\" was not injected: check your FXML file 'login.fxml'.";

    // Custom initialization
    languageComboBoxInit();
    signUpUserRoleComboBoxInit();
    loginSideWebViewInit();
  }

  // #########################################
  // ### CUSTOM INITIALIZATION FUNCTION(S) ###
  // #########################################

  private void languageComboBoxInit() {
    languageComboBox.getItems()
        .addAll((ObservableList<String>) FXCollections.observableArrayList(
            I18nUtil.getSupportedLanguagesToString()));
    languageComboBox.setValue(I18nUtil.getLocale().getLanguage());
  }

  private void signUpUserRoleComboBoxInit() {
    signUpUserRoleComboBox.getItems().addAll(
        UserRole.PATIENT, // Patient
        UserRole.THERAPIST // Therapist
    );
    signUpUserRoleComboBox.setValue(UserRole.PATIENT); // Default Value: Patient
  }

  private void loginSideWebViewInit() {
    final URL url = getClass().getResource(ResourceFile.LOGIN_SIDE_HTML.getFileName());

    if (url == null) {
      throw new IllegalStateException("Resource not found: "
          + ResourceFile.LOGIN_SIDE_HTML.getFileName());
    }

    loginSideWebView.getEngine().load(url.toExternalForm());
  }
} // LoginController class
