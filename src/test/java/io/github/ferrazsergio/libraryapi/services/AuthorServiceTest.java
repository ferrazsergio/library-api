package io.github.ferrazsergio.libraryapi.services;

import io.github.ferrazsergio.libraryapi.application.service.AuthorService;
import io.github.ferrazsergio.libraryapi.domain.model.Author;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.AuthorRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthorDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author author;
    private AuthorDTO authorDTO;
    private Book book;

    @BeforeEach
    void setUp() {
        // Setup author
        author = new Author();
        author.setId(1);
        author.setName("George Orwell");
        author.setBiography("English novelist");
        author.setBirthDate(LocalDate.of(1903, 6, 25));
        author.setBooks(new HashSet<>());

        // Setup authorDTO
        authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        authorDTO.setName("George Orwell");
        authorDTO.setBiography("English novelist");
        authorDTO.setBirthDate(LocalDate.of(1903, 6, 25));

        // Setup book
        book = new Book();
        book.setId(1);
        book.setTitle("1984");
        book.setIsbn("9780451524935");

        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book.setAuthors(authors);

        Set<Book> books = new HashSet<>();
        books.add(book);
        author.setBooks(books);
    }

    @Test
    void findByIdShouldReturnAuthorWhenExists() {
        // Arrange
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        // Act
        AuthorDTO result = authorService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("George Orwell", result.getName());
    }

    @Test
    void findByIdShouldThrowExceptionWhenAuthorNotFound() {
        // Arrange
        when(authorRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authorService.findById(99);
        });

        assertTrue(exception.getMessage().contains("Author not found"));
    }

    @Test
    void findAllShouldReturnPageOfAuthors() {
        // Arrange
        List<Author> authors = List.of(author);
        Page<Author> authorPage = new PageImpl<>(authors);
        Pageable pageable = PageRequest.of(0, 10);

        when(authorRepository.findAll(pageable)).thenReturn(authorPage);

        // Act
        Page<AuthorDTO> result = authorService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("George Orwell", result.getContent().get(0).getName());
    }

    @Test
    void findByNameShouldReturnAuthors() {
        // Arrange
        List<Author> authors = List.of(author);
        Page<Author> authorPage = new PageImpl<>(authors);
        Pageable pageable = PageRequest.of(0, 10);

        when(authorRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(authorPage);

        // Act
        Page<AuthorDTO> result = authorService.findByName("Orwell", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("George Orwell", result.getContent().get(0).getName());
    }

    @Test
    void findBooksByAuthorShouldReturnBooks() {
        // Arrange
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);

        when(authorRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByAuthorId(1, pageable)).thenReturn(bookPage);

        // Act
        Page<BookDTO> result = authorService.findBooksByAuthor(1, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("1984", result.getContent().get(0).getTitle());
    }

    @Test
    void findBooksByAuthorShouldThrowExceptionWhenAuthorNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(authorRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authorService.findBooksByAuthor(99, pageable);
        });

        assertTrue(exception.getMessage().contains("Author not found"));
    }

    @Test
    void createShouldReturnSavedAuthor() {
        // Arrange
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        // Act
        AuthorDTO result = authorService.create(authorDTO);

        // Assert
        assertNotNull(result);
        assertEquals("George Orwell", result.getName());
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    void updateShouldReturnUpdatedAuthor() {
        // Arrange
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        authorDTO.setBiography("Updated biography");

        // Act
        AuthorDTO result = authorService.update(1, authorDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated biography", result.getBiography());
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    void deleteShouldRemoveAuthor() {
        // Arrange
        author.setBooks(new HashSet<>()); // Author has no books
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        // Act
        authorService.delete(1);

        // Assert
        verify(authorRepository, times(1)).delete(any(Author.class));
    }

    @Test
    void deleteShouldThrowExceptionWhenAuthorHasBooks() {
        // Arrange
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authorService.delete(1);
        });

        assertTrue(exception.getMessage().contains("Cannot delete author that has books"));
        verify(authorRepository, never()).delete(any(Author.class));
    }
}