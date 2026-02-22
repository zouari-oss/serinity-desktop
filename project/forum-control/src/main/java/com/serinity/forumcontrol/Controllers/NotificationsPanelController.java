package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.HardcodedUser.FakeUser;
import com.serinity.forumcontrol.Models.Notification;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Services.ServiceNotification;
import com.serinity.forumcontrol.Services.ServiceThread;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Controller for notifications panel popup
 */
public class NotificationsPanelController {

    @FXML private VBox notificationsBox;
    @FXML private VBox emptyState;
    @FXML private Button markAllReadButton;
    @FXML private Button clearAllButton;

    private ServiceNotification notificationService;
    private ServiceThread threadService;
    private FakeUser user;
    private Runnable onCloseCallback;

    public NotificationsPanelController() {
        this.notificationService = new ServiceNotification();
        this.threadService = new ServiceThread();
    }

    @FXML
    public void initialize() {
        loadNotifications();
    }

    public void loadNotifications() {
        notificationsBox.getChildren().clear();

        String userId = user.getCurrentUserId();
        List<Notification> notifications = notificationService.getUserNotifications(userId);

        if (notifications.isEmpty()) {
            // Show empty state
            notificationsBox.setVisible(false);
            notificationsBox.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            markAllReadButton.setDisable(true);
            clearAllButton.setDisable(true);
        } else {
            // Show notifications
            notificationsBox.setVisible(true);
            notificationsBox.setManaged(true);
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            markAllReadButton.setDisable(false);
            clearAllButton.setDisable(false);

            for (Notification notification : notifications) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/forum/Notificationcard.fxml")
                    );
                    Node card = loader.load();

                    NotificationCardController controller = loader.getController();
                    controller.setNotification(
                            notification,
                            () -> onNotificationClick(notification),
                            this::loadNotifications // Refresh on delete
                    );

                    notificationsBox.getChildren().add(card);

                } catch (IOException e) {
                    System.err.println("Error loading notification card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle notification click - open thread detail
     */
    private void onNotificationClick(Notification notification) {
        // Mark as seen
        notificationService.markAsSeen(notification.getId());

        // Get thread
        Thread thread = threadService.getById(notification.getThreadId());

        if (thread == null) {
            showAlert("Thread not found", "This thread no longer exists.", Alert.AlertType.WARNING);
            return;
        }

        // Navigate to thread detail
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/ThreadDetailView.fxml")
            );
            Parent threadView = loader.load();

            ThreadDetailController controller = loader.getController();
            controller.setThread(thread);

            // Find the content host and switch view
            StackPane host = findContentHost();
            if (host != null) {
                host.getChildren().setAll(threadView);
            }

            // Close the notifications panel
            closePanel();

        } catch (IOException e) {
            System.err.println("Error loading thread detail: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open thread: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Mark all notifications as read
     */
    @FXML
    private void onMarkAllAsRead() {
        String userId = user.getCurrentUserId();
        notificationService.markAllAsSeen(userId);
        loadNotifications();

        // Notify parent to update badge
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    /**
     * Clear all notifications
     */
    @FXML
    private void onClearAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Notifications");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete all notifications?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            String userId = user.getCurrentUserId();
            notificationService.deleteAllForUser(userId);
            loadNotifications();

            // Notify parent to update badge
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        }
    }

    /**
     * Close the notifications panel
     */
    @FXML
    private void onClose() {
        closePanel();
    }

    /**
     * Close this panel
     */
    private void closePanel() {
        StackPane overlay = (StackPane) notificationsBox.getScene().lookup("#overlayPane");
        if (overlay != null) {
            overlay.getChildren().clear();
            overlay.setVisible(false);
        }
    }

    /**
     * Find the content host StackPane
     */
    private StackPane findContentHost() {
        if (notificationsBox.getScene() != null) {
            return (StackPane) notificationsBox.getScene().lookup("#contentHost");
        }
        return null;
    }

    /**
     * Set callback for when panel closes (to update badge)
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}