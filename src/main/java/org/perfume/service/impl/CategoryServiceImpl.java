package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Category;
import org.perfume.domain.repo.CategoryDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.CategoryMapper;
import org.perfume.model.dto.request.CategoryRequest;
import org.perfume.model.dto.response.CategoryResponse;
import org.perfume.service.CategorySevice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategorySevice {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Category with name " + request.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toDto(categoryDao.save(category));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));

        if (category.getName().equals(request.getName()) && categoryDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Category with name " + request.getName() + " already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toDto(categoryDao.save(category));
    }

    @Override
    public void delete(Long id) {
        if (!categoryDao.existsById(id)) {
            throw new NotFoundException("Category with id " + id + " not found");
        }
        categoryDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category =categoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));

        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryDao.findAllByOrderByNameAsc().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesWithPerfumes() {
        return categoryDao.findCategoriesWithPerfumes().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String name) {
        if(!categoryDao.existsByName(name)) {
            throw new NotFoundException("Category with name " + name + " not found");
        }

        return categoryDao.findByNameContainingIgnoreCase(name).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }
}
