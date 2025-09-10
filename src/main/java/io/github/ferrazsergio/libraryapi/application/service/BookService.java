package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.Author;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Category;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.AuthorRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.CategoryRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#isbn", unless = "#result == null")
    public BookDTO findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(BookDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public BookDTO findById(Integer id) {
        return bookRepository.findById(id)
                .map(BookDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<BookDTO> findAll(Pageable pageable) {
        return bookRepository.findAllNotDeleted(pageable)
                .map(BookDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BookDTO> findByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(title, pageable)
                .map(BookDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BookDTO> findByAuthor(Integer authorId, Pageable pageable) {
        return bookRepository.findByAuthorId(authorId, pageable)
                .map(BookDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BookDTO> findByCategory(Integer categoryId, Pageable pageable) {
        return bookRepository.findByCategoryId(categoryId, pageable)
                .map(BookDTO::fromEntity);
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookDTO create(BookDTO bookDTO) {
        // Validate if ISBN already exists
        bookRepository.findByIsbn(bookDTO.getIsbn())
                .ifPresent(book -> {
                    throw new RuntimeException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
                });

        // Map DTO to entity
        Book book = new Book();
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setDescription(bookDTO.getDescription());
        book.setPublishDate(bookDTO.getPublishDate());
        book.setAvailableQuantity(bookDTO.getAvailableQuantity());
        book.setTotalQuantity(bookDTO.getTotalQuantity());
        book.setPublisher(bookDTO.getPublisher());

        // Set category if provided
        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + bookDTO.getCategoryId()));
            book.setCategory(category);
        }

        // Set authors if provided
        if (bookDTO.getAuthorIds() != null && !bookDTO.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>();
            for (Integer authorId : bookDTO.getAuthorIds()) {
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));
                authors.add(author);
            }
            book.setAuthors(authors);
        }

        // Save the book
        Book savedBook = bookRepository.save(book);

        return BookDTO.fromEntity(savedBook);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public BookDTO update(Integer id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));

        // Check if another book already has this ISBN
        bookRepository.findByIsbn(bookDTO.getIsbn())
                .ifPresent(existingBook -> {
                    if (!existingBook.getId().equals(id)) {
                        throw new RuntimeException("Another book already exists with ISBN: " + bookDTO.getIsbn());
                    }
                });

        // Update basic fields
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setDescription(bookDTO.getDescription());
        book.setPublishDate(bookDTO.getPublishDate());
        book.setPublisher(bookDTO.getPublisher());

        // Only update quantity if it makes sense
        if (bookDTO.getAvailableQuantity() <= bookDTO.getTotalQuantity()) {
            book.setAvailableQuantity(bookDTO.getAvailableQuantity());
            book.setTotalQuantity(bookDTO.getTotalQuantity());
        } else {
            throw new RuntimeException("Available quantity cannot be greater than total quantity");
        }

        // Update category if provided
        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + bookDTO.getCategoryId()));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }

        // Update authors if provided
        if (bookDTO.getAuthorIds() != null) {
            Set<Author> authors = new HashSet<>();
            for (Integer authorId : bookDTO.getAuthorIds()) {
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));
                authors.add(author);
            }
            book.setAuthors(authors);
        }

        Book updatedBook = bookRepository.save(book);
        return BookDTO.fromEntity(updatedBook);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void delete(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));

        // Perform soft delete
        book.setDeleted(true);
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<BookDTO> findMostBorrowedBooks(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return bookRepository.findMostBorrowedBooks(pageable)
                .stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }
}