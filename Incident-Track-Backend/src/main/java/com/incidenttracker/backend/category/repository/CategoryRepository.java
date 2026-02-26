package com.incidenttracker.backend.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.incidenttracker.backend.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT DISTINCT c.categoryName FROM category c WHERE c.isVisible = true")
    List<String> findAllParents();

    @Query("SELECT c.subCategory FROM category c WHERE c.categoryName = :parentName AND c.isVisible = true")
    List<String> findAllSubCategories(@Param("parentName") String parentName);

    Optional<Category> findByCategoryIdAndIsVisibleTrue(Long id);

    // ---- Pageable versions ----
    Page<Category> findAll(Pageable pageable);

    Page<Category> findByIsVisibleTrue(Pageable pageable);
}
