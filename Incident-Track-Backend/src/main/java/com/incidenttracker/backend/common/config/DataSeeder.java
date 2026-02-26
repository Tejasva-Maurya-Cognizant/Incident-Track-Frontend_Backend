// package com.incidenttracker.backend.common.config;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import com.incidenttracker.backend.categories.entity.Categories;
// import
// com.incidenttracker.backend.categories.repository.CategoriesRepository;
// import com.incidenttracker.backend.user.entity.User;
// import com.incidenttracker.backend.user.repository.UserRepository;

// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class DataSeeder implements CommandLineRunner {
// private final UserRepository userRepository;
// private final CategoriesRepository categoryRepository;

// @Override
// public void run(String... args) {
// seedUsers();
// seedCategories();
// }

// private void seedUsers() {
// if (userRepository.count() > 0)
// return;
// for (long i = 1; i <= 20; i++) {
// User user = new User();
// user.setUsername("user" + 1);
// userRepository.save(user);
// }
// }

// private void seedCategories() {
// if (categoryRepository.count() > 0)
// return;
// saveCategory("Hardware", 1);
// saveCategory("Network", 2);
// saveCategory("Security", 4);
// saveCategory("Email", 6);
// saveCategory("VPN", 8);
// saveCategory("Server", 10);
// saveCategory("Database", 12);
// saveCategory("Application", 16);
// saveCategory("Cloud", 24);
// saveCategory("Other", 48);

// }

// private void saveCategory(String name, int slaHours) {
// Categories c = new Categories();
// c.setCategoryName(name);
// c.setSlaTimeHours(slaHours);
// categoryRepository.save(c);
// }
// }