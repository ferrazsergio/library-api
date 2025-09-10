package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.CategoryService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.CategoryDTO;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Operations related to book category management")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "List all categories with pagination",
            description = "Retrieves a paginated list of all book categories. Results can be sorted and paginated using query parameters.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of categories retrieved successfully",
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
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves detailed information about a specific book category by its unique identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Category found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"name\": \"Fiction\", \"description\": \"Fiction books including novels and short stories\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Category not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"title\": \"Not Found\", \"message\": \"Category not found with ID: 999\", \"timestamp\": \"2025-09-10T19:11:52\", \"path\": \"/api/v1/categories/999\"}")
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
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(
            summary = "Get category by name",
            description = "Retrieves a category by its exact name (case sensitive)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Category found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Category not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"title\": \"Not Found\", \"message\": \"Category not found with name: NonExistentCategory\", \"timestamp\": \"2025-09-10T19:11:52\", \"path\": \"/api/v1/categories/name/NonExistentCategory\"}")
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
    public ResponseEntity<CategoryDTO> getCategoryByName(
            @Parameter(description = "Name of the category to retrieve", required = true, example = "Fiction")
            @PathVariable String name) {
        return ResponseEntity.ok(categoryService.findByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Create a new category",
            description = "Creates a new book category with the provided details. Requires ADMIN or LIBRARIAN role.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Category created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data or category with same name already exists",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Category with name 'Fiction' already exists\", \"timestamp\": \"2025-09-10T19:11:52\", \"path\": \"/api/v1/categories\"}")
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
    public ResponseEntity<CategoryDTO> createCategory(
            @Parameter(
                    description = "Category details",
                    required = true,
                    schema = @Schema(implementation = CategoryDTO.class),
                    examples = @ExampleObject(value = "{\"name\": \"Fiction\", \"description\": \"Fiction books including novels and short stories\"}")
            )
            @Valid @RequestBody CategoryDTO categoryDTO) {
        return new ResponseEntity<>(categoryService.create(categoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Update an existing category",
            description = "Updates an existing book category with the provided details. Requires ADMIN or LIBRARIAN role.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Category updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Category not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data or category name conflict",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Another category already exists with name: SciFi\", \"timestamp\": \"2025-09-10T19:11:52\", \"path\": \"/api/v1/categories/1\"}")
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
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "ID of the category to update", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(
                    description = "Updated category details",
                    required = true,
                    schema = @Schema(implementation = CategoryDTO.class),
                    examples = @ExampleObject(value = "{\"name\": \"Fiction\", \"description\": \"Updated description for fiction category\"}")
            )
            @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.update(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "Delete a category",
            description = "Removes a book category from the system. This operation is only allowed for ADMIN users. " +
                    "Cannot delete a category that is assigned to any books.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Category deleted successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Category not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Cannot delete category that is used by books",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Cannot delete category that is used by books\", \"timestamp\": \"2025-09-10T19:11:52\", \"path\": \"/api/v1/categories/1\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN users can delete categories",
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
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete", required = true, example = "1")
            @PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}