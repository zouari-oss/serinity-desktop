package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.HardcodedUser.FakeUser;
import com.serinity.forumcontrol.Models.Reply;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Services.ServiceReply;
import com.serinity.forumcontrol.Services.ServiceThread;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.awt.*;
import java.util.Optional;

public class ReplyCardController {
    String currentUserId = FakeUser.getCurrentUserId();
    @FXML private Label authorLabel;
    @FXML private Label contentLabel;
    @FXML private Label dateLabel;
    @FXML private VBox childrenBox;
    @FXML private VBox cardContainer;
    @FXML
    private MenuButton menuButton;


    private Reply reply;
    private ServiceReply service= new ServiceReply();
    private ServiceThread servicethread= new ServiceThread();
    private ThreadDetailController parentController;

    private boolean isEditMode = false;
    private TextArea editTextArea;
    private HBox editButtonsBox;

    public void setData(Reply reply,ThreadDetailController parentController) {

        this.reply = reply;
        String author=service.getReplyAuthor(reply.getUserId());
        this.parentController = parentController;

        authorLabel.setText("U/" + author );
        contentLabel.setText(reply.getContent());
        dateLabel.setText(String.valueOf(reply.getCreatedAt()));
        configureMenu();
    }

    public VBox getChildrenBox() {
        return childrenBox;
    }

    @FXML
    private void onReplyClick() {
        parentController.prepareReplyTo(reply.getId());
    }
    private void configureMenu() {
        try {
            boolean isAdmin = servicethread.isAdmin(currentUserId);
            boolean isOwner = reply.getUserId().equalsIgnoreCase(currentUserId);
            boolean ispostOwner=servicethread.isOwner(parentController.getThread().getId(), currentUserId);

            if (isOwner || isAdmin || ispostOwner) {
                menuButton.setVisible(true);
                menuButton.setManaged(true);
                menuButton.getItems().clear();

                if (isOwner) {
                    buildOwnerMenu();
                } else if ((isAdmin || ispostOwner) && !isOwner) {
                    buildAdminNotOwnerMenu();
            } else {
                menuButton.setVisible(false);
                menuButton.setManaged(false);
            }
            }

        } catch (Exception e) {
            System.err.println("Error configuring menu: " + e.getMessage());
            e.printStackTrace();
            menuButton.setVisible(false);
            menuButton.setManaged(false);
        }
    }
    private void buildOwnerMenu() {
        MenuItem editItem = new MenuItem("✏️ Edit");
        editItem.setOnAction(this::handleEdit);

        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        menuButton.getItems().addAll(editItem, deleteItem);
    }

    private void buildAdminNotOwnerMenu() {
        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        menuButton.getItems().addAll(deleteItem);
    }
    private void handleEdit(ActionEvent event) {
        if (isEditMode) {
            return;
        }

        try {
            System.out.println("Editing reply ID: " + reply.getId());
            enterEditMode();

        } catch (Exception e) {
            System.err.println("Error entering edit mode: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to enter edit mode: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void enterEditMode() {
        isEditMode = true;

        contentLabel.setVisible(false);
        contentLabel.setManaged(false);

        editTextArea = new TextArea(reply.getContent());
        editTextArea.setWrapText(true);
        editTextArea.setPrefRowCount(3);
        editTextArea.setStyle("-fx-font-size: 14; -fx-border-color: #4CAF50; -fx-border-width: 2;");
        VBox.setMargin(editTextArea,
                new Insets(5, 0, 5, 0));

        editButtonsBox = new HBox(10);
        editButtonsBox.setPadding(new Insets(5, 0, 5, 0));

        Button saveButton = new Button("💾 Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        saveButton.setOnAction(e -> saveEdit());

        Button cancelButton = new Button("❌ Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> cancelEdit());

        editButtonsBox.getChildren().addAll(saveButton, cancelButton);

        if (cardContainer != null) {
            int contentIndex = cardContainer.getChildren().indexOf(contentLabel);
            if (contentIndex >= 0) {
                cardContainer.getChildren().add(contentIndex + 1, editTextArea);
                cardContainer.getChildren().add(contentIndex + 2, editButtonsBox);
            } else {
                cardContainer.getChildren().addAll(editTextArea, editButtonsBox);
            }
        }

        editTextArea.requestFocus();
        editTextArea.selectAll();
    }
    private void saveEdit() {
        String newContent = editTextArea.getText().trim();

        if (newContent.isEmpty()) {
            showAlert("Validation Error", "Reply content cannot be empty!", Alert.AlertType.WARNING);
            return;
        }

        if (newContent.equals(reply.getContent())) {
            exitEditMode();
            return;
        }

        try {
            System.out.println("Saving reply ID: " + reply.getId());
            System.out.println("New content: " + newContent);

            service.updateContent(reply.getId(), newContent);

            reply.setContent(newContent);

            contentLabel.setText(newContent);

            exitEditMode();

            showAlert("Success", "Reply updated successfully! ✅", Alert.AlertType.INFORMATION);

            if (parentController != null) {
                parentController.refreshReplies();
            }

        } catch (Exception e) {
            System.err.println("Error saving reply: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error",
                    "Failed to save reply.\n\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void cancelEdit() {
        String currentText = editTextArea.getText().trim();
        if (!currentText.equals(reply.getContent())) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Discard Changes");
            confirmDialog.setHeaderText("Are you sure you want to discard your changes?");
            confirmDialog.setContentText("Any unsaved changes will be lost.");

            ButtonType discardButton = new ButtonType("Discard", ButtonBar.ButtonData.OK_DONE);
            ButtonType continueButton = new ButtonType("Continue Editing", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmDialog.getButtonTypes().setAll(discardButton, continueButton);

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isEmpty() || result.get() != discardButton) {
                return;
            }
        }

        exitEditMode();
    }
    private void exitEditMode() {
        isEditMode = false;

        if (cardContainer != null && editTextArea != null && editButtonsBox != null) {
            cardContainer.getChildren().remove(editTextArea);
            cardContainer.getChildren().remove(editButtonsBox);
        }

        contentLabel.setVisible(true);
        contentLabel.setManaged(true);

        editTextArea = null;
        editButtonsBox = null;

        System.out.println("Exited edit mode");
    }
    private void handleDelete(ActionEvent event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Reply");
        confirmDialog.setHeaderText("Are you sure you want to delete this reply?");
        confirmDialog.setContentText(
                "Reply: " +
                        "This will permanently delete:\n" +
                        "• The reply\n" +
                        "• All replies to this reply\n\n" +
                        "⚠️ This action cannot be undone!"
        );
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == deleteButton) {
            try {
                try {
                    ServiceReply replyService = new ServiceReply();
                    replyService.deleteNestedReplies(reply.getId());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete replies: " + e.getMessage());
                }

                service.delete(reply);

                showAlert("Success",
                        "Reply has been deleted successfully!",
                        Alert.AlertType.INFORMATION);
                if (parentController != null) {
                    parentController.refreshReplies();
                }

            } catch (Exception e) {
                System.err.println("Error deleting thread: " + e.getMessage());
                e.printStackTrace();

                showAlert("Error",
                        "Failed to delete thread.\n\n" +
                                "Error: " + e.getMessage() + "\n\n" +
                                "Please try again or contact support if the problem persists.",
                        Alert.AlertType.ERROR);
            }
        } else {
        }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}