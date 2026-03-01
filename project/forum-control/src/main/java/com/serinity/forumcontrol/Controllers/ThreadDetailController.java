package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Reply;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import com.serinity.forumcontrol.Models.Thread;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.List;

import javafx.stage.FileChooser;
import java.io.File;


public class ThreadDetailController {
    private com.serinity.forumcontrol.CurrentUser.CurrentUser user ;
    String currentUserId = user.getCurrentUserId();
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label lfoukLabel;
    @FXML private Label Category;
    @FXML private Label contentLabel;
    @FXML private ImageView threadImageView;
    @FXML private VBox repliesBox;
    @FXML private TextArea replyArea;
    @FXML private Button upvoteButton;
    @FXML private Button downvoteButton;
    @FXML private Label upvoteCount;
    @FXML private Label downvoteCount;
    @FXML private Label netVotesLabel;
    @FXML private Label repliescountLabel;
    @FXML private Button followButton;
    @FXML private Label followerCountLabel;
    @FXML private Button summarizeButton;
    @FXML private Label summaryLabel;
    @FXML private VBox summaryBox;
    @FXML private Button exportPdfButton;
    @FXML private Button translateButton;
    @FXML private javafx.scene.control.ComboBox<String> languageBox;

    private ServiceTranslate translateService = new ServiceTranslate();
    private ServicePdfExport pdfExportService = new ServicePdfExport();
    private ServicePostInteraction interactionService = new ServicePostInteraction();
    private ServiceThread service = new ServiceThread();
    private ServiceReply replyService = new ServiceReply();
    private ServiceSummarize summaryService = new ServiceSummarize();
    private Long replyingToParentId = null;
    private Thread thread;



    public void setThread(Thread t) {
        this.thread = t;
        languageBox.getItems().addAll(
                "French",
                "Arabic",
                "Spanish",
                "German",
                "English"
        );

        languageBox.setValue("French");
        if (summaryBox != null) {
            summaryBox.setVisible(false);
            summaryBox.setManaged(false);
            summarizeButton.setText("🤖 AI Summary");
            summarizeButton.setDisable(false);
        }
        String author = service.getAuthor(t.getUserId());
        String lfouk =
                "U/" + author +
                        "    " +
                        "Type: " + t.getType() +
                        "                                               " +
                        "                                               • Created at: " + t.getCreatedAt() +
                        "        • Updated at: " + t.getUpdatedAt();
        lfoukLabel.setText(lfouk);
        titleLabel.setText(t.getTitle());
        Category.setText("  C/" + service.getCategory(t.getCategoryId()));
        metaLabel.setText("Status: " + t.getStatus());
        contentLabel.setText(t.getContent());
        if (t.getImageUrl() != null && !t.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(t.getImageUrl(), true);
                threadImageView.setImage(image);
                threadImageView.setVisible(true);
                threadImageView.setManaged(true);
            } catch (Exception e) {
                System.err.println("Failed to load thread image: " + e.getMessage());
                threadImageView.setVisible(false);
                threadImageView.setManaged(false);
            }
        } else {
            threadImageView.setVisible(false);
            threadImageView.setManaged(false);
        }
        loadInteractions();
        loadReplies();
    }

    private void loadReplies() {
        repliesBox.getChildren().clear();
        List<Reply> top = replyService.getTopLevelReplies(thread.getId());
        for (Reply r : top) {
            Node card = buildReplyNode(r);
            repliesBox.getChildren().add(card);
        }
    }

    private Node buildReplyNode(Reply reply) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass()
                            .getResource("/fxml/forum/ReplyCard.fxml"));

            Parent card = loader.load();

            ReplyCardController ctrl = loader.getController();
            ctrl.setData(reply, this);

            VBox childrenBox = ctrl.getChildrenBox();

            List<Reply> children =
                    replyService.getNestedReplies(reply.getId());

            for (Reply child : children) {
                childrenBox.getChildren().add(
                        buildReplyNode(child)
                );
            }

            return card;

        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Error loading reply");
        }
    }

    @FXML
    private void addReply() {
        String text = replyArea.getText().trim();
        if (text.isEmpty()) return;

        Reply r = new Reply(
                thread.getId(),
                user.getCurrentUserId(),
                replyingToParentId,
                text
        );

        if (thread.getStatus() != ThreadStatus.LOCKED) {
            replyService.add(r);
        } else {
            alert("This Thread is Locked", Alert.AlertType.INFORMATION);
        }

        replyArea.clear();
        replyingToParentId = null;

        loadReplies();
    }

    public void refreshReplies() {
        System.out.println("Refreshing replies for thread: " + thread.getId());
        loadReplies();
    }

    private void alert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.setHeaderText(null);
        alert.show();
    }

    public void prepareReplyTo(long parentId) {
        replyingToParentId = parentId;
        replyArea.requestFocus();
        replyArea.setPromptText("Replying...");
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/forum/ForumPostsView.fxml"));
            Parent page = loader.load();

            BorderPane borderPane = findBorderPane();
            if (borderPane != null) {
                borderPane.setCenter(page);
            } else {
                System.err.println("Could not find BorderPane to go back");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.BorderPane findBorderPane() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            javafx.scene.Node node = titleLabel.getScene().getRoot();
            while (node != null) {
                if (node instanceof javafx.scene.layout.BorderPane) {
                    return (javafx.scene.layout.BorderPane) node;
                }
                node = node.getParent();
            }
        }
        return null;
    }

    public Thread getThread() {
        return this.thread;
    }

    private void loadInteractions() {
        int threadId = (int) thread.getId();

        Thread refreshedThread = service.getById(thread.getId());
        if (refreshedThread != null) {
            this.thread = refreshedThread;
        }

        int likeCount = thread.getLikecount();
        int dislikeCount = thread.getDislikecount();
        int followCount = thread.getFollowcount();
        int repliescount = thread.getRepliescount();
        upvoteCount.setText(String.valueOf(likeCount));
        downvoteCount.setText(String.valueOf(dislikeCount));

        int netVotes = likeCount - dislikeCount;
        String pointsText = Math.abs(netVotes) == 1 ? "point" : "points";
        netVotesLabel.setText(netVotes + " " + pointsText);
        String repliescountText = Math.abs(repliescount) == 1 ? "comment" : "comments";
        repliescountLabel.setText(repliescount + " " + repliescountText);


        String voteColor;
        if (netVotes > 0) {
            voteColor = "#4CAF50";
        } else if (netVotes < 0) {
            voteColor = "#F44336";
        } else {
            voteColor = "#2196F3";
        }

        netVotesLabel.setStyle(
                "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + voteColor + ";" +
                        "-fx-padding: 8 15;"
        );

        int userVote = interactionService.getUserVote(threadId, currentUserId);
        updateVoteButtonStyles(userVote);

        String followersText = followCount == 1 ? "follower" : "followers";
        followerCountLabel.setText(followCount + " " + followersText);

        boolean isFollowing = interactionService.isFollowing(threadId, currentUserId);
        updateFollowButton(isFollowing);

    }

    private void updateVoteButtonStyles(int userVote) {
        if (userVote == 1) {
            upvoteButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5;" +
                            "-fx-background-radius: 50%;"
            );
            downvoteButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );
        } else if (userVote == -1) {
            upvoteButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );
            downvoteButton.setStyle(
                    "-fx-background-color: #F44336;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5;" +
                            "-fx-background-radius: 50%;"
            );
        } else {
            upvoteButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );
            downvoteButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 18;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );
        }
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            followButton.setText("✓ Following");
            followButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        } else {
            followButton.setText("⭐ Follow");
            followButton.setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        }
    }

    @FXML
    private void onUpvote(MouseEvent event) {
        int threadId = (int) thread.getId();

        interactionService.toggleUpvote(threadId, currentUserId);

        loadInteractions();
    }

    @FXML
    private void onDownvote(MouseEvent event) {
        int threadId = (int) thread.getId();

        interactionService.toggleDownvote(threadId, currentUserId);

        loadInteractions();
    }

    @FXML
    private void onToggleFollow() {
        int threadId = (int) thread.getId();

        interactionService.toggleFollow(threadId, currentUserId);

        loadInteractions();

    }
    /**
     * Generate AI summary of thread content
     */
    @FXML
    private void onSummarize() {
        if (thread == null || thread.getContent() == null) {
            return;
        }

        // Check if API is configured
        if (!summaryService.isApiConfigured()) {
            alert("Hugging Face API token not configured in ServiceHuggingFace.java",
                    Alert.AlertType.WARNING);
            return;
        }

        // Disable button and show loading
        summarizeButton.setDisable(true);
        summarizeButton.setText("⏳ Generating summary...");

        // Use JavaFX Task for background work
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<String>() {
            @Override
            protected String call() throws Exception {
                String content = thread.getContent();
                return summaryService.summarizeText(content);
            }
        };

        // On success
        task.setOnSucceeded(event -> {
            String summary = task.getValue();
            if (summary != null && !summary.isEmpty()) {
                summaryLabel.setText(summary);
                summaryBox.setVisible(true);
                summaryBox.setManaged(true);
                summarizeButton.setText("✓ Summary Generated");
            } else {
                alert("Failed to generate summary. Please try again.",
                        Alert.AlertType.ERROR);
                summarizeButton.setText("🤖 AI Summary");
                summarizeButton.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
            if (errorMsg != null && errorMsg.contains("Model is loading")) {
                alert("AI model is warming up. Please try again in 10-20 seconds.",
                        Alert.AlertType.INFORMATION);
            } else {
                alert("Error generating summary: " + errorMsg,
                        Alert.AlertType.ERROR);
            }
            summarizeButton.setText("🤖 AI Summary");
            summarizeButton.setDisable(false);
        });

        java.lang.Thread backgroundThread = new java.lang.Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    @FXML
    private void onHideSummary() {
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);
        summarizeButton.setText("🤖 AI Summary");
        summarizeButton.setDisable(false);
    }
    @FXML
    private void onExportPdf() {
        if (thread == null) {
            return;
        }

        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Thread as PDF");

        // Set default filename
        String defaultFilename = pdfExportService.generateFilename(thread);
        fileChooser.setInitialFileName(defaultFilename);

        // Set file extension filter
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(pdfFilter);

        // Set initial directory to user's Documents folder
        String userHome = System.getProperty("user.home");
        File documentsDir = new File(userHome, "Documents");
        if (documentsDir.exists()) {
            fileChooser.setInitialDirectory(documentsDir);
        }

        // Show save dialog
        File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());

        if (file != null) {
            // Disable button and show progress
            exportPdfButton.setDisable(true);
            exportPdfButton.setText("⏳ Exporting...");

            // Export in background thread
            new java.lang.Thread(() -> {
                try {
                    // Get replies if available
                    List<Reply> replies = null;
                    if (replyService != null) {
                        replies = replyService.getTopLevelReplies(thread.getId());
                    }

                    // Export to PDF
                    boolean success = pdfExportService.exportThreadToPdf(
                            thread,
                            replies,
                            file.getAbsolutePath()
                    );

                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            exportPdfButton.setText("✓ PDF Saved");
                            alert("PDF exported successfully!\n\nSaved to: " + file.getAbsolutePath(),
                                    Alert.AlertType.INFORMATION);
                        } else {
                            exportPdfButton.setText("📄 Save as PDF");
                            alert("Failed to export PDF. Please try again.",
                                    Alert.AlertType.ERROR);
                        }

                        // Reset button after 3 seconds
                        new java.util.Timer().schedule(new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> {
                                    exportPdfButton.setText("📄 Save as PDF");
                                    exportPdfButton.setDisable(false);
                                });
                            }
                        }, 3000);
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        exportPdfButton.setText("📄 Save as PDF");
                        exportPdfButton.setDisable(false);
                        alert("Error exporting PDF: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }
    @FXML
    private void onQuickExportPdf() {
        if (thread == null) {
            return;
        }

        exportPdfButton.setDisable(true);
        exportPdfButton.setText("⏳ Exporting...");

        new java.lang.Thread(() -> {
            try {
                // Export to Downloads folder
                String userHome = System.getProperty("user.home");
                File downloadsDir = new File(userHome, "Downloads");
                String filename = pdfExportService.generateFilename(thread);
                File file = new File(downloadsDir, filename);

                List<Reply> replies = null;
                if (replyService != null) {
                    replies = replyService.getTopLevelReplies(thread.getId());
                }

                boolean success = pdfExportService.exportThreadToPdf(
                        thread,
                        replies,
                        file.getAbsolutePath()
                );

                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        exportPdfButton.setText("✓ PDF Saved");
                        alert("PDF saved to Downloads:\n" + filename,
                                Alert.AlertType.INFORMATION);
                    } else {
                        exportPdfButton.setText("📄 Save as PDF");
                        alert("Failed to export PDF", Alert.AlertType.ERROR);
                    }

                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                exportPdfButton.setText("📄 Save as PDF");
                                exportPdfButton.setDisable(false);
                            });
                        }
                    }, 3000);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    exportPdfButton.setText("📄 Save as PDF");
                    exportPdfButton.setDisable(false);
                    alert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }
    @FXML
    private void onTranslate() {

        if (thread == null || thread.getContent() == null) {
            return;
        }

        if (!translateService.isApiConfigured()) {
            alert("Gemini API key not configured.", Alert.AlertType.WARNING);
            return;
        }

        String targetLanguage = languageBox.getValue();
        String originalText = thread.getContent();

        translateButton.setDisable(true);
        translateButton.setText("⏳ Translating...");

        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                return translateService.translateText(originalText, targetLanguage);
            }
        };

        task.setOnSucceeded(event -> {
            String translated = task.getValue();

            if (translated != null && !translated.isEmpty()) {
                contentLabel.setText(translated);
                translateButton.setText("✓ Translated");
            } else {
                alert("Translation failed.", Alert.AlertType.ERROR);
                translateButton.setText("🌍 Translate");
                translateButton.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            alert("Error: " + task.getException().getMessage(),
                    Alert.AlertType.ERROR);

            translateButton.setText("🌍 Translate");
            translateButton.setDisable(false);
        });

        java.lang.Thread backgroundThread = new java.lang.Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}