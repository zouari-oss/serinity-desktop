// `LoginController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.dto.ServiceResult;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.service.UserService;
import com.serinity.accesscontrol.util.FXMLAnimationUtil;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
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
public final class LoginController implements StackNavigable, StatusMessageProvider {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="forgetPasswordHyperlink"
  private Hyperlink forgetPasswordHyperlink; // Value injected by FXMLLoader

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

  @FXML // fx:id="signUpUserRoleComboBox"
  private ComboBox<UserRole> signUpUserRoleComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="singnUpEmailTextField"
  private TextField singnUpEmailTextField; // Value injected by FXMLLoader

  @FXML // fx:id="usernameOrEmail"
  private TextField usernameOrEmail; // Value injected by FXMLLoader

  private StackPane stackHost; // Will be injected by RootController

  private StatusMessageProvider statusProvider; // Delegate to RootController

  // ############################
  // ### OVERRIDE FUNCTION(S) ###
  // ############################

  @Override
  public StackPane getStackHost() {
    return stackHost;
  }

  @Override
  public void setStackHost(final StackPane host) {
    this.stackHost = host;
  }

  @Override
  public void setStatusProvider(final StatusMessageProvider provider) {
    this.statusProvider = provider;
  }

  @Override
  public void showStatusMessage(final String message, final MessageStatus status) {
    if (statusProvider != null) {
      statusProvider.showStatusMessage(message, status);
    }
  }

  // ########################
  // ### SLOT FUNCTION(S) ###
  // ########################

  @FXML
  void onFaceIdAction(final ActionEvent event) {
    final FXMLLoaderUtil.ViewLoader<CameraDesktopController> loader = FXMLLoaderUtil.loadView(
        getClass(),
        ResourceFile.CAMERA_DESKTOP_FXML.getFileName(),
        I18nUtil.getBundle());

    final CameraDesktopController cameraController = loader.getController();
    cameraController.setRecognizeMode(user -> {
      UserService.signInWithFace(user);
      push(user.getRole().equals(UserRole.ADMIN)
          ? ResourceFile.ADMIN_DASHBOARD_FXML.getFileName()
          : ResourceFile.USER_HOME_FXML.getFileName(),
          controller -> injectUserIntoDashboard(controller, user));
    });

    final Stage stage = new Stage();
    stage.setTitle(I18nUtil.getValue("camera.stage.title.face_id_verification"));
    stage.setScene(new Scene(loader.getRoot()));
    stage.setResizable(false);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.show();
  }

  @FXML
  void onForgetPasswordHyperlinkAction(final ActionEvent event) {
    push(ResourceFile.RESET_PASSWORD_FXML.getFileName());
  }

  @FXML
  void onSignInButtonAction(final ActionEvent event) {
    final ServiceResult<User> userServiceResult = UserService.signIn(
        usernameOrEmail.getText(),
        password.getText());

    if (userServiceResult.isSuccess()) {
      showStatusMessage(userServiceResult.getMessage(), MessageStatus.SUCCESS);
      final User user = userServiceResult.getData();
      push(user.getRole().equals(UserRole.ADMIN)
          ? ResourceFile.ADMIN_DASHBOARD_FXML.getFileName()
          : ResourceFile.USER_HOME_FXML.getFileName(),
          controller -> injectUserIntoDashboard(controller, user));

    } else {
      showStatusMessage(userServiceResult.getMessage(), MessageStatus.WARNING);
    }
  }

  @FXML
  void onSignUpButtonAction(final ActionEvent event) {
    final ServiceResult<User> userServiceResult = UserService.signUp(
        singnUpEmailTextField.getText(),
        signUpPasswordField.getText(),
        signUpConfirmPasswordField.getText(),
        signUpUserRoleComboBox.getValue());

    if (userServiceResult.isSuccess()) {
      showStatusMessage(userServiceResult.getMessage(), MessageStatus.SUCCESS);
      final User user = userServiceResult.getData();
      push(user.getRole().equals(UserRole.ADMIN)
          ? ResourceFile.ADMIN_DASHBOARD_FXML.getFileName()
          : ResourceFile.USER_HOME_FXML.getFileName(),
          controller -> injectUserIntoDashboard(controller, user));

    } else {
      showStatusMessage(userServiceResult.getMessage(), MessageStatus.WARNING);
    }
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
  void onSignUpHyperlinkAction(final ActionEvent event) {
    FXMLAnimationUtil.slideFullScreen(
        loginSideWebView,
        loginInterface
            .getScene()
            .getWindow()
            .getWidth(),
        true);
  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert forgetPasswordHyperlink != null
        : "fx:id=\"forgetPasswordHyperlink\" was not injected: check your FXML file 'login.fxml'.";
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
    assert signUpUserRoleComboBox != null
        : "fx:id=\"signUpUserRoleComboBox\" was not injected: check your FXML file 'login.fxml'.";
    assert singnUpEmailTextField != null
        : "fx:id=\"singnUpEmailTextField\" was not injected: check your FXML file 'login.fxml'.";
    assert usernameOrEmail != null : "fx:id=\"usernameOrEmail\" was not injected: check your FXML file 'login.fxml'.";

    // Custom initialization
    signUpUserRoleComboBoxInit();
    loginSideWebViewInit();
  }

  private void injectUserIntoDashboard(final Object controller, final User user) {
    if (controller instanceof final AdminDashboardController admin) {
      admin.setUser(user);
      admin.setStatusProvider(this);
    }

    if (controller instanceof final UserHomeController home) {
      home.setUser(user);
      home.setStatusProvider(this);
    }
  }

  private void signUpUserRoleComboBoxInit() {
    signUpUserRoleComboBox.getItems().addAll(
        UserRole.PATIENT,
        UserRole.THERAPIST);
    signUpUserRoleComboBox.setValue(UserRole.PATIENT);
  }

  private void loginSideWebViewInit() {
    final URL url = getClass().getResource(ResourceFile.LOGIN_SIDE_HTML.getFileName());

    if (url == null) {
      throw new IllegalStateException("Resource not found: " + ResourceFile.LOGIN_SIDE_HTML.getFileName());
    }

    loginSideWebView.getEngine().load(url.toExternalForm());
  }
} // LoginController final class
