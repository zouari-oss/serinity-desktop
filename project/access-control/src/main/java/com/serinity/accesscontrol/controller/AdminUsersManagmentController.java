// `serinity` package name
package com.serinity.accesscontrol.controller;

import javafx.event.ActionEvent;
// `javafx` import(s)
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

// `java` import(s)
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.AccountStatus;
import com.serinity.accesscontrol.flag.MessageStatus;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.UserRepository;

/**
 * `admin-user-dashboard.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 22/02/2026
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/AdminUsersManagmentController.java">
 *        AdminUsersManagmentController.java
 *        </a>
 */
public final class AdminUsersManagmentController implements StackNavigable, StatusMessageProvider {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="roleFilterComboBox"
  private ComboBox<String> roleFilterComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="searchTextField"
  private TextField searchTextField; // Value injected by FXMLLoader

  @FXML // fx:id="usersContainer"
  private VBox usersContainer; // Value injected by FXMLLoader

  @FXML // fx:id="usersPage"
  private AnchorPane usersPage; // Value injected by FXMLLoader

  private StackPane stackHost; // Will be injected by RootController

  private StatusMessageProvider statusProvider; // Delegate to RootController

  private final List<User> allUsers = new ArrayList<>();

  // #############################
  // ### GETTER(S) & SETTER(S) ###
  // #############################

  public void setStatusProvider(final StatusMessageProvider provider) {
    this.statusProvider = provider;
  }

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
  public void showStatusMessage(final String message, final MessageStatus status) {
    if (statusProvider != null) {
      statusProvider.showStatusMessage(message, status);
    }
  }

  // ################################
  // ### SLOT HANDLER FUNCTION(S) ###
  // ################################

  /**
   * Triggered whenever the role filter changes.
   */
  @FXML
  void onRoleFilterComboBoxAction(final ActionEvent event) {
    filterUsers(searchTextField.getText(), roleFilterComboBox.getValue());
  }

  /**
   * Triggered whenever the search field changes.
   */
  @FXML
  void onSearchTextFieldAction(final ActionEvent event) {
    filterUsers(searchTextField.getText(), roleFilterComboBox.getValue());
  }

  // ##########################
  // ### HELPER FUNCTION(S) ###
  // ##########################

  /**
   * Filters users by keyword and role.
   */
  private void filterUsers(final String keyword, final String selectedRole) {
    final String lowerKeyword = keyword.toLowerCase();

    final List<User> filtered = allUsers.stream()
        .filter(u -> u.getEmail().toLowerCase().contains(lowerKeyword))
        .filter(u -> "ALL".equals(selectedRole) || u.getRole().toString().equalsIgnoreCase(selectedRole))
        .collect(Collectors.toList());

    loadUsers(filtered);
  }

  /**
   * Adds user cards to the container.
   */
  private void loadUsers(final List<User> users) {
    usersContainer.getChildren().clear();

    for (final User user : users) {
      final VBox card = createUserCard(user);
      usersContainer.getChildren().add(card);
    }
  }

  /**
   * Create a styled user card.
   */
  private VBox createUserCard(final User user) {
    final VBox card = new VBox(8);
    card.getStyleClass().add("user-card");

    final Label emailLabel = new Label(user.getEmail());
    emailLabel.getStyleClass().add("user-email");

    final Label roleLabel = new Label("Role: " + user.getRole().toString());
    roleLabel.getStyleClass().add("user-role");

    final Label statusLabel = new Label(user.getAccountStatus().toString());
    statusLabel.getStyleClass().add("user-status");

    // ================= STATUS COLOR =================
    switch (user.getAccountStatus()) {
      case ACTIVE -> statusLabel.getStyleClass().add("status-active");
      case DISABLED -> statusLabel.getStyleClass().add("status-disabled");
    }

    // ================= ACTION BUTTONS =================
    final Button deleteButton = new Button("Delete");
    deleteButton.getStyleClass().add("danger-btn");

    final Button toggleStatusButton = new Button(
        "ACTIVE".equalsIgnoreCase(user.getAccountStatus().toString())
            ? "Disable"
            : "Enable");
    toggleStatusButton.getStyleClass().add("secondary-btn");

    // Delete action
    deleteButton.setOnAction(event -> {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
      final UserRepository repo = new UserRepository(em);
      repo.delete(user);

      allUsers.remove(user);
      showStatusMessage("User " + user.getEmail() + " has been deleted successfully!", MessageStatus.INFO);
      loadUsers(allUsers); // refresh UI
    });

    // Toggle status action
    toggleStatusButton.setOnAction(event -> {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
      final UserRepository repo = new UserRepository(em);

      user.setAccountStatus(
          "ACTIVE".equalsIgnoreCase(user.getAccountStatus().toString())
              ? AccountStatus.DISABLED
              : AccountStatus.ACTIVE);

      repo.update(user);
      showStatusMessage("User " + user.getEmail() + " has been deleted successfully!", MessageStatus.INFO);
      loadUsers(allUsers); // refresh UI
    });

    // Buttons container
    final HBox actionsBox = new HBox(10, toggleStatusButton, deleteButton);
    actionsBox.setStyle("-fx-padding: 5 0 0 0;");

    card.getChildren().addAll(emailLabel, roleLabel, statusLabel, actionsBox);

    return card;
  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert roleFilterComboBox != null
        : "fx:id=\"roleFilterComboBox\" was not injected: check your FXML file 'admin-users-management.fxml'.";
    assert searchTextField != null
        : "fx:id=\"searchTextField\" was not injected: check your FXML file 'admin-users-management.fxml'.";
    assert usersContainer != null
        : "fx:id=\"usersContainer\" was not injected: check your FXML file 'admin-users-management.fxml'.";
    assert usersPage != null
        : "fx:id=\"usersPage\" was not injected: check your FXML file 'admin-users-management.fxml'.";

    // Custom initialization
    setStackHost(stackHost);
    setStatusProvider(statusProvider);
    usersManagmentInit();
  }

  public void usersManagmentInit() {
    // Initialize filter dropdown
    roleFilterComboBox.getItems().addAll(
        "ALL",
        UserRole.ADMIN.toString(),
        UserRole.PATIENT.toString(),
        UserRole.THERAPIST.toString());
    roleFilterComboBox.setValue("ALL");

    // Add sample users
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final UserRepository userRepository = new UserRepository(em);
    allUsers.addAll(userRepository.findAll());

    // Load all users initially
    loadUsers(allUsers);
  }
} // AdminUsersManagmentController final class
