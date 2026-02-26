package com.incidenttracker.backend.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.category.dto.CategoryRequestDto;
import com.incidenttracker.backend.category.dto.CategoryResponseDto;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.category.repository.CategoryRepository;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private CategoryService categoryService;

    // Verify retrieval of top-level parent names
    @Test
    // Provides a readable name for the test in reports.
    @DisplayName("allParents() - Should return list of parent categories")
    void shouldReturnAllParentCategories() {
        // Arrange
        when(categoryRepository.findAllParents()).thenReturn(List.of("IT", "HR"));

        // Act
        List<String> result = categoryService.allParents();

        // Assert
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAllParents();
    }

    @Test
    @DisplayName("allSubCategories() - Should return sub-categories for parent")
    void shouldReturnSubCategories() {
        // Arrange
        when(categoryRepository.findAllSubCategories("IT")).thenReturn(List.of("Hardware", "Software"));

        // Act
        List<String> result = categoryService.allSubCategories("IT");

        // Assert
        assertTrue(result.contains("Hardware"));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findById() - Should return category when visible")
    void shouldFindVisibleCategoryById() {
        // Arrange
        Category category = Category.builder().categoryId(1L).isVisible(true).build();
        when(categoryRepository.findByCategoryIdAndIsVisibleTrue(1L)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> result = categoryService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getCategoryId());
    }

    @Test
    @DisplayName("getAllCategories() - Should return all categories as DTOs")
    void shouldReturnAllCategoryObjects() {
        // Arrange
        Department dept = new Department();
        dept.setDepartmentName("IT Support");

        Category c1 = Category.builder()
                .categoryId(1L)
                .categoryName("Hardware")
                .department(dept)
                .build();

        Category c2 = Category.builder()
                .categoryId(2L)
                .categoryName("Software")
                .department(dept)
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<CategoryResponseDto> result = categoryService.getAllCategories();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Hardware", result.get(0).getCategoryName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createCategory() - Should save category and map to DTO")
    void shouldCreateCategoryFromDto() {
        // Arrange
        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setCategoryName("Network");
        dto.setDepartmentId(1L);

        Department dept = new Department();
        dept.setDepartmentId(1L);
        dept.setDepartmentName("IT Dept");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        CategoryResponseDto result = categoryService.createCategory(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Network", result.getCategoryName());
        assertEquals("IT Dept", result.getDepartmentName());
    }

    @Test
    @DisplayName("updateCategory() - Should update fields and return DTO")
    void shouldUpdateCategoryData() {
        // Arrange
        Category existing = Category.builder().categoryId(1L).categoryName("Old Name").build();
        CategoryRequestDto updates = new CategoryRequestDto();
        updates.setCategoryName("New Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        CategoryResponseDto result = categoryService.updateCategory(1L, updates);

        // Assert
        assertEquals("New Name", result.getCategoryName());
    }

    @Test
    @DisplayName("toggleVisibility() - Should flip IsVisible state")
    void shouldToggleVisibility() {
        // Arrange
        Category category = Category.builder().categoryId(1L).isVisible(true).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        // Act & Assert
        // First Toggle: True -> False
        CategoryResponseDto firstResult = categoryService.toggleVisibility(1L);
        assertFalse(firstResult.getIsVisible());

        // Second Toggle: False -> True
        CategoryResponseDto secondResult = categoryService.toggleVisibility(1L);
        assertTrue(secondResult.getIsVisible());
    }
}