package com.serinity.accesscontrol.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MoodHistoryCardController {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  @FXML
  private VBox root;
  @FXML
  private Circle moodDot;
  @FXML
  private Label titleLabel;
  @FXML
  private Label subLabel;
  @FXML
  private Button btnEdit;
  @FXML
  private Button btnDelete;
  @FXML
  private Label chevronLabel;

  @FXML
  private FlowPane previewPane;

  @FXML
  private VBox detailsBox;
  @FXML
  private Label emotionsTitle;
  @FXML
  private FlowPane emotionsPane;
  @FXML
  private Label influencesTitle;
  @FXML
  private FlowPane influencesPane;

  private ResourceBundle resources;

  private MoodHistoryController.MoodCardVM vm;
  private Consumer<MoodHistoryController.MoodCardVM> onEdit;
  private Consumer<MoodHistoryController.MoodCardVM> onDelete;

  @FXML
  public void initialize() {
    // default collapsed
    collapse(detailsBox);

    // Toggle collapse on click
    root.setOnMouseClicked(e -> toggleDetails());

    // prevent card toggle when clicking buttons
    btnEdit.setOnMouseClicked(e -> e.consume());
    btnDelete.setOnMouseClicked(e -> e.consume());

    btnEdit.setOnAction(e -> {
      if (onEdit != null && vm != null)
        onEdit.accept(vm);
    });
    btnDelete.setOnAction(e -> {
      if (onDelete != null && vm != null)
        onDelete.accept(vm);
    });
  }

  public void setData(
      MoodHistoryController.MoodCardVM vm,
      ResourceBundle resources,
      Consumer<MoodHistoryController.MoodCardVM> onEdit,
      Consumer<MoodHistoryController.MoodCardVM> onDelete) {
    this.vm = vm;
    this.resources = resources;
    this.onEdit = onEdit;
    this.onDelete = onDelete;

    // Dot color
    moodDot.setFill(colorForMood(vm.moodLevel));

    // Title
    String type = "DAY".equalsIgnoreCase(vm.momentType) ? t("history.type.day") : t("history.type.moment");
    String time = "DAY".equalsIgnoreCase(vm.momentType) ? "" : (" • " + TIME_FMT.format(vm.dateTime));
    titleLabel.setText(type + time);

    // Sub
    subLabel.setText(t("history.mood.prefix") + " " + t("mood.level." + vm.moodLevel));

    // Titles
    emotionsTitle.setText(t("history.details.emotions"));
    influencesTitle.setText(t("history.details.influences"));

    // Preview tags
    previewPane.getChildren().clear();
    addPreviewTags(previewPane, vm.emotions, 3, "emotion-tag", "emotion.");
    addPreviewTags(previewPane, vm.influences, 2, "influence-tag", "influence.");

    // Full details
    emotionsPane.getChildren().clear();
    for (String code : vm.emotions)
      emotionsPane.getChildren().add(tag(t("emotion." + code), "emotion-tag"));

    influencesPane.getChildren().clear();
    for (String code : vm.influences)
      influencesPane.getChildren().add(tag(t("influence." + code), "influence-tag"));
  }

  private void addPreviewTags(FlowPane pane, List<String> items, int max, String styleClass, String keyPrefix) {
    int count = 0;
    for (String s : items) {
      if (count >= max)
        break;
      pane.getChildren().add(tag(t(keyPrefix + s), styleClass));
      count++;
    }
    int remaining = items.size() - max;
    if (remaining > 0) {
      Label more = new Label("+" + remaining);
      more.getStyleClass().addAll("tag", "tag-more");
      pane.getChildren().add(more);
    }
  }

  private Label tag(String text, String extraClass) {
    Label l = new Label(text);
    l.getStyleClass().addAll("tag", extraClass);
    return l;
  }

  private void toggleDetails() {
    boolean collapsed = !detailsBox.isVisible();
    if (collapsed) {
      chevronLabel.setText("⌃");
      expandAnimated(detailsBox);
    } else {
      chevronLabel.setText("⌄");
      collapseAnimated(detailsBox);
    }
  }

  private void collapse(final Region n) {
    n.setVisible(false);
    n.setManaged(false);
    n.setOpacity(0);
    n.setMaxHeight(0);
  }

  private void expandAnimated(final Region n) {
    n.setVisible(true);
    n.setManaged(true);

    n.setMaxHeight(0);
    n.applyCss();
    n.autosize();

    double target = Math.max(120, n.prefHeight(-1));

    Timeline tl = new Timeline(
        new KeyFrame(Duration.millis(200),
            new KeyValue(n.maxHeightProperty(), target, Interpolator.EASE_OUT),
            new KeyValue(n.opacityProperty(), 1, Interpolator.EASE_OUT)));
    n.setOpacity(0);
    tl.play();
  }

  private void collapseAnimated(final Region n) {
    Timeline tl = new Timeline(
        new KeyFrame(Duration.millis(160),
            new KeyValue(n.maxHeightProperty(), 0, Interpolator.EASE_IN),
            new KeyValue(n.opacityProperty(), 0, Interpolator.EASE_IN)));
    tl.setOnFinished(e -> collapse(n));
    tl.play();
  }

  private String t(String key) {
    if (resources == null)
      return key;
    try {
      return resources.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
  }

  private Color colorForMood(final int level) {
    switch (level) {
      case 1:
        return Color.web("#6B7C93");
      case 2:
        return Color.web("#4F8FB8");
      case 3:
        return Color.web("#7A8C8C");
      case 4:
        return Color.web("#62B48F");
      case 5:
        return Color.web("#B9C56A");
      default:
        return Color.web("#7A8C8C");
    }
  }
}
