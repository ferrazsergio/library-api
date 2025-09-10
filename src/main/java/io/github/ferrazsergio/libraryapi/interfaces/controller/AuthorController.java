package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.AuthorService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthorDTO;
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

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author management APIs")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @Operation(summary = "List all authors with pagination")
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(Pageable pageable) {
        return ResponseEntity.ok(authorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Integer id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search authors by name")
    public ResponseEntity<Page<AuthorDTO>> searchAuthorsByName(
            @RequestParam String name,
            Pageable pageable) {
        return ResponseEntity.ok(authorService.findByName(name, pageable));
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "Get books by author")
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @PathVariable Integer id,
            Pageable pageable) {
        return ResponseEntity.ok(authorService.findBooksByAuthor(id, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Create a new author")
    public ResponseEntity<AuthorDTO> createAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        return new ResponseEntity<>(authorService.create(authorDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update an existing author")
    public ResponseEntity<AuthorDTO> updateAuthor(
            @PathVariable Integer id,
            @Valid @RequestBody AuthorDTO authorDTO) {
        return ResponseEntity.ok(authorService.update(id, authorDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete an author")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}