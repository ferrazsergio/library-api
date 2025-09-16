package io.github.ferrazsergio.libraryapi.services;

import io.github.ferrazsergio.libraryapi.application.service.BookService;
import io.github.ferrazsergio.libraryapi.domain.model.Author;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Category;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.AuthorRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.CategoryRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
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

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookDTO bookDTO;
    private Category category;
    private Author author;
    private Set<Author> authors;

    @BeforeEach
    void setUp() {
        // Setup category
        category = new Category();
        category.setId(1);
        category.setName("Fiction");
        category.setDescription("Fiction books");

        // Setup author
        author = new Author();
        author.setId(1);
        author.setName("George Orwell");
        author.setBiography("English novelist");
        author.setBirthDate(LocalDate.of(1903, 6, 25));

        authors = new HashSet<>();
        authors.add(author);

        // Setup book
        book = new Book();
        book.setId(1);
        book.setIsbn("9780451524935");
        book.setTitle("1984");
        book.setDescription("Dystopian novel");
        book.setPublishDate(LocalDate.of(1949, 6, 8));
        book.setAvailableQuantity(5);
        book.setTotalQuantity(5);
        book.setPublisher("Secker & Warburg");
        book.setCategory(category);
        book.setAuthors(authors);
        book.setDeleted(false);

        // Setup bookDTO
        bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setIsbn("9780451524935");
        bookDTO.setTitle("1984");
        bookDTO.setDescription("Dystopian novel");
        bookDTO.setPublishDate(LocalDate.of(1949, 6, 8));
        bookDTO.setAvailableQuantity(5);
        bookDTO.setTotalQuantity(5);
        bookDTO.setPublisher("Secker & Warburg");
        bookDTO.setCategoryId(1);
        bookDTO.setAuthorIds(Set.of(1));
    }

    @Test
    void findByIdShouldReturnBookWhenExists() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        // Act
        BookDTO result = bookService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("1984", result.getTitle());
        assertEquals("9780451524935", result.getIsbn());
    }

    @Test
    void findByIdShouldThrowExceptionWhenBookNotFound() {
        // Arrange
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookService.findById(99);
        });

        assertTrue(exception.getMessage().contains("Book not found"));
    }

    @Test
    void findByIsbnShouldReturnBookWhenExists() {
        // Arrange
        when(bookRepository.findByIsbn("9780451524935")).thenReturn(Optional.of(book));

        // Act
        BookDTO result = bookService.findByIsbn("9780451524935");

        // Assert
        assertNotNull(result);
        assertEquals("1984", result.getTitle());
    }

    @Test
    void findAllShouldReturnPageOfBooks() {
        // Arrange
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findAllNotDeleted(pageable)).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = bookService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("1984", result.getContent().get(0).getTitle());
    }

    @Test
    void createShouldReturnSavedBook() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        BookDTO result = bookService.create(bookDTO);

        // Assert
        assertNotNull(result);
        assertEquals("1984", result.getTitle());
        assertEquals("9780451524935", result.getIsbn());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createShouldThrowExceptionWhenIsbnExists() {
        // Arrange
        when(bookRepository.findByIsbn("9780451524935")).thenReturn(Optional.of(book));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookService.create(bookDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateShouldReturnUpdatedBook() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("9780451524935")).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        bookDTO.setTitle("1984 - Updated");

        // Act
        BookDTO result = bookService.update(1, bookDTO);

        // Assert
        assertNotNull(result);
        assertEquals("1984 - Updated", result.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void deleteShouldMarkBookAsDeleted() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        // Act
        bookService.delete(1);

        // Assert
        verify(bookRepository, times(1)).save(any(Book.class));
        assertTrue(book.isDeleted());
    }

    @Test
    void findMostBorrowedBooksShouldReturnList() {
        // Arrange
        List<Book> books = List.of(book);
        when(bookRepository.findMostBorrowedBooks(any(Pageable.class))).thenReturn(books);

        // Act
        List<BookDTO> result = bookService.findMostBorrowedBooks(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1984", result.get(0).getTitle());
    }
}