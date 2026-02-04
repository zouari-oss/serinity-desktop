/**
 * SignInController.java
 *
 * `sign-in.fxml` controller class
 *
 * <p>none</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-02
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/SignInController.java" 
 * target="_blank">
 * SignInController.java
 * </a>
 */

// `SignInController` package name
package com.serinity.accesscontrol.controller;

// `java` import(s)
import java.net.URL;
import java.util.ResourceBundle;

// `javafx` import(s)
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

public class SignInController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="go_btn"
  private Button go_btn; // Value injected by FXMLLoader

  @FXML
  void onGoDashboard(KeyEvent event) {
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert go_btn != null : "fx:id=\"go_btn\" was not injected: check your FXML file 'sign-in.fxml'.";
  }
} // SignInController class
