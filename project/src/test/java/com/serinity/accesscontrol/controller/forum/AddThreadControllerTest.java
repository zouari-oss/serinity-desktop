package com.serinity.accesscontrol.controller.forum;

import com.serinity.accesscontrol.controller.LoginController;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.model.forum.Category;
import com.serinity.accesscontrol.model.forum.ThreadType;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddThreadControllerTest {

    private AddThreadController controller;

    // Initialize JavaFX Toolkit once
    @BeforeAll
    static void initToolkit() throws Exception {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
        // Inject a mock therapist user so checkAdminAndSetPinnedVisibility() returns true
        User mockUser = new User();
        Field idField = mockUser.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockUser, UUID.fromString("00000000-0000-0000-0000-000000000001"));
        mockUser.setRole(UserRole.THERAPIST);
        Field userField = LoginController.class.getDeclaredField("user");
        userField.setAccessible(true);
        userField.set(null, mockUser);
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new AddThreadController();

        // Inject all required UI fields
        inject("titleField", new TextField());
        inject("contentArea", new TextArea());
        inject("categoryBox", new ComboBox<Category>());
        inject("typeBox", new ComboBox<ThreadType>());
        inject("pinnedCheck", new CheckBox());
        inject("headerLabel", new Label());
        inject("publishButton", new Button());
        inject("pinnedContainer", new HBox());


        controller.initialize();
    }

    // ================= HELPER METHODS =================

    private void inject(String fieldName, Object value) throws Exception {
        Field field = AddThreadController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private boolean validate() throws Exception {
        Method method = AddThreadController.class.getDeclaredMethod("validateForm");
        method.setAccessible(true);
        return (boolean) method.invoke(controller);
    }

    private void setTitle(String text) throws Exception {
        ((TextField) getField("titleField")).setText(text);
    }

    private void setContent(String text) throws Exception {
        ((TextArea) getField("contentArea")).setText(text);
    }

    private void setCategory(Category category) throws Exception {
        ComboBox<Category> box = (ComboBox<Category>) getField("categoryBox");
        box.getItems().clear();
        box.getItems().add(category);
        box.setValue(category);
    }

    private void setThreadType(ThreadType type) throws Exception {
        ComboBox<ThreadType> box = (ComboBox<ThreadType>) getField("typeBox");
        box.getItems().clear();
        box.getItems().add(type);
        box.setValue(type);
    }

    private Object getField(String fieldName) throws Exception {
        Field field = AddThreadController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }

    // ================= TITLE VALIDATION =================

    @Test
    @Order(1)
    @DisplayName("Should fail when title is empty")
    void testTitleEmpty() throws Exception {
        setTitle("");
        setContent("Valid content with enough length.");
        setCategory(new Category());
        setThreadType(ThreadType.DISCUSSION);

        assertFalse(validate());
    }

    @Test
    @Order(2)
    @DisplayName("Should fail when title is too short")
    void testTitleTooShort() throws Exception {
        setTitle("abc");
        setContent("Valid content with enough length.");
        setCategory(new Category());
        setThreadType(ThreadType.DISCUSSION);

        assertFalse(validate());
    }

    // ================= CONTENT VALIDATION =================

    @Test
    @Order(3)
    @DisplayName("Should fail when content is empty")
    void testContentEmpty() throws Exception {
        setTitle("Valid Title");
        setContent("");
        setCategory(new Category());
        setThreadType(ThreadType.DISCUSSION);

        assertFalse(validate());
    }

    @Test
    @Order(4)
    @DisplayName("Should fail when content is too short")
    void testContentTooShort() throws Exception {
        setTitle("Valid Title");
        setContent("Too short");
        setCategory(new Category());
        setThreadType(ThreadType.DISCUSSION);

        assertFalse(validate());
    }
    // ================= SUCCESS CASE =================

    @Test
    @Order(5)
    @DisplayName("Should pass when all inputs are valid")
    void testValidForm() throws Exception {
        setTitle("Valid Thread Title");
        setContent("This is valid content with more than twenty characters.");
        setCategory(new Category());
        setThreadType(ThreadType.DISCUSSION);

        assertTrue(validate());
    }

    // ================= PINNED CHECK VISIBILITY =================

    @Test
    @Order(6)
    @DisplayName("Pinned checkbox should be visible for admin")
    void testPinnedVisibilityForAdmin() throws Exception {
        HBox container = (HBox) getField("pinnedContainer");
        CheckBox pinned = (CheckBox) getField("pinnedCheck");

        // user is admin by default in setup
        controller.initialize();

        assertTrue(pinned.isVisible());
        assertTrue(container.isVisible());
    }
}
