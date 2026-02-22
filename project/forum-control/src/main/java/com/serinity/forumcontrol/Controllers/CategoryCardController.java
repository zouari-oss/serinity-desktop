package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Services.ServiceCategory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class CategoryCardController {
    @FXML
    private VBox rootCard;
    @FXML
    private Label titleLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private Label lfoukLabel;
    @FXML
    private MenuButton menuButton;
    private ServiceCategory service = new ServiceCategory();
    private Category category;
    private Runnable onRefreshCallback;
    public void setData(Category c) {
        this.category= c;
        String parent;
        if(c.getParentId()==null){
            parent="none";
        }else parent=(service.getById(c.getParentId())).getSlug();
        String lfouk =
                " • Parent:" + parent ;
        String title =
                " • Name: " + c.getName() +
                "           • Slug: " + c.getSlug();
        titleLabel.setText(title);
        metaLabel.setText(c.getDescription());
        lfoukLabel.setText(lfouk);
        buildMenu();
    }

    private void buildMenu() {
        MenuItem editItem = new MenuItem("✏️ Edit");
        editItem.setOnAction(this::handleEdit);

        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(this::handleDelete);

        menuButton.getItems().addAll(editItem, deleteItem);
    }

    private void handleDelete(ActionEvent event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Category");
        confirmDialog.setHeaderText("Are you sure you want to delete this Category?");
        confirmDialog.setContentText(
                "Thread: \"" + category.getSlug() + "\"\n\n" +
                        "This will permanently delete:\n" +
                        "• The category\n" +
                        "• All SubCategories \n\n" +
                        "⚠️ This action cannot be undone!"
        );
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == deleteButton) {
            try {
                try {
                    ServiceCategory categoryService = new ServiceCategory();
                    categoryService.deleteByParent(category.getId());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete subcategories: " + e.getMessage());
                }

                service.delete(category);

                showAlert("Success",
                        "Category \"" + category.getSlug() + "\" has been deleted successfully!",
                        Alert.AlertType.INFORMATION);
                refreshCategoryList();

            } catch (Exception e) {
                System.err.println("Error deleting category: " + e.getMessage());
                e.printStackTrace();

                showAlert("Error",
                        "Failed to delete category.\n\n" +
                                "Error: " + e.getMessage() + "\n\n" +
                                "Please try again or contact support if the problem persists.",
                        Alert.AlertType.ERROR);
            }
        } else {
        }
    }


    private void handleEdit(ActionEvent event) {
        try {
            System.out.println("Edit category: " + category.getId());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/AddCategory.fxml"));
            Parent editView = loader.load();

            AddCategoryController controller = loader.getController();
            controller.setEditMode(category);

            System.out.println("Edit view loaded with category data");

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

    public void handleMenuClick(MouseEvent event) {
        event.consume();
    }
    private void refreshCategoryList() {
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
}