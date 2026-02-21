// `RootController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;
import com.serinity.accesscontrol.flag.SupportedLanguage;
import com.serinity.accesscontrol.util.FXMLLoaderUtil;
import com.serinity.accesscontrol.util.I18nUtil;

// `javafx` import(s)
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/RootPasswordController.java">
 *        RootPasswordController.java
 *        </a>
 */
public final class RootController {
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

  @FXML // fx:id="rootStackPane"
  private StackPane rootStackPane; // Value injected by FXMLLoader

  // ##########################
  // ### HELPER FUNCTION(S) ###
  // ##########################

  public <T> void push(final String fxml) {
    FXMLLoaderUtil.ViewLoader<T> view = FXMLLoaderUtil.loadView(
        getClass(),
        fxml,
        I18nUtil.getBundle());

    T controller = view.getController();

    if (controller instanceof com.serinity.accesscontrol.controller.base.BaseController baseController) {
      baseController.setRootController(this);
    }

    rootStackPane.getChildren().add(view.getRoot());
  }

  public void replace(final String fxml) {
    rootStackPane.getChildren().setAll(FXMLLoaderUtil.loadFXML(
        getClass(),
        fxml,
        I18nUtil.getBundle()));
  }

  public void pop() {
    rootStackPane.getChildren().removeLast();
  }

  // ##############################
  // ### SLOT HANDLER FUNCTIONS ###
  // ##############################

  @FXML
  void onLanguageComboBoxAction(final ActionEvent event) {
    I18nUtil.setLocale(
        languageComboBox.getValue().equals(SupportedLanguage.FR.getCode())
            ? SupportedLanguage.FR.getLocale() // To `fr`
            : SupportedLanguage.EN.getLocale()); // To `en`

    ((Stage) loginInterface.getScene().getWindow())
        .setScene(FXMLLoaderUtil.loadScene(
            getClass(),
            ResourceFile.ROOT_FXML.getFileName(),
            I18nUtil.getBundle()));
  }

  // ##################################
  // ### INITIALIZATION FUNCTION(S) ###
  // ##################################

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert footerLabel != null : "fx:id=\"footerLabel\" was not injected: check your FXML file 'root.fxml'.";
    assert languageComboBox != null : "fx:id=\"languageComboBox\" was not injected: check your FXML file 'root.fxml'.";
    assert loginInterface != null : "fx:id=\"loginInterface\" was not injected: check your FXML file 'root.fxml'.";
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

    // inject RootController into LoginController
    view.getController().setRootController(this);

    rootStackPane.getChildren().add(view.getRoot());
  }

  private void languageComboBoxInit() {
    languageComboBox.getItems()
        .addAll((ObservableList<String>) FXCollections.observableArrayList(
            I18nUtil.getSupportedLanguagesToString()));
    languageComboBox.setValue(I18nUtil.getLocale().getLanguage());
  }
} // RootController final class
