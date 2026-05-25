package com.finance.controller;

import com.finance.dto.CategoryDTO;
import com.finance.service.AuthService;
import com.finance.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<?> getCategories(HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            List<CategoryDTO.CategoryResponse> categories = categoryService.getAllCategories(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

 @PostMapping(consumes = org.springframework.http.MediaType.ALL_VALUE)
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO.CreateRequest request, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            CategoryDTO.CategoryResponse response = categoryService.createCustomCategory(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("already exists") ?
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCategory(@PathVariable String name, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            categoryService.deleteCustomCategory(name, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Category not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }
}
