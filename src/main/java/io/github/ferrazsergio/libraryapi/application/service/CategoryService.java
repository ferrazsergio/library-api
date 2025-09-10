package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.Category;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.CategoryRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public CategoryDTO findById(Integer id) {
        return categoryRepository.findById(id)
                .map(CategoryDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#name", unless = "#result == null")
    public CategoryDTO findByName(String name) {
        return categoryRepository.findByName(name)
                .map(CategoryDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDTO::fromEntity);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO create(CategoryDTO categoryDTO) {
        // Verify if category with same name already exists
        categoryRepository.findByName(categoryDTO.getName())
                .ifPresent(category -> {
                    throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
                });

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(savedCategory);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#id")
    public CategoryDTO update(Integer id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // Check if another category already has this name
        categoryRepository.findByName(categoryDTO.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new RuntimeException("Another category already exists with name: " + categoryDTO.getName());
                    }
                });

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(updatedCategory);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#id")
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // Check if category is used by any book
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete category that is used by books");
        }

        categoryRepository.delete(category);
    }
}