package com.serinity.forumcontrol.controller;

import com.serinity.forumcontrol.model.Category;
import com.serinity.forumcontrol.service.ServiceCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

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
                            getClass().getResource("/fxml/CategoryCard.fxml")
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
                    getClass().getResource("/fxml/AddCategory.fxml")
            );
            Parent addCategoryView = loader.load();

            AddCategoryController controller = loader.getController();
            controller.setAddMode();

            // Replace this view's content inside its parent
            Parent thisView = categoriesGrid.getScene().getRoot();
            javafx.scene.Node node = categoriesGrid;
            while (node.getParent() != null) {
                node = node.getParent();
            }
            // Swap inside the immediate parent container
            replaceInParent(addCategoryView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Category page.", Alert.AlertType.ERROR);
        }
    }

    private void replaceInParent(Parent newView) {
        javafx.scene.Node thisRoot = categoriesGrid;

        // Walk up until we find a node whose PARENT is a StackPane
        while (thisRoot.getParent() != null) {
            if (thisRoot.getParent() instanceof StackPane) break;
            if (thisRoot.getParent() instanceof Pane) break; // fallback
            thisRoot = thisRoot.getParent();
        }

        if (thisRoot.getParent() == null) {
            System.err.println("Could not find StackPane or Pane parent");
            showAlert("Error", "Could not navigate.", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("Found parent: " + thisRoot.getParent().getClass().getName());

        if (thisRoot.getParent() instanceof StackPane sp) {
            if (newView instanceof Region r) {
                r.prefWidthProperty().bind(sp.widthProperty());
                r.prefHeightProperty().bind(sp.heightProperty());
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
                StackPane.setAlignment(r, javafx.geometry.Pos.TOP_LEFT);
            }
            sp.getChildren().setAll(newView);
        } else if (thisRoot.getParent() instanceof Pane pane) {
            if (newView instanceof Region r) {
                r.prefWidthProperty().bind(pane.widthProperty());
                r.prefHeightProperty().bind(pane.heightProperty());
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            int idx = pane.getChildren().indexOf(thisRoot);
            pane.getChildren().set(idx, newView);
        }
    }
    @FXML
    private void onStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/StatisticsView.fxml")
            );
            Parent statisticsView = loader.load();

            replaceInParent(statisticsView);

        } catch (IOException e) {
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




    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
