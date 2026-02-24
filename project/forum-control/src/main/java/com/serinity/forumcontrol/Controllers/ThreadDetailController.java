package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.HardcodedUser.FakeUser;
import com.serinity.forumcontrol.Models.Reply;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Services.ServicePostInteraction;
import com.serinity.forumcontrol.Services.ServiceReply;
import com.serinity.forumcontrol.Services.ServiceThread;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.List;

public class ThreadDetailController {
    private FakeUser user ;
    String currentUserId = user.getCurrentUserId();
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label lfoukLabel;
    @FXML private Label Category;
    @FXML private TextArea contentArea;
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

    private ServicePostInteraction interactionService = new ServicePostInteraction();
    private ServiceThread service = new ServiceThread();
    private ServiceReply replyService = new ServiceReply();
    private Long replyingToParentId = null;
    private Thread thread;



    public void setThread(Thread t) {
        this.thread = t;
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
        contentArea.setText(t.getContent());
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
}