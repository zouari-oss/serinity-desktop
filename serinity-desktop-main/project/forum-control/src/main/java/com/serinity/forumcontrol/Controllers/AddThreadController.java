package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.HardcodedUser.FakeUser;
import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Models.ThreadType;
import com.serinity.forumcontrol.Models.ThreadStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Services.ServiceThread;
import com.serinity.forumcontrol.Services.ServiceCategory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

public class AddThreadController {

    public HBox pinnedContainer;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private ComboBox<ThreadType> typeBox;
    @FXML private CheckBox pinnedCheck;
    @FXML private Label headerLabel;
    @FXML private Button publishButton;

    private FakeUser user;
    private final ServiceThread threadService = new ServiceThread();
    private final ServiceCategory categoryService = new ServiceCategory();

    private boolean isEditMode = false;
    private Thread threadToEdit = null;
    @FXML
    public void initialize() {
        setAddMode();

        loadCategories();

        loadThreadTypes();
        listeners();

        typeBox.setValue(ThreadType.DISCUSSION);
        checkAdminAndSetPinnedVisibility();
    }
    public void setAddMode() {
        isEditMode = false;
        threadToEdit = null;

        if (headerLabel != null) {
            headerLabel.setText("Create New Thread");
        }
        if (publishButton != null) {
            publishButton.setText("Publish Thread");
        }

        clearForm();
    }
    public void setEditMode(Thread thread) {
        isEditMode = true;
        threadToEdit = thread;

        if (headerLabel != null) {
            headerLabel.setText("Edit Thread");
        }
        if (publishButton != null) {
            publishButton.setText("Update Thread");
        }

        fillFormWithThreadData(thread);
    }
    private void fillFormWithThreadData(Thread thread) {
        titleField.setText(thread.getTitle());

        contentArea.setText(thread.getContent());

        Category threadCategory = categoryService.getById(thread.getCategoryId());
        if (threadCategory != null) {
            categoryBox.setValue(threadCategory);
        }

        typeBox.setValue(thread.getType());

        if (pinnedCheck != null && pinnedCheck.isVisible()) {
            pinnedCheck.setSelected(thread.isPinned());
        }
    }
    private void checkAdminAndSetPinnedVisibility() {
        String currentUserId = user.getCurrentUserId();
        if (threadService.isAdmin(currentUserId)) {

            pinnedCheck.setVisible(true);
            pinnedCheck.setManaged(true);
            pinnedContainer.setVisible(true);
            pinnedContainer.setManaged(true);
        } else {
            pinnedCheck.setVisible(false);
            pinnedCheck.setManaged(false);
            pinnedCheck.setSelected(false);
            pinnedContainer.setVisible(false);
            pinnedContainer.setManaged(false);
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAll();

            categoryBox.getItems().clear();

            categoryBox.getItems().addAll(categories);

            categoryBox.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        if (item.getParentId() != null) {
                            setText("  └─ " + item.getSlug());
                        } else {
                            setText(item.getSlug());
                        }
                    }
                }
            });

            categoryBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });

            if (!categories.isEmpty()) {
                categoryBox.setValue(categories.get(0));
            }

        } catch (Exception e) {
            alert("Error loading categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void loadThreadTypes() {
        typeBox.getItems().clear();
        typeBox.getItems().addAll(
                ThreadType.DISCUSSION,
                ThreadType.QUESTION,
                ThreadType.ANNOUNCEMENT
        );

        typeBox.setCellFactory(param -> new ListCell<ThreadType>() {
            @Override
            protected void updateItem(ThreadType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Capitalize first letter
                    String display = item.getValue().substring(0, 1).toUpperCase() +
                            item.getValue().substring(1);
                    setText(display);
                }
            }
        });

        typeBox.setButtonCell(new ListCell<ThreadType>() {
            @Override
            protected void updateItem(ThreadType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String display = item.getValue().substring(0, 1).toUpperCase() +
                            item.getValue().substring(1);
                    setText(display);
                }
            }
        });
    }

    @FXML
    private void publish() {
        if (!validateForm()) return;

        try {
            if (isEditMode) {
                updateThread();
            } else {
                createThread();
            }

        } catch (Exception e) {
            e.printStackTrace();
            alert("Error saving thread: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createThread() {
        Thread thread = new Thread();
        thread.setTitle(titleField.getText().trim());
        thread.setContent(contentArea.getText().trim());
        thread.setCategoryId(categoryBox.getValue().getId());
        thread.setType(typeBox.getValue());
        thread.setStatus(ThreadStatus.OPEN);


        boolean isPinned = false;
        if (pinnedCheck != null && pinnedCheck.isVisible() && pinnedCheck.isSelected()) {
            isPinned = true;
        }
        thread.setPinned(isPinned);

        thread.setUserId(user.getCurrentUserId());

        System.out.println("Creating thread: " + thread.getTitle());

        threadService.add(thread);

        System.out.println("Thread created successfully!");

        alert("Thread created successfully! ✅", Alert.AlertType.INFORMATION);

        returnToForum();
    }

    private void updateThread() {
        if (threadToEdit == null) {
            alert("Error: No thread to update!", Alert.AlertType.ERROR);
            return;
        }

        threadToEdit.setTitle(titleField.getText().trim());
        threadToEdit.setContent(contentArea.getText().trim());
        threadToEdit.setCategoryId(categoryBox.getValue().getId());
        threadToEdit.setType(typeBox.getValue());

        if (pinnedCheck != null && pinnedCheck.isVisible()) {
            threadToEdit.setPinned(pinnedCheck.isSelected());
        }

        System.out.println("Updating thread ID: " + threadToEdit.getId());

        threadService.update(threadToEdit);

        System.out.println("Thread updated successfully!");

        alert("Thread updated successfully! ✅", Alert.AlertType.INFORMATION);

        returnToForum();
    }

    @FXML
    private void cancel() {
        returnToForum();
    }

    @FXML
    private void refreshCategories() {
        loadCategories();
        alert("Categories refreshed!", Alert.AlertType.INFORMATION);
    }

    private void returnToForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forum/ForumPostsView.fxml"));
            Parent forumView = loader.load();

            BorderPane root = findBorderPane();
            if (root != null) {
                root.setCenter(forumView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            alert("Error returning to forum: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private BorderPane findBorderPane() {
        if (titleField != null && titleField.getScene() != null) {
            javafx.scene.Node node = titleField.getScene().getRoot();
            if (node instanceof BorderPane) {
                return (BorderPane) node;
            }
            while (node != null) {
                if (node instanceof BorderPane) {
                    return (BorderPane) node;
                }
                node = node.getParent();
            }
        }
        return null;
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        categoryBox.setValue(null);
        typeBox.setValue(ThreadType.DISCUSSION);  // Reset to default
        pinnedCheck.setSelected(false);
    }

    private void alert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.setHeaderText(null);
        alert.show();
    }

    private void alert(String msg) {
        alert(msg, Alert.AlertType.INFORMATION);
    }
    private boolean validateForm() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty()) {
            showError(titleField, "Title is required");
            return false;
        }
        if (title.length() < 5) {
            showError(titleField, "Title must be at least 5 characters");
            return false;
        }

        if (content.isEmpty()) {
            showError(contentArea, "Content is required");
            return false;
        }
        if (content.length() < 20) {
            showError(contentArea, "Content too short (min 20 chars)");
            return false;
        }

        if (categoryBox.getValue() == null) {
            alert("Please select a category!", Alert.AlertType.WARNING);
            return false;
        }

        if (typeBox.getValue() == null) {
            alert("Please select a thread type!", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }
    private void showError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        alert(message, Alert.AlertType.WARNING);
    }

    private void clearError(Control field) {
        field.setStyle(null);
    }
   private void listeners(){
       titleField.focusedProperty().addListener((obs, oldVal, isFocused) -> {
           if (!isFocused) {
               titleField.setText(titleField.getText().trim());
           }
       });
       publishButton.disableProperty().bind(
               titleField.textProperty().isEmpty()
                       .or(contentArea.textProperty().isEmpty())
                       .or(categoryBox.valueProperty().isNull())
                       .or(typeBox.valueProperty().isNull())
       );
       contentArea.textProperty().addListener((obs, oldVal, newVal) -> clearError(contentArea));

   }
}