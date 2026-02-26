package com.incidenttracker.backend.category.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.category.dto.CategoryRequestDto;
import com.incidenttracker.backend.category.dto.CategoryResponseDto;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.category.service.CategoryService;
import com.incidenttracker.backend.common.dto.PagedResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // To create new Category object
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto categoryDto) {
        log.info("Request received to create a new category: {}", categoryDto.getCategoryName());
        CategoryResponseDto newCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    // To display all the Category objects (non-paged)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        log.info("Fetching all categories");
        return ResponseEntity.ok().body(categoryService.getAllCategories());
    }

    /**
     * GET /api/categories/paged?page=0&size=10&sortBy=categoryName&sortDir=asc
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<CategoryResponseDto>> getAllCategoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Fetching paged categories");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(categoryService.getAllCategoriesPaged(pageable));
    }

    /**
     * GET /api/categories/visible/paged - only visible (non-soft-deleted)
     * categories
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/visible/paged")
    public ResponseEntity<PagedResponse<CategoryResponseDto>> getVisibleCategoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Fetching paged visible categories");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(categoryService.getVisibleCategoriesPaged(pageable));
    }

    // To get the list of parent categories as a drop-down
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/parent-categories")
    public ResponseEntity<List<String>> getAllParents() {
        log.info("Fetching all distinct parent categories");
        return ResponseEntity.ok().body(categoryService.allParents());
    }

    // To get the list of child categories as a drop-down
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/subcategories")
    public ResponseEntity<List<String>> getAllSubCategories(@RequestParam String parent) {
        log.info("Fetching subcategories for parent category: {}", parent);
        return ResponseEntity.ok().body(categoryService.allSubCategories(parent));
    }

    // To display the Category object using its Id
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/details/{id}")
    public ResponseEntity<Optional<Category>> findById(@PathVariable Long id) {
        log.info("Fetching category details for ID: {}", id);
        return ResponseEntity.ok().body(categoryService.findById(id));
    }

    // To change or update individual value of a record
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/details/{id}")
    public ResponseEntity<CategoryResponseDto> updateData(@PathVariable Long id,
            @RequestBody CategoryRequestDto categoryUpdates) {
        log.info("Request received to update category ID: {}", id);
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, categoryUpdates);
        return ResponseEntity.ok().body(updatedCategory);
    }

    // To toggle visibility (Soft-delete)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/visibility/{id}")
    public ResponseEntity<CategoryResponseDto> toggleVisibility(@PathVariable Long id) {
        log.info("Toggling visibility for category ID: {}", id);
        CategoryResponseDto updatedCategory = categoryService.toggleVisibility(id);
        return ResponseEntity.ok().body(updatedCategory);
    }
}