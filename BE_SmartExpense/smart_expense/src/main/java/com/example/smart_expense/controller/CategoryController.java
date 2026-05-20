package com.example.smart_expense.controller;

import com.example.smart_expense.model.Category;
import com.example.smart_expense.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Lấy danh sách danh mục (bao gồm cả mặc định hệ thống và danh mục tự tạo).
     * GET /api/categories?userId=1
     */
    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@RequestParam Integer userId) {
        List<Category> categories = categoryRepository.findActiveCategoriesForUser(userId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Tạo danh mục tùy chỉnh mới cho người dùng.
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }
}
