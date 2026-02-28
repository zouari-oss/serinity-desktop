// `RootController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.controller.base.StackNavigable;
import com.serinity.accesscontrol.controller.base.StatusMessageProvider;
import com.serinity.accesscontrol.flag.MessageStatus;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * `root.fxml` controller class
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/RootController.java">
 *        RootController.java
 *        </a>
 */
public final class RootController implements StackNavigable, StatusMessageProvider {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="footerLabel"
  private Label footerLabel; // Value injected by FXMLLoader

  @FXML // fx:id="languageComboBox"
  private ComboBox<String> languageComboBox; // Value injected by FXMLLoader

  @FXML // fx:id="loginInterface"
  private AnchorPane loginInterface; // Value injected by FXMLLoader

  @FXML // fx:id="messageStatusImageView"
  private ImageView messageStatusImageView; // Value injected by FXMLLoader
  //
  @FXML // fx:id="messageStatusLabel"
  private Label messageStatusLabel; // Value injected by FXMLLoader

  @FXML // fx:id="rootStackPane"
  private StackPane rootStackPane; // Value injected by FXMLLoader

  @Override
  public StackPane getStackHost() {
    return rootStackPane;
  }

  @Override
  public void showStatusMessage(final String message, final MessageStatus status) {
    // Reset CSS
    messageStatusLabel.getStyleClass().removeAll("success", "error", "warning", "info");
    if (!messageStatusLabel.getStyleClass().contains("statusMsg")) {
      messageStatusLabel.getStyleClass().add("statusMsg");
    }
    messageStatusLabel.getStyleClass().add(status.getCssClass());

    // Set message text
    messageStatusLabel.setText(message);

    // Set icon
    if (messageStatusImageView != null && status.getIconPath() != null) {
      messageStatusImageView.setImage(new Image(getClass().getResourceAsStream(status.getIconPath())));
    }

    // Animate from top
    FXMLAnimationUtil.slideFromTop(
        messageStatusLabel,
        50,
        500,
        true,
        5);
  }

  @FXML
  void onLanguageComboBoxAction(final ActionEvent event) {
    I18nUtil.setLocale(
        languageComboBox.getValue().equals(SupportedLanguage.FR.getCode())
            ? SupportedLanguage.FR.getLocale() // To `fr`
            : SupportedLanguage.EN.getLocale()); // To `en`

    final Stage stage = (Stage) loginInterface.getScene().getWindow();
    stage.setScene(FXMLLoaderUtil.loadScene(
        getClass(),
        ResourceFile.ROOT_FXML.getFileName(),
        I18nUtil.getBundle()));
    stage.setTitle(I18nUtil.getValue("app.scene.title.sign_in"));
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert footerLabel != null : "fx:id=\"footerLabel\" was not injected: check your FXML file 'root.fxml'.";
    assert languageComboBox != null : "fx:id=\"languageComboBox\" was not injected: check your FXML file 'root.fxml'.";
    assert loginInterface != null : "fx:id=\"loginInterface\" was not injected: check your FXML file 'root.fxml'.";
    assert messageStatusLabel != null
        : "fx:id=\"messageStatusLabel\" was not injected: check your FXML file 'root.fxml'.";
    assert rootStackPane != null : "fx:id=\"rootStackPane\" was not injected: check your FXML file 'root.fxml'.";

    // Custom initialization
    languageComboBoxInit();
    rootStackPaneInit();
  }

  private void rootStackPaneInit() {
    final FXMLLoaderUtil.ViewLoader<LoginController> view = FXMLLoaderUtil.loadView(
        getClass(),
        ResourceFile.LOGIN_FXML.getFileName(),
        I18nUtil.getBundle());

    final LoginController loginController = view.getController();
    // inject RootController into LoginController
    loginController.setStackHost(rootStackPane);
    loginController.setStatusProvider(this);

    view.getRoot().setUserData(loginController);
    rootStackPane.getChildren().add(view.getRoot());
  }

  private void languageComboBoxInit() {
    languageComboBox.getItems()
        .addAll((ObservableList<String>) FXCollections.observableArrayList(
            I18nUtil.getSupportedLanguagesToString()));
    languageComboBox.setValue(I18nUtil.getLocale().getLanguage());
  }
} // RootController final class
