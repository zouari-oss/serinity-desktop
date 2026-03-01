package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.HardcodedUser.FakeUser;
import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Models.ThreadType;
import com.serinity.forumcontrol.Services.ServiceCategory;
import com.serinity.forumcontrol.Services.ServiceNotification;
import com.serinity.forumcontrol.Services.ServicePostInteraction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Services.ServiceThread;

import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ForumPostsController {
    String currentUserId = FakeUser.getCurrentUserId();
    @FXML private VBox cardsContainer;
    @FXML private Button categoriesButton;
    @FXML private Button newCategoryButton;
    @FXML private Button homeButton;
    @FXML private Button myThreadsButton;
    @FXML private Button followedButton;
    @FXML private Button archivedButton;
    @FXML private BorderPane rootPane;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private Button toggleFilterButton;
    @FXML private HBox filterPanel;
    @FXML private CheckBox statusOpenCheckBox;
    @FXML private CheckBox statusLockedCheckBox;
    @FXML private CheckBox typeDiscussionCheckBox;
    @FXML private CheckBox typeQuestionCheckBox;
    @FXML private CheckBox typeAnnouncementCheckBox;
    @FXML private VBox categoriesFilterBox;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private Button notificationsButton;
    @FXML private Label notificationBadge;
    @FXML private StackPane overlayPane;

    private ServiceNotification notificationService = new ServiceNotification(); // NEW
    private ServiceThread service = new ServiceThread();
    private ServiceCategory categoryService = new ServiceCategory();
    private List<Thread> currentThreads = new ArrayList<>();
    private Map<Long, CheckBox> categoryCheckBoxes = new HashMap<>();
    private Map<Long, VBox> subcategoryContainers = new HashMap<>();

    private Button currentActiveButton;


    @FXML
    public void initialize() {
        loadThreads();
        configureSearch();
        configureFilters();
        loadCheckboxCategories();
        configureCategory();
        setActiveNavButton(homeButton);
        updateNotificationBadge();
        Timeline notificationRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> updateNotificationBadge())
        );
        notificationRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationRefreshTimeline.play();
    }
    private void configureSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                onSearchTextChanged(newValue);
            });
        }
    }

    private void onSearchTextChanged(String searchText) {
        if (clearSearchButton != null) {
            if (searchText != null && !searchText.trim().isEmpty()) {
                clearSearchButton.setVisible(true);
                clearSearchButton.setManaged(true);
            } else {
                clearSearchButton.setVisible(false);
                clearSearchButton.setManaged(false);
            }
        }

        applyFilters();
    }
    private void applyFilters() {
        List<Thread> filtered = new ArrayList<>(currentThreads);

        String searchText = searchField != null ? searchField.getText() : "";
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerSearch = searchText.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        Set<ThreadStatus> allowedStatuses = new HashSet<>();
        if (statusOpenCheckBox != null && statusOpenCheckBox.isSelected()) {
            allowedStatuses.add(ThreadStatus.OPEN);
        }
        if (statusLockedCheckBox != null && statusLockedCheckBox.isSelected()) {
            allowedStatuses.add(ThreadStatus.LOCKED);
        }

        if (!allowedStatuses.isEmpty()) {
            filtered = filtered.stream()
                    .filter(t -> allowedStatuses.contains(t.getStatus()))
                    .collect(Collectors.toList());
        }

        Set<ThreadType> allowedTypes = new HashSet<>();
        if (typeDiscussionCheckBox != null && typeDiscussionCheckBox.isSelected()) {
            allowedTypes.add(ThreadType.DISCUSSION);
        }
        if (typeQuestionCheckBox != null && typeQuestionCheckBox.isSelected()) {
            allowedTypes.add(ThreadType.QUESTION);
        }
        if (typeAnnouncementCheckBox != null && typeAnnouncementCheckBox.isSelected()) {
            allowedTypes.add(ThreadType.ANNOUNCEMENT);
        }

        if (!allowedTypes.isEmpty()) {
            filtered = filtered.stream()
                    .filter(t -> allowedTypes.contains(t.getType()))
                    .collect(Collectors.toList());
        }

        Set<Long> allowedCategories = new HashSet<>();
        for (Map.Entry<Long, CheckBox> entry : categoryCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                allowedCategories.add(entry.getKey());
            }
        }

        if (!allowedCategories.isEmpty()) {
            filtered = filtered.stream()
                    .filter(t -> allowedCategories.contains(t.getCategoryId()))
                    .collect(Collectors.toList());
        }
        filtered = sortThreads(filtered);


        displayThreads(filtered);
    }
    @FXML
    private void onClearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }


    private void configureCategory() {
        try {
            boolean isAdmin = service.isAdmin(currentUserId);
            if (isAdmin) {
                categoriesButton.setVisible(true);
                categoriesButton.setManaged(true);
                newCategoryButton.setVisible(true);
                newCategoryButton.setManaged(true);
            } else {
                categoriesButton.setVisible(false);
                categoriesButton.setManaged(false);
                newCategoryButton.setVisible(false);
                newCategoryButton.setManaged(false);
            }

        } catch (Exception e) {
            System.err.println("Error configuring menu: " + e.getMessage());
            e.printStackTrace();
            categoriesButton.setVisible(false);
            categoriesButton.setManaged(false);
            newCategoryButton.setVisible(false);
            newCategoryButton.setManaged(false);
        }
    }
    @FXML
    private void onNewThread() {
        try {


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forum/AddThread.fxml"));
            Parent addThreadView = loader.load();

            BorderPane borderPane = findBorderPane();

            if (borderPane != null) {
                borderPane.setCenter(addThreadView);

            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error Loading View",
                    "Could not load New Thread form.\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Check that AddThread.fxml is at: /fxml/forum/AddThread.fxml",
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Unexpected Error",
                    "An unexpected error occurred: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onNewCategory() {
        try {


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forum/AddCategory.fxml"));
            Parent addCategoryView = loader.load();

            System.out.println("FXML loaded successfully");
            BorderPane borderPane = findBorderPane();

            if (borderPane != null) {
                borderPane.setCenter(addCategoryView);
                System.out.println("AddCategory view set in center pane");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error Loading View",
                    "Could not load New Category form.\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Check that AddCategory.fxml is at: /fxml/forum/AddCategory.fxml",
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Unexpected Error",
                    "An unexpected error occurred: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onHome(){
        toggleFilterButton.setVisible(true);toggleFilterButton.setManaged(true);
        loadThreads();
    }
    @FXML
    private void onMyThreads(){
        toggleFilterButton.setVisible(true);
        toggleFilterButton.setManaged(true);
        loadMyThreads();
    }
    @FXML
    private void onFollowed(){
        toggleFilterButton.setVisible(true);
        toggleFilterButton.setManaged(true);
        loadFollowedThreads();

    }
    @FXML
    private void onArchived(){
        toggleFilterButton.setVisible(false);
        toggleFilterButton.setManaged(false);
        loadArchivedThreads();
    }
    @FXML
    private void onCategories(){
        toggleFilterButton.setVisible(false);
        toggleFilterButton.setManaged(false);
        loadCategories();
    }
    public void loadThreads() {
        setActiveNavButton(homeButton);
        try {
            cardsContainer.getChildren().clear();
            ServiceThread service = new ServiceThread();
            List<Thread> threads = service.getAll(FakeUser.getCurrentUserId()).stream().filter(t-> t.getStatus()!= ThreadStatus.ARCHIVED).toList();
            currentThreads = threads;
            displayThreads(threads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadMyThreads() {
        setActiveNavButton(myThreadsButton);
        try {
            cardsContainer.getChildren().clear();
            ServiceThread service = new ServiceThread();
            List<Thread> threads = service.getAll().stream().filter(t-> t.getUserId().equalsIgnoreCase(FakeUser.getCurrentUserId()) ).filter(t-> t.getStatus()!= ThreadStatus.ARCHIVED).toList();
            currentThreads = threads;
            displayThreads(threads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadArchivedThreads() {
        setActiveNavButton(archivedButton);
        try {
            cardsContainer.getChildren().clear();
            ServiceThread service = new ServiceThread();
            List<Thread> threads = service.getAll().stream().filter(t-> (t.getUserId().equalsIgnoreCase(FakeUser.getCurrentUserId()) && t.getStatus()== ThreadStatus.ARCHIVED)).toList();
            currentThreads = threads;
            displayThreads(threads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCategories() {
        setActiveNavButton(categoriesButton);
        try {
            cardsContainer.getChildren().clear();
            ServiceCategory service = new ServiceCategory();
            List<Category> categories = service.getAll();

            for (Category c : categories) {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/forum/CategoryCard.fxml"));

                Node card = loader.load();

                CategoryCardController ctrl =
                        loader.getController();

                ctrl.setData(c);

                cardsContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadFollowedThreads() {
        setActiveNavButton(followedButton);
        try {
            cardsContainer.getChildren().clear();
            ServicePostInteraction interactionService = new ServicePostInteraction();
            ServiceThread service = new ServiceThread();
            List<Thread> threads = service.getAll().stream()
                    .filter(t->interactionService.isFollowing((int) t.getId(),currentUserId))
                    .filter(t -> t.getStatus() != ThreadStatus.ARCHIVED)
                    .collect(Collectors.toList());

            currentThreads = threads;
            displayThreads(threads);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error",
                    "Failed to load followed threads: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    public void displayThreads(List<Thread> threads) {
        try {
            cardsContainer.getChildren().clear();

            if (threads.isEmpty()) {
                javafx.scene.control.Label noResults = new javafx.scene.control.Label();

                if (searchField != null && !searchField.getText().trim().isEmpty()) {
                    noResults.setText("🔍 No threads found matching \"" + searchField.getText() + "\"");
                } else {
                    noResults.setText("📭 No threads to display");
                }

                noResults.setStyle("-fx-font-size: 16; -fx-text-fill: #999; -fx-padding: 50;");
                cardsContainer.getChildren().add(noResults);
                return;
            }

            for (Thread t : threads) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/forum/ThreadCard.fxml"));

                Node card = loader.load();
                ThreadCardController ctrl = loader.getController();
                ctrl.setData(t);

                cardsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to display threads: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private BorderPane findBorderPane() {
        if (rootPane != null) {
            return rootPane;
        }

        if (cardsContainer != null && cardsContainer.getParent() != null) {
            javafx.scene.Node node = cardsContainer;
            while (node != null) {
                if (node instanceof BorderPane) {
                    return (BorderPane) node;
                }
                node = node.getParent();
            }
        }

        return null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
    private void configureFilters() {
        if (statusOpenCheckBox != null) {
            statusOpenCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        }
        if (statusLockedCheckBox != null) {
            statusLockedCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        }
        if (typeDiscussionCheckBox != null) {
            typeDiscussionCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        }
        if (typeQuestionCheckBox != null) {
            typeQuestionCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        }
        if (typeAnnouncementCheckBox != null) {
            typeAnnouncementCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        }
    }
    private void loadCheckboxCategories() {
        if (categoriesFilterBox == null) return;

        try {
            categoriesFilterBox.getChildren().clear();
            categoryCheckBoxes.clear();
            subcategoryContainers.clear();

            List<Category> allCategories = categoryService.getAll();
            List<Category> rootCategories = allCategories.stream()
                    .filter(c -> c.getParentId() == null)
                    .collect(Collectors.toList());

            for (Category category : rootCategories) {
                VBox categoryBox = createCategoryFilterItem(category, allCategories);
                categoriesFilterBox.getChildren().add(categoryBox);
            }

        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private VBox createCategoryFilterItem(Category category, List<Category> allCategories) {
        VBox container = new VBox(5);
        HBox categoryRow = new HBox(5);
        categoryRow.setStyle("-fx-alignment: CENTER_LEFT;");
        List<Category> subcategories = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .collect(Collectors.toList());
        Button expandButton = new Button("+");
        expandButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 0; -fx-min-width: 20;");

        if (subcategories.isEmpty()) {
            expandButton.setVisible(false);
            expandButton.setManaged(false);
        }
        CheckBox categoryCheckBox = new CheckBox(category.getName());
        categoryCheckBox.setSelected(false);
        categoryCheckBoxes.put(category.getId(), categoryCheckBox);
        categoryCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            if (!newVal && !subcategories.isEmpty()) {
                for (Category sub : subcategories) {
                    CheckBox subCheckBox = categoryCheckBoxes.get(sub.getId());
                    if (subCheckBox != null) {
                        subCheckBox.setSelected(false);
                    }
                }
            }
            else if (newVal && !subcategories.isEmpty()) {
                for (Category sub : subcategories) {
                    CheckBox subCheckBox = categoryCheckBoxes.get(sub.getId());
                    if (subCheckBox != null) {
                        subCheckBox.setSelected(true);
                    }
                }
            }
            applyFilters();
        });

        categoryRow.getChildren().addAll(expandButton, categoryCheckBox);
        container.getChildren().add(categoryRow);

        if (!subcategories.isEmpty()) {
            VBox subcategoriesBox = new VBox(3);
            subcategoriesBox.setPadding(new Insets(0, 0, 0, 25));
            subcategoriesBox.setVisible(false);
            subcategoriesBox.setManaged(false);
            subcategoryContainers.put(category.getId(), subcategoriesBox);
            for (Category subcat : subcategories) {
                CheckBox subCheckBox = new CheckBox(subcat.getName());
                subCheckBox.setSelected(false);
                subCheckBox.setStyle("-fx-font-size: 12;");
                categoryCheckBoxes.put(subcat.getId(), subCheckBox);
                subCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
                    applyFilters();
                });
                subcategoriesBox.getChildren().add(subCheckBox);
            }
            container.getChildren().add(subcategoriesBox);

            expandButton.setOnAction(e -> {
                boolean isExpanded = subcategoriesBox.isVisible();
                subcategoriesBox.setVisible(!isExpanded);
                subcategoriesBox.setManaged(!isExpanded);
                expandButton.setText(isExpanded ? "+" : "-");
            });
        }

        return container;
    }
    @FXML
    private void onToggleFilters() {
        if (filterPanel != null && toggleFilterButton != null) {
            boolean isVisible = filterPanel.isVisible();
            filterPanel.setVisible(!isVisible);
            filterPanel.setManaged(!isVisible);
            toggleFilterButton.setText(isVisible ? "🔽 Filters" : "🔼 Filters");
        }
    }
    @FXML
    private void onResetFilters() {
        if (statusOpenCheckBox != null) statusOpenCheckBox.setSelected(false);
        if (statusLockedCheckBox != null) statusLockedCheckBox.setSelected(false);

        if (typeDiscussionCheckBox != null) typeDiscussionCheckBox.setSelected(false);
        if (typeQuestionCheckBox != null) typeQuestionCheckBox.setSelected(false);
        if (typeAnnouncementCheckBox != null) typeAnnouncementCheckBox.setSelected(false);

        for (CheckBox cb : categoryCheckBoxes.values()) {
            cb.setSelected(false);
        }

        for (VBox subBox : subcategoryContainers.values()) {
            subBox.setVisible(false);
            subBox.setManaged(false);
        }
        if (sortByComboBox != null) {
            sortByComboBox.setValue("Most Followers");
            currentSortOption = SortOption.MOST_FOLLOWERS;
        }
        applyFilters();
    }
    private void setActiveNavButton(Button activeButton) {
        resetNavButton(homeButton);
        resetNavButton(myThreadsButton);
        resetNavButton(followedButton);
        resetNavButton(archivedButton);
        resetNavButton(categoriesButton);

        if (activeButton != null) {
            activeButton.setStyle(
                    "-fx-background-color: #051261;" +  // Blue background
                            "-fx-text-fill: white;" +             // White text
                            "-fx-font-size: 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10;" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;"
            );
            currentActiveButton = activeButton;
        }
    }
    private void resetNavButton(Button button) {
        if (button != null) {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #333;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 10;" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;"
            );
        }
    }
    private enum SortOption {
        NEWEST_FIRST,
        OLDEST_FIRST,
        MOST_POINTS,
        LEAST_POINTS,
        MOST_COMMENTS,
        LEAST_COMMENTS,
        MOST_FOLLOWERS,
        LEAST_FOLLOWERS
    }
    private SortOption currentSortOption = SortOption.NEWEST_FIRST;
    @FXML
    private void onSortByChanged() {
        if (sortByComboBox == null || sortByComboBox.getValue() == null) {
            return;
        }

        String selected = sortByComboBox.getValue();

        // Map selection to sort option
        switch (selected) {
            case "Newest First":
                currentSortOption = SortOption.NEWEST_FIRST;
                break;
            case "Oldest First":
                currentSortOption = SortOption.OLDEST_FIRST;
                break;
            case "Most Points":
                currentSortOption = SortOption.MOST_POINTS;
                break;
            case "Least Points":
                currentSortOption = SortOption.LEAST_POINTS;
                break;
            case "Most Comments":
                currentSortOption = SortOption.MOST_COMMENTS;
                break;
            case "Least Comments":
                currentSortOption = SortOption.LEAST_COMMENTS;
                break;
            case "Most Followers":
                currentSortOption = SortOption.MOST_FOLLOWERS;
                break;
            case "Least Followers":
                currentSortOption = SortOption.LEAST_FOLLOWERS;
                break;
        }

        applyFilters();

        System.out.println("Sort changed to: " + selected);
    }
    private List<Thread> sortThreads(List<Thread> threads) {
        List<Thread> sorted = new ArrayList<>(threads);

        switch (currentSortOption) {
            case NEWEST_FIRST:
                sorted.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));
                break;

            case OLDEST_FIRST:
                sorted.sort((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()));
                break;

            case MOST_POINTS:
                sorted.sort((t1, t2) -> {
                    int points1 = t1.getLikecount() - t1.getDislikecount();
                    int points2 = t2.getLikecount() - t2.getDislikecount();
                    return Integer.compare(points2, points1);
                });
                break;

            case LEAST_POINTS:
                sorted.sort((t1, t2) -> {
                    int points1 = t1.getLikecount() - t1.getDislikecount();
                    int points2 = t2.getLikecount() - t2.getDislikecount();
                    return Integer.compare(points1, points2);
                });
                break;

            case MOST_COMMENTS:
                sorted.sort((t1, t2) ->Integer.compare(t2.getRepliescount(), t1.getRepliescount()));

                break;

            case LEAST_COMMENTS:
                sorted.sort((t1, t2) -> Integer.compare(t1.getRepliescount(), t2.getRepliescount()));
                break;

            case MOST_FOLLOWERS:
                sorted.sort((t1, t2) -> Integer.compare(t2.getFollowcount(), t1.getFollowcount()));
                break;

            case LEAST_FOLLOWERS:
                sorted.sort((t1, t2) -> Integer.compare(t1.getFollowcount(), t2.getFollowcount()));
                break;
        }

        return sorted;
    }
    private void updateNotificationBadge() {
        int unseenCount = notificationService.getUnseenCount(currentUserId);

        if (unseenCount > 0) {
            // Show badge
            if (notificationBadge != null) {
                notificationBadge.setText(String.valueOf(unseenCount));
                notificationBadge.setVisible(true);
                notificationBadge.setManaged(true);
            }

            // Highlight notification button
            if (notificationsButton != null) {
                notificationsButton.setStyle(
                        "-fx-background-color: #E3F2FD; " +
                                "-fx-text-fill: #333; " +
                                "-fx-font-size: 14; " +
                                "-fx-padding: 10; " +
                                "-fx-background-radius: 5; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-color: #2196F3; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 5;"
                );
            }
        } else {
            // Hide badge
            if (notificationBadge != null) {
                notificationBadge.setVisible(false);
                notificationBadge.setManaged(false);
            }

            // Normal button style
            if (notificationsButton != null) {
                notificationsButton.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #333; " +
                                "-fx-font-size: 14; " +
                                "-fx-padding: 10; " +
                                "-fx-background-radius: 5; " +
                                "-fx-cursor: hand;"
                );
            }
        }
    }

    /**
     * Handle notifications button click
     */
    @FXML
    private void onNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/NotificationsPanel.fxml")
            );
            overlayPane.setManaged(true);
            // Must call load() BEFORE getController()
            Parent notificationsPanel = loader.load();

            NotificationsPanelController controller = loader.getController();
            controller.setOnCloseCallback(this::updateNotificationBadge);

            // Look up the overlay StackPane in the scene
            StackPane overlay = (StackPane) notificationsButton.getScene().lookup("#overlayPane");

            if (overlay == null) {
                System.err.println("overlayPane not found in scene. Add a StackPane with fx:id=\"overlayPane\" to your main FXML.");
                return;
            }

            // Clear any existing popups and show this one
            overlay.getChildren().clear();
            overlay.getChildren().add(notificationsPanel);
            overlay.setVisible(true);


            notificationService.markAllAsSeen(currentUserId);
            updateNotificationBadge();

        } catch (IOException e) {
            System.err.println("Error loading notifications panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show notifications panel as popup overlay
     */
    private void showNotificationsPopup(Parent notificationsPanel) {
        // Get the root of the scene
        Node sceneRoot = notificationsButton.getScene().getRoot();

        if (sceneRoot instanceof StackPane) {
            StackPane root = (StackPane) sceneRoot;

            // Create semi-transparent background overlay
            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.setOnMouseClicked(event -> {
                // Close popup when clicking outside
                root.getChildren().remove(root.getChildren().size() - 1);
            });

            // Create container for the panel
            StackPane popupContainer = new StackPane();
            popupContainer.setAlignment(Pos.TOP_RIGHT);
            popupContainer.setStyle("-fx-padding: 80 20 20 20;");
            popupContainer.getChildren().add(notificationsPanel);
            popupContainer.setPickOnBounds(false);

            // Combine overlay and popup
            StackPane combined = new StackPane();
            combined.getChildren().addAll(overlay, popupContainer);

            // Add to root
            root.getChildren().add(combined);
        } else if (sceneRoot instanceof BorderPane) {
            // If root is BorderPane, wrap it in a StackPane first
            BorderPane borderPane = (BorderPane) sceneRoot;

            // We need to add overlay to the scene differently
            // Create a temporary overlay approach
            System.err.println("Root is BorderPane - notification popup may not display correctly");
            System.err.println("Consider wrapping your root in a StackPane in the FXML");
        }
    }
}
