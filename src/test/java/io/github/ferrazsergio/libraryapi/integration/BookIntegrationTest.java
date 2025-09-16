package io.github.ferrazsergio.libraryapi.integration;

import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Category;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindBook() {
        // Arrange
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Fiction books");
        Category savedCategory = categoryRepository.save(category);

        Book book = new Book();
        book.setIsbn("9780451524935");
        book.setTitle("1984");
        book.setDescription("Dystopian novel");
        book.setPublishDate(LocalDate.of(1949, 6, 8));
        book.setAvailableQuantity(5);
        book.setTotalQuantity(5);
        book.setPublisher("Secker & Warburg");
        book.setCategory(savedCategory);
        book.setDeleted(false);

        // Act
        Book savedBook = bookRepository.save(book);
        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        Optional<Book> foundByIsbn = bookRepository.findByIsbn("9780451524935");

        // Assert
        assertTrue(foundBook.isPresent());
        assertEquals("1984", foundBook.get().getTitle());
        assertTrue(foundByIsbn.isPresent());
        assertEquals("1984", foundByIsbn.get().getTitle());
        assertEquals("Fiction", foundBook.get().getCategory().getName());
    }

    @Test
    void shouldFindBooksByTitle() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("9780451524935");
        book1.setTitle("1984");
        book1.setAvailableQuantity(5);
        book1.setTotalQuantity(5);
        book1.setDeleted(false);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setIsbn("9780156012195");
        book2.setTitle("The Old Man and the Sea");
        book2.setAvailableQuantity(3);
        book2.setTotalQuantity(3);
        book2.setDeleted(false);
        bookRepository.save(book2);

        // Act
        Page<Book> found = bookRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(
                "man", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, found.getTotalElements());
        assertEquals("The Old Man and the Sea", found.getContent().get(0).getTitle());
    }

    @Test
    void shouldHandleSoftDelete() {
        // Arrange
        Book book = new Book();
        book.setIsbn("9780451524935");
        book.setTitle("1984");
        book.setAvailableQuantity(5);
        book.setTotalQuantity(5);
        book.setDeleted(false);
        Book savedBook = bookRepository.save(book);

        // Act - perform soft delete
        savedBook.setDeleted(true);
        bookRepository.save(savedBook);

        Page<Book> activeBooks = bookRepository.findAllNotDeleted(PageRequest.of(0, 10));
        Optional<Book> stillInDb = bookRepository.findById(savedBook.getId());

        // Assert
        assertTrue(stillInDb.isPresent()); // Book still exists in DB
        assertTrue(stillInDb.get().isDeleted()); // But is marked as deleted
        assertEquals(0, activeBooks.getTotalElements()); // Not returned in active books query
    }
}