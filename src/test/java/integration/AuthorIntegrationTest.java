package integration;

import io.github.ferrazsergio.libraryapi.domain.model.Author;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.AuthorRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthorIntegrationTest {

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


    private final AuthorRepository authorRepository;


    private final BookRepository bookRepository;

    public AuthorIntegrationTest(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Test
    void shouldSaveAndFindAuthor() {
        // Arrange
        Author author = new Author();
        author.setName("George Orwell");
        author.setBiography("English novelist");
        author.setBirthDate(LocalDate.of(1903, 6, 25));

        // Act
        Author savedAuthor = authorRepository.save(author);
        Optional<Author> foundAuthor = authorRepository.findById(savedAuthor.getId());

        // Assert
        assertTrue(foundAuthor.isPresent());
        assertEquals("George Orwell", foundAuthor.get().getName());
    }

    @Test
    void shouldFindAuthorsByName() {
        // Arrange
        Author author1 = new Author();
        author1.setName("George Orwell");
        author1.setBiography("English novelist");
        authorRepository.save(author1);

        Author author2 = new Author();
        author2.setName("Ernest Hemingway");
        author2.setBiography("American novelist");
        authorRepository.save(author2);

        // Act
        Page<Author> found = authorRepository.findByNameContainingIgnoreCase(
                "Orwell", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, found.getTotalElements());
        assertEquals("George Orwell", found.getContent().get(0).getName());
    }

    @Test
    void shouldHandleAuthorBookRelationship() {
        // Arrange
        Author author = new Author();
        author.setName("George Orwell");
        author.setBiography("English novelist");
        Author savedAuthor = authorRepository.save(author);

        Book book = new Book();
        book.setIsbn("9780451524935");
        book.setTitle("1984");
        book.setAvailableQuantity(5);
        book.setTotalQuantity(5);

        Set<Author> authors = new HashSet<>();
        authors.add(savedAuthor);
        book.setAuthors(authors);

        Book savedBook = bookRepository.save(book);

        // Act
        Page<Author> bookAuthors = authorRepository.findByBookId(
                savedBook.getId(), PageRequest.of(0, 10));

        // Assert
        assertEquals(1, bookAuthors.getTotalElements());
        assertEquals("George Orwell", bookAuthors.getContent().get(0).getName());
    }
}