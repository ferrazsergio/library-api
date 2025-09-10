package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.BookService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management APIs")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "List all books with pagination")
    public ResponseEntity<Page<BookDTO>> getAllBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN")
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findByIsbn(isbn));
    }

    @GetMapping("/search/title")
    @Operation(summary = "Search books by title")
    public ResponseEntity<Page<BookDTO>> searchBooksByTitle(
            @RequestParam String title,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findByTitle(title, pageable));
    }

    @GetMapping("/search/author/{authorId}")
    @Operation(summary = "Search books by author")
    public ResponseEntity<Page<BookDTO>> searchBooksByAuthor(
            @PathVariable Integer authorId,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAuthor(authorId, pageable));
    }

    @GetMapping("/search/category/{categoryId}")
    @Operation(summary = "Search books by category")
    public ResponseEntity<Page<BookDTO>> searchBooksByCategory(
            @PathVariable Integer categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findByCategory(categoryId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Create a new book")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        return new ResponseEntity<>(bookService.create(bookDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update an existing book")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Integer id,
            @Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookService.update(id, bookDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Delete a book (soft delete)")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/most-borrowed")
    @Operation(summary = "Get most borrowed books")
    public ResponseEntity<List<BookDTO>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.findMostBorrowedBooks(limit));
    }
}