package org.perfume.service;

import org.perfume.model.dto.request.CategoryRequest;
import org.perfume.model.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> searchCategories(String name);
    List<CategoryResponse> getCategoriesWithPerfumes();
}