// `UserDashboardController` import(s)
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.model.User;

// `javafx` import(s)
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * `user-dashboard.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-23
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/UserDashboardController.java">
 *        UserDashboardController.java
 *        </a>
 */
public final class UserDashboardController implements StatusMessageProvider {

  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(UserDashboardController.class);

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="aboutMeTextArea"
  private TextArea aboutMeTextArea; // Value injected by FXMLLoader

  @FXML // fx:id="browseImageButton"
  private Button browseImageButton; // Value injected by FXMLLoader

  @FXML // fx:id="cancelButton"
  private Button cancelButton; // Value injected by FXMLLoader

  @FXML // fx:id="countryTextField"
  private TextField countryTextField; // Value injected by FXMLLoader

  @FXML // fx:id="firstNameTextField"
  private TextField firstNameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="genderComboBox"
  private ComboBox<?> genderComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="lastNameTextField"
  private TextField lastNameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="phoneTextField"
  private TextField phoneTextField; // Value injected by FXMLLoader

  @FXML // fx:id="profileImageView"
  private ImageView profileImageView; // Value injected by FXMLLoader

  @FXML // fx:id="saveButton"
  private Button saveButton; // Value injected by FXMLLoader

  @FXML // fx:id="stateTextField"
  private TextField stateTextField; // Value injected by FXMLLoader

  @FXML // fx:id="usernameTextField"
  private TextField usernameTextField; // Value injected by FXMLLoader

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private User user;

  // #############################
  // ### GETTER(S) & SETTER(S) ###
  // #############################

  public void setUser(final User user) {
    this.user = user;
  }

  public void setStatusProvider(final StatusMessageProvider provider) {
    this.statusProvider = provider;
  }

  // ############################
  // ### OVERRIDE FUNCTION(S) ###
  // ############################

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
  void onBrowseImage(final ActionEvent event) {

  }

  @FXML
  void onCancelButtonAction(final ActionEvent event) {

  }

  @FXML
  void onSaveButtonAction(final ActionEvent event) {

  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert aboutMeTextArea != null
        : "fx:id=\"aboutMeTextArea\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert browseImageButton != null
        : "fx:id=\"browseImageButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert cancelButton != null
        : "fx:id=\"cancelButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert countryTextField != null
        : "fx:id=\"countryTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert firstNameTextField != null
        : "fx:id=\"firstNameTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert genderComboBox != null
        : "fx:id=\"genderComboBox\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert lastNameTextField != null
        : "fx:id=\"lastNameTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert phoneTextField != null
        : "fx:id=\"phoneTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert profileImageView != null
        : "fx:id=\"profileImageView\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert stateTextField != null
        : "fx:id=\"stateTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert usernameTextField != null
        : "fx:id=\"usernameTextField\" was not injected: check your FXML file 'user-dashboard.fxml'.";

    // Custom initialization
    setStatusProvider(statusProvider);

    Platform.runLater(() -> { // NOTE: After controller initialized
      // TODO: Init user data (profile) & setup the edit profile side
      usernameTextField.setText(user.getEmail());
    });

    _LOGGER.info("User Dashboard Interface initialized successfully!");
  }
} // UserDashboardController final class
