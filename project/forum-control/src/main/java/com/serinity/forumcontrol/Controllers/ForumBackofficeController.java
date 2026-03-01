package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Services.ServiceCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ForumBackofficeController {

    @FXML private GridPane categoriesGrid;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox emptyStateBox;
    @FXML private Button statisticsButton;
    @FXML private Button addCategoryButton;
    @FXML private Label totalCategoriesLabel;
    @FXML private Label parentCategoriesLabel;
    @FXML private Label subCategoriesLabel;

    private ServiceCategory categoryService = new ServiceCategory();

    @FXML
    public void initialize() {
        loadCategories();
        updateCategoryStats();
    }

    private void loadCategories() {
        categoriesGrid.getChildren().clear();

        List<Category> categories = categoryService.getAll();

        if (categories.isEmpty()) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            categoriesGrid.setVisible(false);
        } else {
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);
            categoriesGrid.setVisible(true);

            int row = 0;
            int col = 0;

            for (Category category : categories) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/forum/CategoryCard.fxml")
                    );
                    Node categoryCard = loader.load();

                    CategoryCardController controller = loader.getController();
                    controller.setData(category);

                    categoriesGrid.add(categoryCard, col, row);

                    GridPane.setFillWidth(categoryCard, true);
                    categoryCard.setStyle(categoryCard.getStyle() +
                            "-fx-min-width: 300; -fx-pref-width: 800; -fx-max-width: 800;");

                    // Move to next position
                    col++;
                    if (col >= 2) {
                        col = 0;
                        row++;
                    }

                } catch (IOException e) {
                    System.err.println("Error loading category card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    private void updateCategoryStats() {
        List<Category> categories = categoryService.getAll();

        int total = categories.size();
        int parents = 0;
        int subs = 0;

        for (Category category : categories) {
            if (category.getParentId() == null) {
                parents++;
            } else {
                subs++;
            }
        }

        totalCategoriesLabel.setText(String.valueOf(total));
        parentCategoriesLabel.setText(String.valueOf(parents));
        subCategoriesLabel.setText(String.valueOf(subs));
    }

    @FXML
    private void onAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/AddCategory.fxml")
            );
            Parent addCategoryView = loader.load();

            AddCategoryController controller = loader.getController();
            controller.setAddMode();

            BorderPane root = findBorderPane();
            if (root != null) {
                root.setCenter(addCategoryView);
            }

        } catch (IOException e) {
            System.err.println("Error loading AddCategory view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open Add Category page.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/StatisticsView.fxml")
            );
            Parent statisticsView = loader.load();

            BorderPane root = findBorderPane();
            if (root != null) {
                root.setCenter(statisticsView);
            }

        } catch (IOException e) {
            System.err.println("Error loading Statistics view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open Statistics page.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRefresh() {
        loadCategories();
        updateCategoryStats();
        System.out.println("Categories refreshed!");
    }


    private BorderPane findBorderPane() {
        if (categoriesGrid != null && categoriesGrid.getScene() != null) {
            javafx.scene.Node node = categoriesGrid.getScene().getRoot();
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}