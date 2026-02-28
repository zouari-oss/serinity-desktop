// `UserDashboardController` import(s)
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.controller.base.StageTitled;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.Gender;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.AuditLogRepository;
import com.serinity.accesscontrol.repository.ProfileRepository;
import com.serinity.accesscontrol.repository.UserFaceRepository;
import com.serinity.accesscontrol.repository.UserRepository;
import com.serinity.accesscontrol.model.UserFace;
import com.serinity.accesscontrol.service.FreeImageHostClient;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;
import com.serinity.accesscontrol.util.RegexValidator;

// `javafx` import(s)
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
public final class UserDashboardController implements StatusMessageProvider, StageTitled {

  @Override
  public String getSceneTitleKey() {
    return "app.scene.title.dashboard";
  }


  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(UserDashboardController.class);

  private static final VBox createActivityCard(final AuditLog log) {
    final VBox card = new VBox();
    card.getStyleClass().add("activity-card");

    final Label action = new Label(log.getAction());
    action.getStyleClass().add("activity-action");

    final Label os = new Label(log.getOsName());
    os.getStyleClass().add("activity-os");

    final HBox topRow = new HBox(15, action, os);

    final Label hostname = new Label(I18nUtil.getValue("activity.hostname") + log.getHostname());
    hostname.getStyleClass().add("activity-meta");

    final Label ip = new Label(I18nUtil.getValue("activity.private_ip") + log.getPrivateIpAddress());
    ip.getStyleClass().add("activity-meta");

    final Label mac = new Label(I18nUtil.getValue("activity.mac") + log.getMacAddress());
    mac.getStyleClass().add("activity-meta");

    final Label location = new Label(I18nUtil.getValue("activity.location") + log.getLocation());
    location.getStyleClass().add("activity-meta");

    final Label date = new Label(log.getCreatedAt().toString());
    date.getStyleClass().add("activity-date");

    final HBox bottomRow = new HBox(date);
    bottomRow.setAlignment(Pos.CENTER_RIGHT);

    card.getChildren().addAll(topRow, hostname, ip, mac, location, bottomRow);

    return card;
  }

  private static String nullSafe(final String s) {
    return s != null ? s : "";
  }

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

  @FXML // fx:id="activityCardsContainerVBox"
  private VBox activityCardsContainerVBox; // Value injected by FXMLLoader

  @FXML // fx:id="firstNameTextField"
  private TextField firstNameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="genderComboBox"
  private ComboBox<Gender> genderComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="lastNameTextField"
  private TextField lastNameTextField; // Value injected by FXMLLoader

  @FXML // fx:id="phoneTextField"
  private TextField phoneTextField; // Value injected by FXMLLoader
  @FXML // fx:id="profileCompletionBar"
  private ProgressBar profileCompletionBar; // Value injected by FXMLLoader

  @FXML // fx:id="profileCompletionLabel"
  private Label profileCompletionLabel; // Value injected by FXMLLoader

  //
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

  @FXML // fx:id="faceRecognitionToggleButton"
  private ToggleButton faceRecognitionToggleButton; // Value injected by FXMLLoader

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private User user;

  private Profile userProfile;

  public void setUser(final User user) {
    this.user = user;
  }

  public void setStatusProvider(final StatusMessageProvider provider) {
    this.statusProvider = provider;
  }

  @Override
  public void showStatusMessage(final String message, final MessageStatus status) {
    if (statusProvider != null) {
      statusProvider.showStatusMessage(message, status);
    }
  }

  public void loadActivityCards() {
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final List<AuditLog> allLogs = new AuditLogRepository(em).findAllByUserId(user.getId());

    // Sort by `created_at` column
    allLogs.sort(Comparator.comparing(AuditLog::getCreatedAt).reversed());

    activityCardsContainerVBox.getChildren().clear();

    allLogs.forEach(log -> activityCardsContainerVBox.getChildren().add(createActivityCard(log)));
  }

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
  void onFaceRecognitionTogglrButtonAction(final ActionEvent event) {
    final ToggleButton toggle = (ToggleButton) event.getSource();
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final UserFaceRepository userFaceRepository = new UserFaceRepository(em);
    final UserRepository userRepository = new UserRepository(em);

    if (toggle.isSelected()) {
      // Open camera popup in enroll mode
      final FXMLLoaderUtil.ViewLoader<CameraDesktopController> view = FXMLLoaderUtil.loadView(
          getClass(),
          ResourceFile.CAMERA_DESKTOP_FXML.getFileName(),
          I18nUtil.getBundle());

      view.getController().setEnrollMode(user, () -> {
        user.setFaceRecognitionEnabled(true);
        userRepository.update(user);
        toggle.setSelected(true);
        toggle.setText(I18nUtil.getValue("user.dashboard.face_id.disable"));
        showStatusMessage(I18nUtil.getValue("status.face_id.registered"), MessageStatus.SUCCESS);
      });

      final Stage stage = new Stage();
      stage.setTitle(I18nUtil.getValue("camera.stage.title.enrollment"));
      stage.setScene(new Scene(view.getRoot()));
      stage.setResizable(false);
      stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

      // If user closes without enrolling, revert toggle
      stage.setOnHidden(e -> {
        if (!user.isFaceRecognitionEnabled()) {
          toggle.setSelected(false);
          toggle.setText(I18nUtil.getValue("user.dashboard.face_id.enable"));
        }
      });

      stage.show();

    } else {
      // Remove stored face and disable flag
      final UserFace existing = userFaceRepository.findByUserId(user.getId());
      if (existing != null) {
        userFaceRepository.delete(existing);
      }
      user.setFaceRecognitionEnabled(false);
      userRepository.update(user);
      toggle.setText(I18nUtil.getValue("user.dashboard.face_id.enable"));
      showStatusMessage(I18nUtil.getValue("status.face_id.disabled"), MessageStatus.INFO);
    }
  }

  @FXML
  void onCancelButtonAction(final ActionEvent event) {
    initUserInfoLater();
    showStatusMessage(I18nUtil.getValue("status.profile.reset"), MessageStatus.INFO);
  }

  @FXML
  void onSaveButtonAction(final ActionEvent event) {
    final String userProfileUsername = usernameTextField.getText();
    if (userProfileUsername.isBlank()) {
      showStatusMessage(I18nUtil.getValue("status.profile.username_blank"), MessageStatus.WARNING);
      return;
    }

    final String phoneNumber = phoneTextField.getText();
    if (phoneNumber != null && !RegexValidator.isValidPhoneNumber(phoneNumber)) {
      showStatusMessage(I18nUtil.getValue("status.profile.invalid_phone"), MessageStatus.WARNING);
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

    // update profile completion
    updateProfileCompletion();

    showStatusMessage(I18nUtil.getValue("status.profile.updated"), MessageStatus.SUCCESS);
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert aboutMeTextArea != null
        : "fx:id=\"aboutMeTextArea\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert activityCardsContainerVBox != null
        : "fx:id=\"activityCardsContainerVBox\" was not injected: check your FXML file 'user-dashboard.fxml'.";
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
    assert profileCompletionBar != null
        : "fx:id=\"profileCompletionBar\" was not injected: check your FXML file 'user-dashboard.fxml'.";
    assert profileCompletionLabel != null
        : "fx:id=\"profileCompletionLabel\" was not injected: check your FXML file 'user-dashboard.fxml'.";
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
    initGenderComboBox();

    Platform.runLater(() -> { // NOTE: After controller initialized
      initUserInfoLater();
      updateProfileCompletion();
      welcomeLabel.setText(welcomeLabel.getText() + userProfile.getUsername());
      loadActivityCards();
      // Sync toggle state with user's face recognition setting
      faceRecognitionToggleButton.setSelected(user.isFaceRecognitionEnabled());
      faceRecognitionToggleButton.setText(user.isFaceRecognitionEnabled()
          ? I18nUtil.getValue("user.dashboard.face_id.disable")
          : I18nUtil.getValue("user.dashboard.face_id.enable"));
    });

    _LOGGER.info("User Dashboard Interface initialized successfully!");
  }

  private void updateProfileCompletion() {
    final int totalFields = 8;
    int filledFields = 0;

    if (!nullSafe(firstNameTextField.getText()).isEmpty())
      filledFields++;
    if (!nullSafe(lastNameTextField.getText()).isEmpty())
      filledFields++;
    if (!nullSafe(usernameTextField.getText()).isEmpty())
      filledFields++;
    if (!nullSafe(phoneTextField.getText()).isEmpty())
      filledFields++;
    if (genderComboBox.getValue() != null)
      filledFields++;
    if (!nullSafe(countryTextField.getText()).isEmpty())
      filledFields++;
    if (!nullSafe(stateTextField.getText()).isEmpty())
      filledFields++;
    if (!nullSafe(aboutMeTextArea.getText()).isEmpty())
      filledFields++;

    final double percentage = (double) filledFields / totalFields;

    profileCompletionBar.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high");

    if (percentage < 0.4) {
      profileCompletionBar.getStyleClass().add("progress-low");
    } else if (percentage < 0.8) {
      profileCompletionBar.getStyleClass().add("progress-medium");
    } else {
      profileCompletionBar.getStyleClass().add("progress-high");
    }

    profileCompletionBar.setProgress(percentage);
    profileCompletionLabel.setText((int) (percentage * 100) + "%");
  }

  private String handleProfileImageUpload() {

    final Image image = profileImageView.getImage();
    if (image == null
        || image.getUrl() == null
        || image.getUrl().equals(ResourceFile.USER_DEFAUL_PROFILE_PNG.getFileName())) {
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
      _LOGGER.error("Image upload failed.", e);
      showStatusMessage(I18nUtil.getValue("status.image.upload_failed"), MessageStatus.ERROR);
    }

    return null;
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
            ? new Image(ResourceFile.USER_DEFAUL_PROFILE_PNG.getFileName())
            : new Image(userProfile.getProfileImageUrl(), true));
  }

  private void initGenderComboBox() {
    genderComboBox.getItems().addAll(
        Gender.MALE,
        Gender.FEMALE);
  }
} // UserDashboardController final class
