package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.Category;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ServiceCategory
 * Tests CRUD operations, parent-child relationships, and edge cases
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceCategoryTest {

    private ServiceCategory service;
    private Long rootCategoryId;
    private Long subcategoryId;
    private Long secondRootCategoryId;

    @BeforeAll
    void setup() {
        service = new ServiceCategory();
        System.out.println("=== Starting ServiceCategory Tests ===");
    }

    @AfterAll
    void cleanup() {
        System.out.println("=== ServiceCategory Tests Completed ===");
    }

    // ========== CREATE TESTS ==========

    @Test
    @Order(1)
    @DisplayName("Should create root category successfully")
    void testAddRootCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Test Root Category");
        category.setSlug("test-root");
        category.setDescription("Root category for testing");
        category.setParentId(null);

        // Act
        service.add(category);
        Category saved = service.getBySlug("test-root");

        // Assert
        assertNotNull(saved, "Category should be saved");
        assertEquals("Test Root Category", saved.getName());
        assertEquals("test-root", saved.getSlug());
        assertEquals("Root category for testing", saved.getDescription());
        assertNull(saved.getParentId(), "Root category should have null parent");

        rootCategoryId = saved.getId();
        System.out.println("✓ Root category created with ID: " + rootCategoryId);
    }

    @Test
    @Order(2)
    @DisplayName("Should create subcategory successfully")
    void testAddSubcategory() {
        // Arrange
        Category subcategory = new Category();
        subcategory.setName("Test Subcategory");
        subcategory.setSlug("test-subcategory");
        subcategory.setDescription("Subcategory for testing");
        subcategory.setParentId(rootCategoryId);

        // Act
        service.add(subcategory);
        Category saved = service.getBySlug("test-subcategory");

        // Assert
        assertNotNull(saved, "Subcategory should be saved");
        assertEquals("Test Subcategory", saved.getName());
        assertEquals(rootCategoryId, saved.getParentId());

        subcategoryId = saved.getId();
        System.out.println("✓ Subcategory created with ID: " + subcategoryId);
    }

    @Test
    @Order(3)
    @DisplayName("Should prevent duplicate slug creation")
    void testAddDuplicateSlug() {
        // Arrange
        Category duplicate = new Category();
        duplicate.setName("Duplicate Category");
        duplicate.setSlug("test-root"); // Same slug as existing
        duplicate.setDescription("Should not be created");
        duplicate.setParentId(null);

        // Act
        service.add(duplicate);
        List<Category> all = service.getAll();
        long count = all.stream()
                .filter(c -> "test-root".equals(c.getSlug()))
                .count();

        // Assert
        assertEquals(1, count, "Should only have one category with this slug");
        System.out.println("✓ Duplicate slug prevented");
    }

    @Test
    @Order(4)
    @DisplayName("Should create second root category")
    void testAddSecondRootCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Second Root");
        category.setSlug("second-root");
        category.setDescription("Another root category");
        category.setParentId(null);

        // Act
        service.add(category);
        Category saved = service.getBySlug("second-root");

        // Assert
        assertNotNull(saved);
        secondRootCategoryId = saved.getId();
        System.out.println("✓ Second root category created with ID: " + secondRootCategoryId);
    }

    // ========== READ TESTS ==========

    @Test
    @Order(5)
    @DisplayName("Should retrieve category by ID")
    void testGetById() {
        // Act
        Category category = service.getById(rootCategoryId);

        // Assert
        assertNotNull(category, "Category should be found");
        assertEquals(rootCategoryId, category.getId());
        assertEquals("Test Root Category", category.getName());
        assertEquals("test-root", category.getSlug());

        System.out.println("✓ Retrieved category by ID: " + category.getName());
    }

    @Test
    @Order(6)
    @DisplayName("Should return null for non-existent ID")
    void testGetByIdNonExistent() {
        // Act
        Category category = service.getById(999999L);

        // Assert
        assertNull(category, "Should return null for non-existent ID");
        System.out.println("✓ Correctly returned null for non-existent ID");
    }

    @Test
    @Order(7)
    @DisplayName("Should retrieve category by slug")
    void testGetBySlug() {
        // Act
        Category category = service.getBySlug("test-root");

        // Assert
        assertNotNull(category, "Category should be found");
        assertEquals("Test Root Category", category.getName());
        assertEquals(rootCategoryId, category.getId());

        System.out.println("✓ Retrieved category by slug");
    }

    @Test
    @Order(8)
    @DisplayName("Should return null for non-existent slug")
    void testGetBySlugNonExistent() {
        // Act
        Category category = service.getBySlug("non-existent-slug");

        // Assert
        assertNull(category, "Should return null for non-existent slug");
        System.out.println("✓ Correctly returned null for non-existent slug");
    }

    @Test
    @Order(9)
    @DisplayName("Should retrieve all categories")
    void testGetAll() {
        // Act
        List<Category> categories = service.getAll();

        // Assert
        assertNotNull(categories, "Should return a list");
        assertTrue(categories.size() >= 3, "Should have at least 3 categories");

        long rootCount = categories.stream()
                .filter(c -> c.getParentId() == null)
                .count();
        long subCount = categories.stream()
                .filter(c -> c.getParentId() != null)
                .count();

        System.out.println("✓ Retrieved all categories: " + categories.size() +
                " total (" + rootCount + " root, " + subCount + " sub)");
    }

    @Test
    @Order(10)
    @DisplayName("Should retrieve root categories only")
    void testGetRootCategories() {
        // Act
        List<Category> rootCategories = service.getRootCategories();

        // Assert
        assertNotNull(rootCategories, "Should return a list");
        assertTrue(rootCategories.size() >= 2, "Should have at least 2 root categories");
        assertTrue(rootCategories.stream().allMatch(c -> c.getParentId() == null),
                "All categories should be root (parent_id = null)");

        System.out.println("✓ Retrieved root categories: " + rootCategories.size());
    }

    @Test
    @Order(11)
    @DisplayName("Should retrieve subcategories of parent")
    void testGetSubcategories() {
        // Act
        List<Category> subcategories = service.getSubcategories(rootCategoryId);

        // Assert
        assertNotNull(subcategories, "Should return a list");
        assertTrue(subcategories.size() >= 1, "Should have at least 1 subcategory");
        assertTrue(subcategories.stream().allMatch(c -> rootCategoryId.equals(c.getParentId())),
                "All categories should have correct parent ID");

        System.out.println("✓ Retrieved subcategories: " + subcategories.size());
    }

    @Test
    @Order(12)
    @DisplayName("Should return empty list for category with no subcategories")
    void testGetSubcategoriesEmpty() {
        // Act
        List<Category> subcategories = service.getSubcategories(secondRootCategoryId);

        // Assert
        assertNotNull(subcategories, "Should return a list");
        assertTrue(subcategories.isEmpty(), "Should be empty for category with no children");

        System.out.println("✓ Correctly returned empty list for category with no subcategories");
    }

    // ========== UPDATE TESTS ==========

    @Test
    @Order(13)
    @DisplayName("Should update category name")
    void testUpdateCategoryName() {
        // Arrange
        Category category = service.getById(rootCategoryId);
        String originalName = category.getName();
        category.setName("Updated Root Category");

        // Act
        service.update(category);
        Category updated = service.getById(rootCategoryId);

        // Assert
        assertNotNull(updated);
        assertEquals("Updated Root Category", updated.getName());
        assertEquals("test-root", updated.getSlug(), "Slug should remain unchanged");

        System.out.println("✓ Updated category name: " + originalName + " → " + updated.getName());
    }

    @Test
    @Order(14)
    @DisplayName("Should update category description")
    void testUpdateCategoryDescription() {
        // Arrange
        Category category = service.getById(rootCategoryId);
        category.setDescription("Updated description for testing");

        // Act
        service.update(category);
        Category updated = service.getById(rootCategoryId);

        // Assert
        assertEquals("Updated description for testing", updated.getDescription());
        System.out.println("✓ Updated category description");
    }

    @Test
    @Order(15)
    @DisplayName("Should update category slug")
    void testUpdateCategorySlug() {
        // Arrange
        Category category = service.getById(rootCategoryId);
        String oldSlug = category.getSlug();
        category.setSlug("updated-test-root");

        // Act
        service.update(category);
        Category updated = service.getById(rootCategoryId);

        // Assert
        assertEquals("updated-test-root", updated.getSlug());
        assertNull(service.getBySlug(oldSlug), "Old slug should not exist");

        System.out.println("✓ Updated category slug: " + oldSlug + " → " + updated.getSlug());
    }

    @Test
    @Order(16)
    @DisplayName("Should prevent updating to duplicate slug")
    void testUpdateToDuplicateSlug() {
        // Arrange
        Category category = service.getById(rootCategoryId);
        String originalSlug = category.getSlug();
        category.setSlug("second-root"); // Try to use existing slug

        // Act
        service.update(category);
        Category result = service.getById(rootCategoryId);

        // Assert
        assertEquals(originalSlug, result.getSlug(),
                "Slug should not change when attempting duplicate");

        System.out.println("✓ Prevented duplicate slug on update");
    }

    @Test
    @Order(17)
    @DisplayName("Should update parent relationship")
    void testUpdateParentRelationship() {
        // Arrange
        Category category = service.getById(subcategoryId);
        assertNotNull(category.getParentId(), "Should initially have a parent");

        category.setParentId(secondRootCategoryId); // Change parent

        // Act
        service.update(category);
        Category updated = service.getById(subcategoryId);

        // Assert
        assertEquals(secondRootCategoryId, updated.getParentId());

        System.out.println("✓ Updated parent relationship");
    }


    // ========== DELETE TESTS ==========

    @Test
    @Order(18)
    @DisplayName("Should create test subcategories for deletion test")
    void setupDeleteTest() {
        // Create a parent with multiple children for deletion test
        Category parent = new Category();
        parent.setName("Parent to Delete");
        parent.setSlug("parent-to-delete");
        parent.setDescription("Will be deleted");
        parent.setParentId(null);
        service.add(parent);

        Category savedParent = service.getBySlug("parent-to-delete");
        assertNotNull(savedParent);

        // Create 2 subcategories
        for (int i = 1; i <= 2; i++) {
            Category child = new Category();
            child.setName("Child " + i);
            child.setSlug("child-" + i);
            child.setDescription("Child category " + i);
            child.setParentId(savedParent.getId());
            service.add(child);
        }

        List<Category> children = service.getSubcategories(savedParent.getId());
        assertEquals(2, children.size(), "Should have 2 children");

        System.out.println("✓ Setup complete: created parent with 2 children");
    }

    @Test
    @Order(19)
    @DisplayName("Should delete subcategories by parent ID")
    void testDeleteByParent() {
        // Arrange
        Category parent = service.getBySlug("parent-to-delete");
        assertNotNull(parent);
        Long parentId = parent.getId();

        List<Category> beforeDelete = service.getSubcategories(parentId);
        assertTrue(beforeDelete.size() >= 2, "Should have children before delete");

        // Act
        service.deleteByParent(parentId);

        // Assert
        List<Category> afterDelete = service.getSubcategories(parentId);
        assertTrue(afterDelete.isEmpty(), "Should have no children after delete");

        // Parent should still exist
        Category parentAfter = service.getById(parentId);
        assertNotNull(parentAfter, "Parent should still exist");

        System.out.println("✓ Deleted " + beforeDelete.size() + " subcategories");
    }

    @Test
    @Order(20)
    @DisplayName("Should delete category by object")
    void testDeleteCategory() {
        // Arrange
        Category toDelete = service.getBySlug("parent-to-delete");
        assertNotNull(toDelete, "Category should exist before deletion");
        Long deletedId = toDelete.getId();

        // Act
        service.delete(toDelete);

        // Assert
        Category deleted = service.getById(deletedId);
        assertNull(deleted, "Category should not exist after deletion");
        assertNull(service.getBySlug("parent-to-delete"), "Should not find by slug");

        System.out.println("✓ Deleted category with ID: " + deletedId);
    }

    @Test
    @Order(21)
    @DisplayName("Should handle deleting non-existent category gracefully")
    void testDeleteNonExistent() {
        // Arrange
        Category nonExistent = new Category();
        nonExistent.setId(999999L);
        nonExistent.setName("Non-existent");
        nonExistent.setSlug("non-existent");

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> service.delete(nonExistent));

        System.out.println("✓ Handled deletion of non-existent category gracefully");
    }

    // ========== CLEANUP ==========

    @Test
    @Order(22)
    @DisplayName("Clean up test data")
    void cleanupTestData() {
        // Delete in order: subcategories first, then root categories
        if (subcategoryId != null) {
            Category subcat = service.getById(subcategoryId);
            if (subcat != null) service.delete(subcat);
        }

        if (rootCategoryId != null) {
            Category root = service.getById(rootCategoryId);
            if (root != null) service.delete(root);
        }

        if (secondRootCategoryId != null) {
            Category second = service.getById(secondRootCategoryId);
            if (second != null) service.delete(second);
        }

        System.out.println("✓ Test data cleaned up");
    }

    // ========== EDGE CASES & VALIDATION ==========

    @Test
    @Order(23)
    @DisplayName("Should handle empty category list")
    void testEmptyList() {
        // This test assumes all test data is cleaned up
        List<Category> all = service.getAll();
        assertNotNull(all, "Should return a list, not null");
        // Note: May have actual categories from database
        System.out.println("✓ Category list contains " + all.size() + " entries");
    }

    @Test
    @Order(24)
    @DisplayName("Should handle null slug in getBySlug")
    void testNullSlugQuery() {
        // Act
        Category result = service.getBySlug(null);

        // Assert
        assertNull(result, "Should handle null slug gracefully");
        System.out.println("✓ Handled null slug query");
    }

    @Test
    @Order(25)
    @DisplayName("Should handle special characters in slug")
    void testSpecialCharactersInSlug() {
        Category category = new Category();
        category.setName("Special Category");
        category.setSlug("test-slug-123-αβγ");
        category.setDescription("Test with special chars");
        category.setParentId(null);

        service.add(category);
        Category saved = service.getBySlug("test-slug-123-αβγ");

        // Assert
        assertNotNull(saved);
        assertEquals("test-slug-123-αβγ", saved.getSlug());

        // Cleanup
        service.delete(saved);
        System.out.println("✓ Handled special characters in slug");
    }
}