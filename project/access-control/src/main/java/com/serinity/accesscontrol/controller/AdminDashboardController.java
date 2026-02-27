// `serinity` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StageTitled;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.model.User;

// `javafx` import(s)
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * `admin-dashboard.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 22/02/2026
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/AdminDashboardController.java">
 *        AdminDashboardController.java
 *        </a>
 */
public final class AdminDashboardController implements StackNavigable, StatusMessageProvider, StageTitled {

  @Override
  public String getSceneTitleKey() {
    return "app.scene.title.admin_dashboard";
  }


  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="contentAreaStackPane"
  private StackPane contentAreaStackPane; // Value injected by FXMLLoader

  @FXML // fx:id="logoutButton"
  private Button logoutButton; // Value injected by FXMLLoader

  @FXML // fx:id="overviewButton"
  private Button overviewButton; // Value injected by FXMLLoader

  @FXML // fx:id="pageTitleLabel"
  private Label pageTitleLabel; // Value injected by FXMLLoader

  @FXML // fx:id="profileAndSettingsButton"
  private Button profileAndSettingsButton; // Value injected by FXMLLoader

  @FXML // fx:id="usersManagmentButton"
  private Button usersManagmentButton; // Value injected by FXMLLoader

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private User user;

  public void setUser(final User user) {
    this.user = user;
  }

  @Override
  public StackPane getStackHost() {
    return contentAreaStackPane;
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

  @FXML
  void onLogoutButtonAction(final ActionEvent event) {

  }

  @FXML
  void onOverviewButtonAction(final ActionEvent event) {

  }

  @FXML
  void onProfileAndSettingsButtonAction(final ActionEvent event) {
  }

  @FXML
  void onUsersManagmentButtonAction(final ActionEvent event) {
    push(ResourceFile.ADMIN_USERS_MANAGMENT_FXML.getFileName());
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert contentAreaStackPane != null
        : "fx:id=\"contentAreaStackPane\" was not injected: check your FXML file 'admin-dashboard.fxml'.";
    assert logoutButton != null
        : "fx:id=\"logoutButton\" was not injected: check your FXML file 'admin-dashboard.fxml'.";
    assert overviewButton != null
        : "fx:id=\"overviewButton\" was not injected: check your FXML file 'admin-dashboard.fxml'.";
    assert pageTitleLabel != null
        : "fx:id=\"pageTitleLabel\" was not injected: check your FXML file 'admin-dashboard.fxml'.";
    assert profileAndSettingsButton != null
        : "fx:id=\"profileAndSettingsButton\" was not injected: check your FXML file 'admin-dashboard.fxml'.";
    assert usersManagmentButton != null
        : "fx:id=\"usersManagmentButton\" was not injected: check your FXML file 'admin-dashboard.fxml'.";

    // Custom initialization
    setStackHost(contentAreaStackPane);
    setStatusProvider(statusProvider);
  }
} // AdminDashboardController final class
