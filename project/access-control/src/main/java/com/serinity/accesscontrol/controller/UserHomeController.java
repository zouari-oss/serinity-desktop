// `UserHomeController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.model.User;

// `javafx` import(s)
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * `user-home.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/UserHomeController.java">
 *        UserHomeController.java
 *        </a>
 */
public final class UserHomeController implements StackNavigable, StatusMessageProvider {

  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(UserHomeController.class);

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="contentHostStackPane"
  private StackPane contentHostStackPane; // Value injected by FXMLLoader

  @FXML // fx:id="logoImg"
  private ImageView logoImg; // Value injected by FXMLLoader

  @FXML // fx:id="navBarAppointmentsButton"
  private Button navBarAppointmentsButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarDashboardButton"
  private Button navBarDashboardButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarExercisesButton"
  private Button navBarExercisesButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarMoodButton"
  private Button navBarMoodButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarSleepButton"
  private Button navBarSleepButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarSupportButton"
  private Button navBarSupportButton; // Value injected by FXMLLoader

  @FXML // fx:id="navBarUserProfileImageView"
  private ImageView navBarUserProfileImageView; // Value injected by FXMLLoader

  @FXML // fx:id="navBarUsernameLabel"
  private Label navBarUsernameLabel; // Value injected by FXMLLoader

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private User user;

  // #############################
  // ### GETTER(S) & SETTER(S) ###
  // #############################

  public void setUser(final User user) {
    this.user = user;
  }

  // ############################
  // ### OVERRIDE FUNCTION(S) ###
  // ############################

  @Override
  public StackPane getStackHost() {
    return contentHostStackPane;
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

  // ##############################
  // ### SLOT HANDLER FUNCTIONS ###
  // ##############################

  @FXML
  void onNavBarAppointmentsButtonAction(final ActionEvent event) {

  }

  @FXML
  void onNavBarDashboardButtonAction(final ActionEvent event) {

  }

  @FXML
  void onNavBarExercisesButtonAction(final ActionEvent event) {

  }

  @FXML
  void onNavBarMoodButtonAction(final ActionEvent event) {

  }

  @FXML
  void onNavBarSleepButtonAction(final ActionEvent event) {

  }

  @FXML
  void onNavBarSupportButtonAction(final ActionEvent event) {

  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert contentHostStackPane != null
        : "fx:id=\"contentHostStackPane\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert logoImg != null : "fx:id=\"logoImg\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarAppointmentsButton != null
        : "fx:id=\"navBarAppointmentsButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarDashboardButton != null
        : "fx:id=\"navBarDashboardButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarExercisesButton != null
        : "fx:id=\"navBarExercisesButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarMoodButton != null
        : "fx:id=\"navBarMoodButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarSleepButton != null
        : "fx:id=\"navBarSleepButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarSupportButton != null
        : "fx:id=\"navBarSupportButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarUserProfileImageView != null
        : "fx:id=\"navBarUserProfileImageView\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert navBarUsernameLabel != null
        : "fx:id=\"navBarUsernameLabel\" was not injected: check your FXML file 'user-dashboard.fxml'.";

    // Custom initialization
    navBarDashboardButton.getStyleClass().add("nav-btn-active");

    // NOTE: After controller initialized
    Platform.runLater(() -> { // INFO: Because @Gl3diator set the default stage as 1200x800
      final Stage stage = (Stage) navBarDashboardButton.getScene().getWindow();
      stage.setWidth(1200);
      stage.setHeight(800);
      stage.centerOnScreen();

      // By default open user dashboard
      push(ResourceFile.USER_DAHBOARD_FXML.getFileName(),
          controller -> {
            if (controller instanceof final UserDashboardController dash) {
              dash.setUser(user);
              dash.setStatusProvider(this);
            }
          });
    });

    setStackHost(contentHostStackPane);
    setStatusProvider(statusProvider);

    _LOGGER.info("User Dashboard Interface initialized successfully!");
  }
} // UserHomeController final class
