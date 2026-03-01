package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Services.ServiceCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.List;

public class AddCategoryController {

    @FXML private TextField nameField;
    @FXML private TextField slugField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private Label headerLabel;
    @FXML private Button publishButton;

    private final ServiceCategory categoryService = new ServiceCategory();
    private int alert;
    private boolean isEditMode = false;
    private Category categoryToEdit = null;
    @FXML
    public void initialize() {
        setAddMode();
        loadCategories();
        listeners();
    }
    public void setAddMode() {
        isEditMode = false;
        categoryToEdit = null;

        if (headerLabel != null) {
            headerLabel.setText("Create New Category");
        }
        if (publishButton != null) {
            publishButton.setText("Publish Category");
        }

        clearForm();
    }
    public void setEditMode(Category category) {
        isEditMode = true;
        categoryToEdit = category;

        if (headerLabel != null) {
            headerLabel.setText("Edit Category");
        }
        if (publishButton != null) {
            publishButton.setText("Update Category");
        }

        fillFormWithCategoryData(category);
    }
    private void fillFormWithCategoryData(Category category) {
        nameField.setText(category.getName());
        slugField.setText(category.getSlug());

        descriptionArea.setText(category.getDescription());
        if(category.getParentId()!=null)
        {Category parentCategory = categoryService.getById(category.getParentId());
        categoryBox.setValue(parentCategory);
        } else{categoryBox.setValue(null);
        }

    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAll();

            categoryBox.getItems().clear();

            categoryBox.getItems().add(null);
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
                        setText(item.getSlug());
                    }
                }
            });

            if (!categories.isEmpty()) {
                categoryBox.setValue(null);
            }

        } catch (Exception e) {
            alert("Error loading categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void publish() {
        if (!validateForm()){ if(alert==1){showError(nameField, "Name is required");
            return;}
            if(alert==2){showError(nameField, "Name must be at least 3 characters");return;}
            if(alert==3){showError(nameField, "Name must contain only letters");return;}
            if(alert==4){showError(slugField, "Slug is required");return;}
            if(alert==5){showError(slugField, "Slug must be lowercase (a-z,-)");return;}
            if(alert==6){showError(descriptionArea, "Description is required");return;}
            if(alert==7){showError(descriptionArea, "Description too short");return;}
            if(alert==8){alert("Category cannot be its own parent!", Alert.AlertType.WARNING);return;}
    }
        try {
            if (isEditMode) {
                updateCategory();
            } else {
                createCategory();
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert("Error saving category: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createCategory() {
        Category category = new Category();
        category.setName(nameField.getText().trim());
        if(categoryService.getBySlug(slugField.getText().trim())==null){category.setSlug(slugField.getText().trim());}
        else{alert("Slug should be unique", Alert.AlertType.WARNING);
        return;}
        category.setDescription(descriptionArea.getText().trim());
        if (categoryBox.getValue() != null) {category.setParentId(categoryBox.getValue().getId());}

        System.out.println("Creating category: " + category.getSlug());

        categoryService.add(category);

        System.out.println("Category created successfully!");

        alert("Category created successfully! ✅", Alert.AlertType.INFORMATION);

        returnToForum();
    }

    private void updateCategory() {
        if (categoryToEdit == null) {
            alert("Error: No category to update!", Alert.AlertType.ERROR);
            return;
        }

        categoryToEdit.setName(nameField.getText().trim());
        if(categoryService.getBySlug(slugField.getText().trim())==null || (categoryService.getBySlug(slugField.getText().trim())).getId()==categoryToEdit.getId() )
        {categoryToEdit.setSlug(slugField.getText().trim());}
        else{alert("Slug should be unique", Alert.AlertType.WARNING);return;}
        categoryToEdit.setDescription(descriptionArea.getText().trim());
        if (categoryBox.getValue() != null){categoryToEdit.setParentId(categoryBox.getValue().getId());}
        else{categoryToEdit.setParentId(null);}


        System.out.println("Updating thread ID: " + categoryToEdit.getId());

        categoryService.update(categoryToEdit);

        System.out.println("Category updated successfully!");

        alert("Category updated successfully! ✅", Alert.AlertType.INFORMATION);

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forum/ForumBackoffice.fxml"));
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
        if (nameField != null && nameField.getScene() != null) {
            javafx.scene.Node node = nameField.getScene().getRoot();
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
        nameField.clear();
        descriptionArea.clear();
        categoryBox.setValue(null);
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
        String name = nameField.getText().trim();
        String slug = slugField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty()) {
            alert=1;
            return false;
        }
        if (name.length() < 3) {
            alert=2;
            return false;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            alert=3;
            return false;
        }

        if (slug.isEmpty()) {
            alert=4;
            return false;
        }
        if (!slug.matches("^[a-z-]+$")) {
            alert=5;
            return false;
        }

        if (description.isEmpty()) {
            alert=6;
            return false;
        }
        if (description.length() < 10) {
            alert=7;
            return false;
        }

        if (isEditMode && categoryBox.getValue() != null) {
            if (categoryBox.getValue().getId() == categoryToEdit.getId()) {
                alert=8;
                return false;
            }
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
       nameField.textProperty().addListener((obs, oldVal, newVal) -> {
           String slug = newVal.toLowerCase()
                   .replaceAll("[^a-z0-9 ]", "")
                   .replaceAll("\\s+", "-");
           slugField.setText(slug);
       });
       slugField.textProperty().addListener((obs, oldVal, newVal) -> {
           if (newVal.isBlank()) return;

           Category existing = categoryService.getBySlug(newVal);

           if (existing != null && (!isEditMode || existing.getId() != categoryToEdit.getId())) {
               slugField.setStyle("-fx-border-color: orange;");
           } else {
               slugField.setStyle(null);
           }
       });

       descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> clearError(descriptionArea));
       publishButton.disableProperty().bind(
               nameField.textProperty().isEmpty()
                       .or(slugField.textProperty().isEmpty())
                       .or(descriptionArea.textProperty().isEmpty())
       );
   }
}