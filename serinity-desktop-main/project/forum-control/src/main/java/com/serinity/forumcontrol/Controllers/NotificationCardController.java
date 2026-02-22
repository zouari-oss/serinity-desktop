package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Notification;
import com.serinity.forumcontrol.Services.ServiceNotification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

/**
 * Controller for individual notification card
 */
public class NotificationCardController {

    @FXML private Label iconLabel;
    @FXML private Label contentLabel;
    @FXML private Label dateLabel;
    @FXML private Label unseenIndicator;
    @FXML private HBox root;

    private Notification notification;
    private ServiceNotification notificationService;
    private Runnable onClickCallback;
    private Runnable onDeleteCallback;

    public NotificationCardController() {
        this.notificationService = new ServiceNotification();
    }


    public void setNotification(Notification notification, Runnable onClickCallback, Runnable onDeleteCallback) {
        this.notification = notification;
        this.onClickCallback = onClickCallback;
        this.onDeleteCallback = onDeleteCallback;

        // Set icon based on type
        switch (notification.getType()) {
            case "like":
                iconLabel.setText("👍");
                break;
            case "dislike":
                iconLabel.setText("👎");
                break;
            case "follow":
                iconLabel.setText("⭐");
                break;
            case "comment":
                iconLabel.setText("💬");
                break;
            default:
                iconLabel.setText("🔔");
        }

        contentLabel.setText(notification.getContent());

        if (notification.getDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String formattedDate = notification.getDate().toLocalDateTime().format(formatter);
            dateLabel.setText(formattedDate);
        }

        // Show unseen indicator
        if (!notification.isSeen()) {
            unseenIndicator.setVisible(true);
            root.setStyle(root.getStyle() + "; -fx-background-color: #E3F2FD;");
        } else {
            unseenIndicator.setVisible(false);
        }

        // Add click handler to card
        root.setOnMouseClicked(event -> {
            if (onClickCallback != null) {
                onClickCallback.run();
            }
        });

        // Hover effect
        root.setOnMouseEntered(event -> {
            if (notification.isSeen()) {
                root.setStyle(root.getStyle() + "; -fx-background-color: #f5f5f5;");
            } else {
                root.setStyle(root.getStyle() + "; -fx-background-color: #BBDEFB;");
            }
        });

        root.setOnMouseExited(event -> {
            if (notification.isSeen()) {
                root.setStyle(root.getStyle().replace("-fx-background-color: #f5f5f5;", "-fx-background-color: white;"));
            } else {
                root.setStyle(root.getStyle().replace("-fx-background-color: #BBDEFB;", "-fx-background-color: #E3F2FD;"));
            }
        });
    }

    /**
     * Handle delete button click
     */
    @FXML
    private void onDelete() {
        if (notification != null) {
            notificationService.deleteNotification(notification.getId());
            if (onDeleteCallback != null) {
                onDeleteCallback.run();
            }
        }
    }

    public Notification getNotification() {
        return notification;
    }
}