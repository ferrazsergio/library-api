package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.BookService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.swagger.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Books", description = "Book management operations")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(
            summary = "List all books",
            description = "Returns a paginated list of books with sorting and filtering options",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of books retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Valid authentication credentials required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get book by ID",
            description = "Returns a single book by its unique identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"title\": \"Not Found\", \"message\": \"Book not found with ID: 999\", \"timestamp\": \"2025-09-10T18:58:53\", \"path\": \"/api/v1/books/999\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<BookDTO> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(
            summary = "Get book by ISBN",
            description = "Returns a single book by its unique ISBN number",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<BookDTO> getBookByIsbn(
            @Parameter(description = "ISBN of the book", required = true, example = "9780451524935")
            @PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findByIsbn(isbn));
    }

    @GetMapping("/search/title")
    @Operation(
            summary = "Search books by title",
            description = "Returns a paginated list of books containing the specified title text (case insensitive)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search results",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
                            )
                    )
            }
    )
    public ResponseEntity<Page<BookDTO>> searchBooksByTitle(
            @Parameter(description = "Title text to search for", required = true, example = "1984")
            @RequestParam String title,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.findByTitle(title, pageable));
    }

    @GetMapping("/search/author/{authorId}")
    @Operation(
            summary = "Find books by author",
            description = "Returns a paginated list of books by a specific author",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Author not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<Page<BookDTO>> findBooksByAuthor(
            @Parameter(description = "ID of the author", required = true, example = "1")
            @PathVariable Integer authorId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAuthor(authorId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Create a new book",
            description = "Creates a new book with the provided details (requires ADMIN or LIBRARIAN role)",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Book created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Book with same ISBN already exists",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Insufficient permissions",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<BookDTO> createBook(
            @Parameter(
                    description = "Book details",
                    required = true,
                    schema = @Schema(implementation = BookDTO.class),
                    examples = @ExampleObject(value = "{\"isbn\": \"9780451524935\", \"title\": \"1984\", \"description\": \"Dystopian novel\", \"publishDate\": \"1949-06-08\", \"availableQuantity\": 5, \"totalQuantity\": 5, \"publisher\": \"Secker & Warburg\", \"categoryId\": 1, \"authorIds\": [1]}")
            )
            @Valid @RequestBody BookDTO bookDTO) {
        return new ResponseEntity<>(bookService.create(bookDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Update an existing book",
            description = "Updates an existing book with the provided details (requires ADMIN or LIBRARIAN role)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Insufficient permissions",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<BookDTO> updateBook(
            @Parameter(description = "ID of the book to update", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(
                    description = "Updated book details",
                    required = true,
                    schema = @Schema(implementation = BookDTO.class)
            )
            @Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookService.update(id, bookDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Delete a book (soft delete)",
            description = "Marks a book as deleted without removing it from the database (requires ADMIN or LIBRARIAN role)",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Book deleted successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Insufficient permissions",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true, example = "1")
            @PathVariable Integer id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/most-borrowed")
    @Operation(
            summary = "Get most borrowed books",
            description = "Returns a list of the most frequently borrowed books",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of most borrowed books",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<BookDTO>> getMostBorrowedBooks(
            @Parameter(description = "Maximum number of books to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.findMostBorrowedBooks(limit));
    }
}