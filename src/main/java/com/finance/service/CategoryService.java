package com.finance.service;

import com.finance.dto.CategoryDTO;
import com.finance.entity.Category;
import com.finance.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostConstruct
    public void initDefaultCategories() {
        if (categoryRepository.findByIsDefaultTrue().isEmpty()) {
            String[] expenseCategories = {
                "Food & Dining", "Transportation", "Housing", "Utilities",
                "Healthcare", "Entertainment", "Shopping", "Education",
                "Personal Care", "Others"
            };
            for (String name : expenseCategories) {
                categoryRepository.save(new Category(name, "EXPENSE", true, null));
            }

            String[] incomeCategories = {
                "Salary", "Freelance", "Business", "Investment", "Rental", "Other Income"
            };
            for (String name : incomeCategories) {
                categoryRepository.save(new Category(name, "INCOME", true, null));
            }
        }
    }

    public List<CategoryDTO.CategoryResponse> getAllCategories(Long userId) {
        return categoryRepository.findAllByUserIdOrDefault(userId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO.CategoryResponse createCustomCategory(CategoryDTO.CreateRequest request, Long userId) {
        if (!request.getType().equals("INCOME") && !request.getType().equals("EXPENSE")) {
            throw new RuntimeException("Type must be INCOME or EXPENSE");
        }

        if (categoryRepository.existsByNameAndIsDefaultTrue(request.getName())) {
            throw new RuntimeException("Category already exists as a default category");
        }

        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category(request.getName(), request.getType(), false, userId);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCustomCategory(String name, Long userId) {
        Category category = categoryRepository.findByNameAndUserId(name, userId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.isDefault()) {
            throw new RuntimeException("Cannot delete default categories");
        }

        categoryRepository.delete(category);
    }

    public String getCategoryType(String categoryName, Long userId) {
        for (Category c : categoryRepository.findByIsDefaultTrue()) {
            if (c.getName().equals(categoryName)) return c.getType();
        }

        return categoryRepository.findByNameAndUserId(categoryName, userId)
            .map(Category::getType)
            .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
    }

    private CategoryDTO.CategoryResponse toResponse(Category category) {
        CategoryDTO.CategoryResponse response = new CategoryDTO.CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setType(category.getType());
        response.setDefault(category.isDefault());
        return response;
    }
}