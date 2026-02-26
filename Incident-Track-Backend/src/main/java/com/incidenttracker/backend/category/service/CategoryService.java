package com.incidenttracker.backend.category.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incidenttracker.backend.category.dto.CategoryRequestDto;
import com.incidenttracker.backend.category.dto.CategoryResponseDto;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.category.repository.CategoryRepository;
import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final DepartmentRepository departmentRepository;

	// Helper method to convert Entity to ResponseDto
	private CategoryResponseDto mapToResponseDto(Category category) {
		return CategoryResponseDto.builder()
				.categoryId(category.getCategoryId())
				.categoryName(category.getCategoryName())
				.subCategory(category.getSubCategory())
				.slaTimeHours(category.getSlaTimeHours())
				.isVisible(category.getIsVisible())
				.departmentName(category.getDepartment() != null ? category.getDepartment().getDepartmentName() : "N/A")
				.build();
	}

	// To return the list of all the Parent Categories
	public List<String> allParents() {
		log.info("Retrieving all parent category names");
		return categoryRepository.findAllParents();
	}

	// To return the list of all the Sub Categories of a parent category
	public List<String> allSubCategories(String parentName) {
		log.info("Retrieving subcategories for parent: {}", parentName);
		return categoryRepository.findAllSubCategories(parentName);
	}

	// To display the category object using its Id
	public Optional<Category> findById(Long id) {
		log.info("Searching for visible category with ID: {}", id);
		return categoryRepository.findByCategoryIdAndIsVisibleTrue(id);
	}

	// To display all the category objects for Admin
	public List<CategoryResponseDto> getAllCategories() {
		log.info("Retrieving all categories for administration");
		return categoryRepository.findAll().stream()
				.map(this::mapToResponseDto)
				.toList();
	}

	// To create new category object
	public CategoryResponseDto createCategory(CategoryRequestDto dto) {
		// Fetch department to store ID in FK column
		log.info("Creating new category: {} in department ID: {}", dto.getCategoryName(), dto.getDepartmentId());
		Department dept = departmentRepository.findById(dto.getDepartmentId())
				.orElseThrow(() -> {
					log.error("Failed to create category: Department ID {} not found", dto.getDepartmentId());
					return new RuntimeException("Department not found");
				});

		Category category = new Category();
		category.setCategoryName(dto.getCategoryName());
		category.setSubCategory(dto.getSubCategory());
		category.setSlaTimeHours(dto.getSlaTimeHours());
		category.setIsVisible(dto.getIsVisible());
		category.setDepartment(dept); // Stores the ID as Foreign Key

		Category savedCategory = categoryRepository.save(category);
		log.info("Successfully created category with ID: {}", savedCategory.getCategoryId());
		return mapToResponseDto(savedCategory);
	}

	// Updating or Editing the category object value
	@Transactional
	public CategoryResponseDto updateCategory(Long id, CategoryRequestDto updates) {
		log.info("Updating category ID: {}", id);
		Category existing = categoryRepository.findById(id)
				.orElseThrow(() -> {
					log.error("Update failed: Category ID {} not found", id);
					return new RuntimeException("Category not found");
				});

		if (updates.getCategoryName() != null) {
			existing.setCategoryName(updates.getCategoryName());
		}
		if (updates.getSubCategory() != null) {
			existing.setSubCategory(updates.getSubCategory());
		}
		if (updates.getSlaTimeHours() != null) {
			existing.setSlaTimeHours(updates.getSlaTimeHours());
		}

		// Logic to update Department ID
		if (updates.getDepartmentId() != null) {
			log.debug("Updating department for category ID: {} to new department ID: {}", id,
					updates.getDepartmentId());
			Department dept = departmentRepository.findById(updates.getDepartmentId())
					.orElseThrow(() -> {
						log.error("Update failed: New Department ID {} not found", updates.getDepartmentId());
						return new RuntimeException("Department not found");
					});
			existing.setDepartment(dept);
		}

		Category updated = categoryRepository.save(existing);
		log.info("Successfully updated category ID: {}", updated.getCategoryId());
		return mapToResponseDto(updated);
	}

	// Toggle the visibility of a categories object
	public CategoryResponseDto toggleVisibility(Long id) {
		log.info("Toggling visibility flag for category ID: {}", id);
		Category existing = categoryRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Toggle visibility failed: Category ID {} not found", id);
					return new RuntimeException("Category not found");
				});
		existing.setIsVisible(!existing.getIsVisible());
		Category toggled = categoryRepository.save(existing);
		log.info("Category ID: {} visibility is now set to: {}", toggled.getCategoryId(), toggled.getIsVisible());
		return mapToResponseDto(toggled);
	}

	// ---- Paginated versions ----

	public PagedResponse<CategoryResponseDto> getAllCategoriesPaged(Pageable pageable) {
		log.info("Retrieving paged categories");
		Page<Category> page = categoryRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	public PagedResponse<CategoryResponseDto> getVisibleCategoriesPaged(Pageable pageable) {
		log.info("Retrieving paged visible categories");
		Page<Category> page = categoryRepository.findByIsVisibleTrue(pageable);
		return toPagedResponse(page);
	}

	private PagedResponse<CategoryResponseDto> toPagedResponse(Page<Category> page) {
		return PagedResponse.<CategoryResponseDto>builder()
				.content(page.getContent().stream().map(this::mapToResponseDto).toList())
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.last(page.isLast())
				.first(page.isFirst())
				.build();
	}
}
