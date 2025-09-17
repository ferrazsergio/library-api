package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.AuthorService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthorDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.swagger.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Authors", description = "Operations related to author management including creation, retrieval, update, and deletion")
@SecurityRequirement(name = "bearerAuth")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @Operation(
            summary = "List all authors with pagination",
            description = "Retrieves a paginated list of all authors. Results can be sorted and paginated using query parameters.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of authors retrieved successfully",
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
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(authorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get author by ID",
            description = "Retrieves detailed information about a specific author by their unique identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Author found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthorDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"name\": \"George Orwell\", \"biography\": \"English novelist and essayist\", \"birthDate\": \"1903-06-25\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Author not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"title\": \"Not Found\", \"message\": \"Author not found with ID: 999\", \"timestamp\": \"2025-09-10T19:10:12\", \"path\": \"/api/v1/authors/999\"}")
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
    public ResponseEntity<AuthorDTO> getAuthorById(
            @Parameter(description = "ID of the author to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search authors by name",
            description = "Searches for authors whose names contain the specified search term (case insensitive). Results are paginated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search results",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
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
    public ResponseEntity<Page<AuthorDTO>> searchAuthorsByName(
            @Parameter(description = "Name text to search for", required = true, example = "Orwell")
            @RequestParam String name,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(authorService.findByName(name, pageable));
    }

    @GetMapping("/{id}/books")
    @Operation(
            summary = "Get books by author",
            description = "Retrieves a paginated list of all books written by a specific author",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of books by author retrieved successfully",
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
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @Parameter(description = "ID of the author", required = true, example = "1")
            @PathVariable Integer id,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(authorService.findBooksByAuthor(id, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Create a new author",
            description = "Creates a new author with the provided details. Requires ADMIN or LIBRARIAN role.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Author created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthorDTO.class)
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
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 403, \"title\": \"Forbidden\", \"message\": \"Access denied\", \"timestamp\": \"2025-09-10T19:10:12\", \"path\": \"/api/v1/authors\"}")
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
    public ResponseEntity<AuthorDTO> createAuthor(
            @Parameter(
                    description = "Author details",
                    required = true,
                    schema = @Schema(implementation = AuthorDTO.class),
                    examples = @ExampleObject(value = "{\"name\": \"George Orwell\", \"biography\": \"English novelist and essayist\", \"birthDate\": \"1903-06-25\"}")
            )
            @Valid @RequestBody AuthorDTO authorDTO) {
        return new ResponseEntity<>(authorService.create(authorDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Update an existing author",
            description = "Updates an existing author with the provided details. Requires ADMIN or LIBRARIAN role.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Author updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthorDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Author not found",
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
    public ResponseEntity<AuthorDTO> updateAuthor(
            @Parameter(description = "ID of the author to update", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(
                    description = "Updated author details",
                    required = true,
                    schema = @Schema(implementation = AuthorDTO.class),
                    examples = @ExampleObject(value = "{\"name\": \"George Orwell\", \"biography\": \"Updated biography\", \"birthDate\": \"1903-06-25\"}")
            )
            @Valid @RequestBody AuthorDTO authorDTO) {
        return ResponseEntity.ok(authorService.update(id, authorDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "Delete an author",
            description = "Removes an author from the system. This operation is only allowed for ADMIN users. " +
                    "Cannot delete an author who has books in the library.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Author deleted successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Author not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Cannot delete author with books",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Cannot delete author that has books\", \"timestamp\": \"2025-09-10T19:10:12\", \"path\": \"/api/v1/authors/1\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN users can delete authors",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
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
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "ID of the author to delete", required = true, example = "1")
            @PathVariable Integer id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}