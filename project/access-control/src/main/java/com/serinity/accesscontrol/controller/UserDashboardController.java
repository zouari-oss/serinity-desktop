// `UserDashboardController` import(s)
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.Gender;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;
import com.serinity.accesscontrol.service.FreeImageHostClient;

// `javafx` import(s)
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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
  private ComboBox<Gender> genderComboBox; // Value injected by FXMLLoader

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

  @FXML // fx:id="welcomeLabel"
  private Label welcomeLabel; // Value injected by FXMLLoader

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private User user;

  private Profile userProfile;

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

  // ##########################
  // ### HELPER FUNCTION(S) ###
  // ##########################

  private String handleProfileImageUpload() {

    final Image image = profileImageView.getImage();
    if (image == null || image.getUrl() == null) {
      return null;
    }

    final String imageUrl = image.getUrl();

    try {
      // Case 1: Local file, so upload required
      if (imageUrl.startsWith("file:")) {
        final File file = new File(new URI(imageUrl));
        final String uploadedUrl = FreeImageHostClient.uploadImage(file);
        if (uploadedUrl != null && !uploadedUrl.isBlank()) {
          profileImageView.setImage(new Image(uploadedUrl));
          return uploadedUrl;
        }
      }

      // Case 2: Already uploaded (http/https)
      if (imageUrl.startsWith("http")) {
        return imageUrl;
      }

    } catch (final Exception e) {
      e.printStackTrace();
      showStatusMessage("Image upload failed.", MessageStatus.ERROR);
    }

    return null;
  }

  // ##############################
  // ### SLOT HANDLER FUNCTIONS ###
  // ##############################

  @FXML
  void onBrowseImage(final ActionEvent event) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select a Profile Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

    final File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      profileImageView.setImage(new Image(file.toURI().toString()));
    }
  }

  @FXML
  void onCancelButtonAction(final ActionEvent event) {
    initUserInfoLater();
    showStatusMessage("User information has been reset successfully!", MessageStatus.INFO);
  }

  @FXML
  void onSaveButtonAction(final ActionEvent event) {
    final String userProfileUsername = usernameTextField.getText();
    if (userProfileUsername.isBlank()) {
      showStatusMessage("User information has been reset successfully!", MessageStatus.WARNING);
      return;
    }

    userProfile.setFirstName(firstNameTextField.getText());
    userProfile.setLastName(lastNameTextField.getText());
    userProfile.setUsername(userProfileUsername);
    userProfile.setPhone(phoneTextField.getText());
    userProfile.setGender(genderComboBox.getValue());
    userProfile.setCountry(countryTextField.getText());
    userProfile.setState(stateTextField.getText());
    userProfile.setAboutMe(aboutMeTextArea.getText());

    final String finalImageUrl = handleProfileImageUpload();
    if (finalImageUrl != null) {
      userProfile.setProfileImageUrl(finalImageUrl);
    }

    // Persist
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final ProfileRepository profileRepository = new ProfileRepository(em);
    profileRepository.update(userProfile);

    showStatusMessage("Profile updated successfully.", MessageStatus.SUCCESS);
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
    assert welcomeLabel != null
        : "fx:id=\"welcomeLabel\" was not injected: check your FXML file 'user-dashboard.fxml'.";

    // Custom initialization
    setStatusProvider(statusProvider);
    initGenderComboBox();

    Platform.runLater(() -> { // NOTE: After controller initialized
      initUserInfoLater();
      welcomeLabel.setText(welcomeLabel.getText() + userProfile.getUsername());
    });

    _LOGGER.info("User Dashboard Interface initialized successfully!");
  }

  /*
   * Init user data (profile) & setup the edit profile side
   */
  private void initUserInfoLater() {
    // Retrive user data
    if (userProfile == null) {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
      final ProfileRepository profileRepository = new ProfileRepository(em);
      userProfile = profileRepository.findByUserId(user.getId());
    }

    // Set user info
    firstNameTextField.setText(userProfile.getFirstName());
    lastNameTextField.setText(userProfile.getLastName());
    usernameTextField.setText(userProfile.getUsername());
    phoneTextField.setText(userProfile.getPhone());
    genderComboBox.setValue(userProfile.getGender());
    countryTextField.setText(userProfile.getCountry());
    stateTextField.setText(userProfile.getState());
    aboutMeTextArea.setText(userProfile.getAboutMe());
    profileImageView.setImage(
        userProfile.getProfileImageUrl() == null || userProfile.getProfileImageUrl().isEmpty()
            ? new Image("/assets/user-dashboard/user-default-profile.png")
            : new Image(userProfile.getProfileImageUrl(), true));
  }

  private void initGenderComboBox() {
    genderComboBox.getItems().addAll(
        Gender.MALE,
        Gender.FEMALE);
  }

} // UserDashboardController final class
