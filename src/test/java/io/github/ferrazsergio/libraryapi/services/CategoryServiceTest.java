package io.github.ferrazsergio.libraryapi.services;



import io.github.ferrazsergio.libraryapi.application.service.CategoryService;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Category;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.CategoryRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.CategoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        // Setup category
        category = new Category();
        category.setId(1);
        category.setName("Fiction");
        category.setDescription("Fiction books");
        category.setBooks(new HashSet<>());

        // Setup categoryDTO
        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1);
        categoryDTO.setName("Fiction");
        categoryDTO.setDescription("Fiction books");
    }

    @Test
    void findByIdShouldReturnCategoryWhenExists() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        // Act
        CategoryDTO result = categoryService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Fiction", result.getName());
    }

    @Test
    void findByIdShouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.findById(99);
        });

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void findByNameShouldReturnCategoryWhenExists() {
        // Arrange
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.of(category));

        // Act
        CategoryDTO result = categoryService.findByName("Fiction");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void findAllShouldReturnPageOfCategories() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // Act
        Page<CategoryDTO> result = categoryService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Fiction", result.getContent().get(0).getName());
    }

    @Test
    void createShouldReturnSavedCategory() {
        // Arrange
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        CategoryDTO result = categoryService.create(categoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Fiction", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createShouldThrowExceptionWhenNameExists() {
        // Arrange
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.of(category));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.create(categoryDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateShouldReturnUpdatedCategory() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryDTO.setDescription("Updated description");

        // Act
        CategoryDTO result = categoryService.update(1, categoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteShouldRemoveCategory() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        // Act
        categoryService.delete(1);

        // Assert
        verify(categoryRepository, times(1)).delete(any(Category.class));
    }

    @Test
    void deleteShouldThrowExceptionWhenCategoryHasBooks() {
        // Arrange
        Set<Book> books = new HashSet<>();
        Book book = new Book();
        book.setId(1);
        books.add(book);

        category.setBooks(books);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.delete(1);
        });

        assertTrue(exception.getMessage().contains("used by books"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}