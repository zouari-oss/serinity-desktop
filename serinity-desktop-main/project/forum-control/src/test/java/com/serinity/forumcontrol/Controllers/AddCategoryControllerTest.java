package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.Category;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddCategoryControllerTest {

    private AddCategoryController controller;

    // Initialize JavaFX Toolkit once
    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @BeforeEach
    void setup() throws Exception {

        controller = new AddCategoryController();

        inject("nameField", new TextField());
        inject("slugField", new TextField());
        inject("descriptionArea", new TextArea());
        inject("categoryBox", new ComboBox<Category>());
        inject("headerLabel", new Label());
        inject("publishButton", new Button());
        controller.initialize();
    }

    // ================= HELPER METHODS =================

    private void inject(String fieldName, Object value) throws Exception {
        Field field = AddCategoryController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private boolean validate() throws Exception {
        Method method = AddCategoryController.class
                .getDeclaredMethod("validateForm");
        method.setAccessible(true);
        return (boolean) method.invoke(controller);
    }

    // ================= NAME VALIDATION =================

    @Test
    @Order(1)
    @DisplayName("Should fail when name is empty")
    void testNameEmpty() throws Exception {

        ((TextField) getField("nameField")).setText("");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        assertFalse(validate());
    }

    @Test
    @Order(2)
    @DisplayName("Should fail when name is too short")
    void testNameTooShort() throws Exception {

        ((TextField) getField("nameField")).setText("ab");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        assertFalse(validate());
    }

    @Test
    @Order(3)
    @DisplayName("Should fail when name contains invalid characters")
    void testInvalidNameCharacters() throws Exception {

        ((TextField) getField("nameField")).setText("Category123");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        assertFalse(validate());
    }

    // ================= SLUG VALIDATION =================

    @Test
    @Order(4)
    @DisplayName("Should fail when slug is empty")
    void testSlugEmpty() throws Exception {

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        assertFalse(validate());
    }

    @Test
    @Order(5)
    @DisplayName("Should fail when slug contains uppercase or invalid characters")
    void testInvalidSlug() throws Exception {

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("InvalidSlug!");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        assertFalse(validate());
    }

    // ================= DESCRIPTION VALIDATION =================

    @Test
    @Order(6)
    @DisplayName("Should fail when description is empty")
    void testDescriptionEmpty() throws Exception {

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea")).setText("");

        assertFalse(validate());
    }

    @Test
    @Order(7)
    @DisplayName("Should fail when description is too short")
    void testDescriptionTooShort() throws Exception {

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea")).setText("short");

        assertFalse(validate());
    }

    // ================= SELF-PARENT VALIDATION =================

    @Test
    @Order(8)
    @DisplayName("Should fail when category is its own parent in edit mode")
    void testOwnParentValidation() throws Exception {

        Category category = new Category();
        category.setId(1L);

        controller.setEditMode(category);

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea"))
                .setText("Valid description here");

        ComboBox<Category> box =
                (ComboBox<Category>) getField("categoryBox");

        box.getItems().add(category);
        box.setValue(category);

        assertFalse(validate());
    }

    // ================= SUCCESS CASE =================

    @Test
    @Order(9)
    @DisplayName("Should pass when all inputs are valid")
    void testValidForm() throws Exception {

        ((TextField) getField("nameField")).setText("Valid Name");
        ((TextField) getField("slugField")).setText("valid-slug");
        ((TextArea) getField("descriptionArea"))
                .setText("This is a valid description.");

        assertTrue(validate());
    }

    // ================= UTILITY =================

    private Object getField(String fieldName) throws Exception {
        Field field = AddCategoryController.class
                .getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }
}
