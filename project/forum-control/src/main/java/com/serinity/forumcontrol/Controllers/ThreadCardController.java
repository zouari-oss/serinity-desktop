package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Services.ServiceReply;
import com.serinity.forumcontrol.Services.ServiceThread;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import com.serinity.forumcontrol.Models.Thread;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ThreadCardController {
    private com.serinity.forumcontrol.CurrentUser.CurrentUser user;
    String currentUserId = user.getCurrentUserId();
    @FXML
    private VBox rootCard;
    @FXML
    private Label titleLabel;
    @FXML
    private Label interactionLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private Label lfoukLabel;
    @FXML
    private MenuButton menuButton;
    @FXML
    private Label badgeLabel;
    private ServiceThread service = new ServiceThread();
    private Thread thread;
    private Runnable onRefreshCallback;
    public void setData(Thread t) {
        this.thread = t;

        int likeCount = thread.getLikecount();
        int dislikeCount = thread.getDislikecount();
        int followCount = thread.getFollowcount();
        int repliescount = thread.getRepliescount();

        int netVotes = likeCount - dislikeCount;
        String pointsText = Math.abs(netVotes) == 1 ? "point" : "points";
        String repliescountText = Math.abs(repliescount) == 1 ? "comment" : "comments";
        String followersText = followCount == 1 ? "follower" : "followers";
        String lfouk =
                " • C/" + service.getCategory(t.getCategoryId()) +
                        "                                               "+
                        "                                               • Created at: " + t.getCreatedAt();
        titleLabel.setText(t.getTitle());
        String meta =
                " • Status: " + t.getStatus() +
                        " • Type: " + t.getType() +
                        "                                               "+
                        (t.isPinned() ? "                                               📌 PINNED" : "");

        metaLabel.setText(meta);
        interactionLabel.setText(netVotes + " " + pointsText + "       " +repliescount + " " + repliescountText+ "      " +followCount + " " + followersText);
        lfoukLabel.setText(lfouk);
        configureMenu();
        applyPinnedStyling();
        String badge = service.getThreadBadge(t.getId());
        if (badgeLabel != null) {
            badgeLabel.setText(badge);
            applyBadgeStyling(badge);
        }
    }

    @FXML
    private void openDetail() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass()
                            .getResource("/fxml/forum/ThreadDetailView.fxml"));

            Parent page = loader.load();

            ThreadDetailController controller =
                    loader.getController();

            controller.setThread(thread);

            BorderPane borderPane = findBorderPane();
            if (borderPane != null) {
                borderPane.setCenter(page);
            } else {
                System.err.println("Could not find BorderPane to open thread detail");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure the three-dot menu based on user permissions
     */
    private void configureMenu() {
        try {

            boolean isAdmin = service.isAdmin(currentUserId);
            boolean isOwner = thread.getUserId().equalsIgnoreCase(currentUserId);

            // Only show menu if user has any permissions
            if (isOwner || isAdmin) {
                menuButton.setVisible(true);
                menuButton.setManaged(true);

                // Clear existing menu items
                menuButton.getItems().clear();

                // Build menu based on permissions
                if (isOwner && !isAdmin) {
                    // Regular user, own post
                    buildOwnerMenu();
                } else if (isAdmin && !isOwner) {
                    // Admin, not own post
                    buildAdminNotOwnerMenu();
                } else if (isAdmin && isOwner) {
                    // Admin, own post
                    buildAdminOwnerMenu();
                }
            } else {
                // Hide menu for users with no permissions
                menuButton.setVisible(false);
                menuButton.setManaged(false);
            }

        } catch (Exception e) {
            System.err.println("Error configuring menu: " + e.getMessage());
            e.printStackTrace();
            menuButton.setVisible(false);
            menuButton.setManaged(false);
        }
    }

    /**
     * Build menu for regular user (own post)
     * Options: Edit, Delete, Archive/Unarchive, Lock/Unlock
     */
    private void buildOwnerMenu() {
        // Edit option
        MenuItem editItem = new MenuItem("✏️ Edit");
        editItem.setOnAction(this::handleEdit);

        // Delete option
        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        // Archive/Unarchive option (based on current status)
        MenuItem archiveItem;
        if (thread.getStatus() == ThreadStatus.ARCHIVED) {
            archiveItem = new MenuItem("📂 Unarchive");
            archiveItem.setOnAction(this::handleUnarchive);
        } else {
            archiveItem = new MenuItem("📁 Archive");
            archiveItem.setOnAction(this::handleArchive);
        }

        // Lock/Unlock option (based on current status)
        MenuItem lockItem;
        if (thread.getStatus() == ThreadStatus.LOCKED) {
            lockItem = new MenuItem("🔓 Unlock");
            lockItem.setOnAction(this::handleUnlock);
        } else {
            lockItem = new MenuItem("🔒 Lock");
            lockItem.setOnAction(this::handleLock);
        }

        // Add items to menu
        menuButton.getItems().addAll(editItem, deleteItem, archiveItem, lockItem);
    }

    /**
     * Build menu for admin (not own post)
     * Options: Delete, Archive
     */
    private void buildAdminNotOwnerMenu() {
        // Delete option
        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        // Archive option
        MenuItem archiveItem;
        if (thread.getStatus() == ThreadStatus.ARCHIVED) {
            archiveItem = new MenuItem("📂 Unarchive");
            archiveItem.setOnAction(this::handleUnarchive);
        } else {
            archiveItem = new MenuItem("📁 Archive");
            archiveItem.setOnAction(this::handleArchive);
        }

        // Add items to menu
        menuButton.getItems().addAll(deleteItem, archiveItem);
    }

    /**
     * Build menu for admin (own post)
     * Options: Edit, Delete, Archive/Unarchive, Lock/Unlock, Pin/Unpin
     */
    private void buildAdminOwnerMenu() {
        // Edit option
        MenuItem editItem = new MenuItem("✏️ Edit");
        editItem.setOnAction(this::handleEdit);

        // Delete option
        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        // Archive/Unarchive option
        MenuItem archiveItem;
        if (thread.getStatus() == ThreadStatus.ARCHIVED) {
            archiveItem = new MenuItem("📂 Unarchive");
            archiveItem.setOnAction(this::handleUnarchive);
        } else {
            archiveItem = new MenuItem("📁 Archive");
            archiveItem.setOnAction(this::handleArchive);
        }

        // Lock/Unlock option
        MenuItem lockItem;
        if (thread.getStatus() == ThreadStatus.LOCKED) {
            lockItem = new MenuItem("🔓 Unlock");
            lockItem.setOnAction(this::handleUnlock);
        } else {
            lockItem = new MenuItem("🔒 Lock");
            lockItem.setOnAction(this::handleLock);
        }

        // Pin/Unpin option
        MenuItem pinItem;
        if (thread.isPinned()) {
            pinItem = new MenuItem("📍 Unpin");
            pinItem.setOnAction(this::handleUnpin);
        } else {
            pinItem = new MenuItem("📌 Pin");
            pinItem.setOnAction(this::handlePin);
        }

        // Add items to menu
        menuButton.getItems().addAll(editItem, deleteItem, archiveItem, lockItem, pinItem);
    }

    private void handleDelete(ActionEvent event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Thread");
        confirmDialog.setHeaderText("Are you sure you want to delete this thread?");
        confirmDialog.setContentText(
                "Thread: \"" + thread.getTitle() + "\"\n\n" +
                        "This will permanently delete:\n" +
                        "• The thread\n" +
                        "• All replies to this thread\n\n" +
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
                    replyService.deleteByThread(thread.getId());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete replies: " + e.getMessage());
                }

                service.delete(thread);

                showAlert("Success",
                        "Thread \"" + thread.getTitle() + "\" has been deleted successfully!",
                        Alert.AlertType.INFORMATION);
                refreshThreadList();

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
        refreshCurrentPage(1);
    }


    private void handleEdit(ActionEvent event) {
        try {
            System.out.println("Edit thread: " + thread.getId());

            // Load AddThread.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/AddThread.fxml"));
            Parent editView = loader.load();

            // Get the controller and set it to Edit mode
            AddThreadController controller = loader.getController();
            controller.setEditMode(thread);

            System.out.println("Edit view loaded with thread data");

            // Find the BorderPane and replace center content
            BorderPane borderPane = findBorderPane();
            if (borderPane != null) {
                borderPane.setCenter(editView);
                System.out.println("Edit view displayed");
            } else {
                System.err.println("Could not find BorderPane");
                showAlert("Error",
                        "Could not open edit view. Please try again.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("Error opening edit view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error",
                    "Failed to open edit view.\n\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private BorderPane findBorderPane() {
        if (rootCard != null && rootCard.getScene() != null) {
            javafx.scene.Node node = rootCard.getScene().getRoot();

            while (node != null) {
                if (node instanceof BorderPane) {
                    return (BorderPane) node;
                }
                node = node.getParent();
            }
        }
        return null;
    }

    private void handleUnpin(ActionEvent event) {
        service.togglePin(thread.getId());
        refreshThreadList();
        refreshCurrentPage(1);
    }

    private void handlePin(ActionEvent event) {
        service.togglePin(thread.getId());
        refreshThreadList();
        refreshCurrentPage(1);
    }

    private void handleUnlock(ActionEvent event) {
        service.updateStatus(thread.getId(), ThreadStatus.OPEN);
        refreshThreadList();
        refreshCurrentPage(2);
    }

    private void handleLock(ActionEvent event) {
        service.updateStatus(thread.getId(), ThreadStatus.LOCKED);
        refreshThreadList();
        refreshCurrentPage(2);
    }

    private void handleUnarchive(ActionEvent event) {
        service.updateStatus(thread.getId(), ThreadStatus.OPEN);
        refreshThreadList();
        refreshCurrentPage(3);
    }

    private void handleArchive(ActionEvent event) {
        service.updateStatus(thread.getId(), ThreadStatus.ARCHIVED);
        refreshThreadList();
        refreshCurrentPage(1);
    }

    public void handleMenuClick(MouseEvent event) {
        event.consume();
    }
    private void refreshThreadList() {
        if (onRefreshCallback != null) {
            onRefreshCallback.run();
        }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
    private void refreshCurrentPage(int k) {
        try {
            // Load ThreadsList.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/ForumPostsView.fxml"));
            Parent ForumPostsView = loader.load();

            // Get controller and reload threads
            ForumPostsController controller = loader.getController();
            if(k==1)
            {controller.loadThreads();}
            else if(k==2)
            {controller.loadMyThreads();}
            else
            {controller.loadArchivedThreads();}

            // Replace center content
            BorderPane borderPane = findBorderPane();
            if (borderPane != null) {
                borderPane.setCenter(ForumPostsView);
            }

        } catch (IOException e) {
            // Fallback to callback if available
            if (onRefreshCallback != null) {
                onRefreshCallback.run();
            }
        }
    }
    private void applyPinnedStyling() {
        if (thread.isPinned()) {
            // Pinned thread - special golden/highlighted style
            rootCard.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #FFF9E6 0%, #FFFEF8 100%);" +
                            "-fx-padding: 16;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-radius: 14;" +
                            "-fx-border-color: #008cff;" +  // Gold border
                            "-fx-border-width: 2;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,72,255,0.3), 10, 0, 0, 3);" // Golden shadow
            );

            // Make title more prominent for pinned threads
            titleLabel.setStyle(
                    "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #0b42b8;" // Dark golden text
            );

            // Add golden tint to metadata
            metaLabel.setStyle(
                    "-fx-text-fill: #2029da;" // Goldenrod color
            );

            lfoukLabel.setStyle(
                    "-fx-text-fill: #051261;" // Dark golden
            );
        } else {
            // Normal thread - default white style
            rootCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-padding: 16;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-radius: 14;" +
                            "-fx-border-color: #dddddd;"
            );

            titleLabel.setStyle(
                    "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;"
            );

            metaLabel.setStyle(
                    "-fx-text-fill: gray;"
            );

            lfoukLabel.setStyle(
                    "-fx-text-fill: gray;"
            );
        }
    }
    private void applyBadgeStyling(String badge) {
        if (badgeLabel == null) return;

        String baseStyle =
                "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-padding: 2 8 2 8;" +
                        "-fx-background-radius: 20;" +
                        "-fx-text-fill: white;";

        String bgColor;
        switch (badge) {
            case "🏆 Elite"   -> bgColor = "#7c3aed"; // purple
            case "🔥 Hot"     -> bgColor = "#ef4444"; // red
            case "⭐ Rising"  -> bgColor = "#f59e0b"; // amber
            case "💬 Active"  -> bgColor = "#3b82f6"; // blue
            default           -> bgColor = "#6b7280"; // grey (New)
        }
        badgeLabel.setStyle(baseStyle + "-fx-background-color: " + bgColor + ";");
    }

}