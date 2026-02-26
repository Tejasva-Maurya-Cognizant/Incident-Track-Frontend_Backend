package com.incidenttracker.backend.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttracker.backend.category.dto.CategoryRequestDto;
import com.incidenttracker.backend.category.dto.CategoryResponseDto;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.category.service.CategoryService;
import com.incidenttracker.backend.user.config.JWTUtil;
import com.incidenttracker.backend.user.service.CustomUserDetailsService;

@WebMvcTest(value = CategoryController.class, excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false) // Security is ignored here
@Import(ObjectMapper.class)
class CategoryControllerTest {

	// Injects a Spring-managed bean into the test.
	@Autowired
	private MockMvc mockMvc;

	// Creates a Mockito mock for isolating dependencies.
	@MockitoBean
	private CategoryService categoryService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JWTUtil jwtUtil;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	// Verify category creation
	@Test
	// Provides a readable name for the test in reports.
	@DisplayName("POST /api/categories")
	void shouldCreateCategory() throws Exception {
		// Arrange
		CategoryRequestDto dto = new CategoryRequestDto();
		dto.setCategoryName("Legal");

		CategoryResponseDto response = new CategoryResponseDto();
		response.setCategoryId(1L);
		response.setCategoryName("Legal");

		when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(response);

		// Act & Assert
		mockMvc.perform(post("/api/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated()) // Matches your @PostMapping logic
				.andExpect(jsonPath("$.categoryId").value(1));
	}

	@Test
	@DisplayName("GET /api/categories")
	void shouldReturnAllCategories() throws Exception {
		// Arrange
		CategoryResponseDto res = new CategoryResponseDto();
		res.setCategoryName("Hardware");

		when(categoryService.getAllCategories()).thenReturn(List.of(res));

		// Act & Assert
		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1))
				.andExpect(jsonPath("$[0].categoryName").value("Hardware"));
	}

	@Test
	@DisplayName("GET /api/categories/parent-categories")
	void shouldReturnParentList() throws Exception {
		// Arrange
		when(categoryService.allParents()).thenReturn(List.of("IT", "HR"));

		// Act & Assert
		mockMvc.perform(get("/api/categories/parent-categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2));
	}

	@Test
	@DisplayName("GET /api/categories/subcategories")
	void shouldReturnSubCategoriesByParent() throws Exception {
		// Arrange
		String parent = "IT";
		when(categoryService.allSubCategories(parent)).thenReturn(List.of("Hardware", "Software"));

		// Act & Assert
		mockMvc.perform(get("/api/categories/subcategories")
				.param("parent", parent))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0]").value("Hardware"));
	}

	@Test
	@DisplayName("GET /api/categories/details/{id}")
	void shouldFindCategoryById() throws Exception {
		// Arrange
		Category category = Category.builder().categoryId(1L).categoryName("Network").build();

		when(categoryService.findById(1L)).thenReturn(Optional.of(category));

		// Act & Assert
		mockMvc.perform(get("/api/categories/details/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoryName").value("Network"));
	}

	@Test
	@DisplayName("PATCH /api/categories/details/{id}")
	void shouldUpdateCategoryData() throws Exception {
		// Arrange
		CategoryRequestDto updates = new CategoryRequestDto();
		updates.setSlaTimeHours(24);

		CategoryResponseDto response = new CategoryResponseDto();
		response.setSlaTimeHours(24);

		when(categoryService.updateCategory(eq(1L), any(CategoryRequestDto.class)))
				.thenReturn(response);

		// Act & Assert
		mockMvc.perform(patch("/api/categories/details/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slaTimeHours").value(24));
	}

	@Test
	@DisplayName("PATCH /api/categories/visibility/{id}")
	void shouldToggleVisibility() throws Exception {
		// Arrange
		CategoryResponseDto response = new CategoryResponseDto();
		response.setIsVisible(false);

		when(categoryService.toggleVisibility(1L)).thenReturn(response);

		// Act & Assert
		mockMvc.perform(patch("/api/categories/visibility/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isVisible").value(false));
	}
}