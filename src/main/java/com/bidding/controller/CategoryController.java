package com.bidding.controller;

import com.bidding.entity.Category;
import com.bidding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/category")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // =============================================
    // SABHI CATEGORIES FETCH KARO (Public - login zaruri nahi)
    // URL: GET /api/category/fetch/all
    // =============================================
    @GetMapping("/fetch/all")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        Map<String, Object> response = new HashMap<>();

        // Sirf Active categories dikhao
        List<Category> categories = categoryRepository.findByStatus("Active");

        response.put("success", true);
        response.put("categories", categories);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // CATEGORY ADD KARO (Admin only)
    // URL: POST /api/category/add
    // =============================================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCategory(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        Category category = new Category();
        category.setName(request.get("name"));
        category.setDescription(request.get("description"));
        category.setStatus("Active");

        categoryRepository.save(category);

        response.put("success", true);
        response.put("message", "Category add ho gayi!");
        return ResponseEntity.ok(response);
    }

    // =============================================
    // CATEGORY DELETE KARO (Admin only)
    // URL: DELETE /api/category/{id}
    // =============================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        Optional<Category> optCat = categoryRepository.findById(id);
        if (optCat.isEmpty()) {
            response.put("success", false);
            response.put("message", "Category nahi mili");
            return ResponseEntity.notFound().build();
        }

        Category category = optCat.get();
        category.setStatus("Deleted"); // Hard delete nahi karte - sirf status change karte hain
        categoryRepository.save(category);

        response.put("success", true);
        response.put("message", "Category delete ho gayi!");
        return ResponseEntity.ok(response);
    }
}
