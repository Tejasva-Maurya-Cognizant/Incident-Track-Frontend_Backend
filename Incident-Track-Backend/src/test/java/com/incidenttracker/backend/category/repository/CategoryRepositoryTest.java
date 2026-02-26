package com.incidenttracker.backend.category.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.category.entity.Category;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private String timestamp;

    // Runs before each test to prepare common setup.
    @BeforeEach
    void setUp() {
        timestamp = String.valueOf(System.currentTimeMillis());
    }

    private Category createCategory(String name, String subCategory, boolean visible) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setSubCategory(subCategory);
        category.setIsVisible(visible);
        // Using repository save to ensure ID generation and persistence alignment
        return categoryRepository.save(category);
    }

    // Verify that only distinct, visible parent category names are returned
    @Test
    // Provides a readable name for the test in reports.
    @DisplayName("findAllParents() - Should contain the newly created visible parents")
    void shouldReturnDistinctVisibleParentNames() {
        // Arrange
        String uniqueName1 = "Test_Hardware_" + timestamp;
        String uniqueName2 = "Test_Software_" + timestamp;

        createCategory(uniqueName1, "Laptop", true);
        createCategory(uniqueName2, "OS", true);
        createCategory("Test_Hidden", "Secret", false);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<String> parents = categoryRepository.findAllParents();

        // Assert
        assertThat(parents).contains(uniqueName1, uniqueName2);
        assertThat(parents).doesNotContain("Test_Hidden");
    }

    @Test
    @DisplayName("findAllSubCategories() - Should return subcategories for a specific parent")
    void shouldReturnSubCategoriesByParentName() {
        // Arrange
        String parent = "IT_Test_" + timestamp;
        createCategory(parent, "Sub1", true);
        createCategory(parent, "Sub2", true);
        createCategory(parent, "Sub3_Hidden", false);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<String> subCategories = categoryRepository.findAllSubCategories(parent);

        // Assert
        assertThat(subCategories).hasSize(2);
        assertThat(subCategories).containsExactlyInAnyOrder("Sub1", "Sub2");
        assertThat(subCategories).doesNotContain("Sub3_Hidden");
    }

    @Test
    @DisplayName("findByCategoryIdAndIsVisibleTrue() - Should respect visibility")
    void shouldFindCategoryByIdOnlyWhenVisible() {
        // Arrange
        Category visibleCategory = createCategory("VisibilityTest", "Visible", true);
        Category hiddenCategory = createCategory("VisibilityTest", "Hidden", false);

        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Category> foundVisible = categoryRepository
                .findByCategoryIdAndIsVisibleTrue(visibleCategory.getCategoryId());
        Optional<Category> foundHidden = categoryRepository
                .findByCategoryIdAndIsVisibleTrue(hiddenCategory.getCategoryId());

        // Assert
        assertThat(foundVisible).isPresent();
        assertThat(foundHidden).isEmpty();
    }
}
