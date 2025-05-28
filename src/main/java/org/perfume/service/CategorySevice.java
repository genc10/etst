package org.perfume.service;

import org.perfume.model.dto.request.CategoryRequest;
import org.perfume.model.dto.response.CategoryResponse;

import java.util.List;

public interface CategorySevice {
    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);

    CategoryResponse getById(Long id);

    List<CategoryResponse> getAll();

    List<CategoryResponse> getCategoriesWithPerfumes();

    List<CategoryResponse> searchCategories(String name);


}
