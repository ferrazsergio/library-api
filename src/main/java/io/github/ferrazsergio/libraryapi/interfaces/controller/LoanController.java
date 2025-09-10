package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.application.service.LoanService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.LoanDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.swagger.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Operations related to book loans, returns, and renewals")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "List all loans with pagination",
            description = "Retrieves a paginated list of all book loans. Only accessible to administrators and librarians.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of loans retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN and LIBRARIAN roles can access this endpoint",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
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
    public ResponseEntity<Page<LoanDTO>> getAllLoans(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(loanService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @loanSecurityService.isLoanOwner(#id, authentication)")
    @Operation(
            summary = "Get loan by ID",
            description = "Retrieves detailed information about a specific loan. Accessible to administrators, librarians, " +
                    "and the user who owns the loan.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loan found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LoanDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"userId\": 1, \"bookId\": 1, \"loanDate\": \"2025-09-01\", \"expectedReturnDate\": \"2025-09-15\", \"status\": \"ACTIVE\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loan not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"title\": \"Not Found\", \"message\": \"Loan not found with ID: 999\", \"timestamp\": \"2025-09-10T19:12:55\", \"path\": \"/api/v1/loans/999\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - You don't have permission to access this loan",
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
    public ResponseEntity<LoanDTO> getLoanById(
            @Parameter(description = "ID of the loan to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(loanService.findById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @userSecurityService.isSameUser(#userId, authentication)")
    @Operation(
            summary = "Get loans by user ID",
            description = "Retrieves a paginated list of all loans for a specific user. Accessible to administrators, " +
                    "librarians, and the user themselves.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of user's loans retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - You don't have permission to access this user's loans",
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
    public ResponseEntity<Page<LoanDTO>> getLoansByUser(
            @Parameter(description = "ID of the user whose loans to retrieve", required = true, example = "1")
            @PathVariable Integer userId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(loanService.findByUser(userId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Create a new loan",
            description = "Creates a new book loan with the provided details. Only accessible to administrators and librarians. " +
                    "This operation checks for book availability and user eligibility (no unpaid fines).",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Loan created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LoanDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data, book not available, or user has unpaid fines",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Book is not available for loan\", \"timestamp\": \"2025-09-10T19:12:55\", \"path\": \"/api/v1/loans\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book or user not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN and LIBRARIAN roles can create loans",
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
    public ResponseEntity<LoanDTO> createLoan(
            @Parameter(
                    description = "Loan details",
                    required = true,
                    schema = @Schema(implementation = LoanDTO.class),
                    examples = @ExampleObject(value = "{\"userId\": 1, \"bookId\": 1}")
            )
            @Valid @RequestBody LoanDTO loanDTO) {
        return new ResponseEntity<>(loanService.createLoan(loanDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Return a borrowed book",
            description = "Processes the return of a borrowed book. Only accessible to administrators and librarians. " +
                    "This operation may generate a fine if the book is returned late.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book returned successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LoanDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"userId\": 1, \"bookId\": 1, \"loanDate\": \"2025-09-01\", \"expectedReturnDate\": \"2025-09-15\", \"returnDate\": \"2025-09-10\", \"status\": \"RETURNED\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loan not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Loan is not active (already returned)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Loan has already been returned\", \"timestamp\": \"2025-09-10T19:12:55\", \"path\": \"/api/v1/loans/1/return\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN and LIBRARIAN roles can process returns",
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
    public ResponseEntity<LoanDTO> returnBook(
            @Parameter(description = "ID of the loan to return", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    @PutMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @loanSecurityService.isLoanOwner(#id, authentication)")
    @Operation(
            summary = "Renew a loan",
            description = "Extends the due date of an active loan. Accessible to administrators, librarians, " +
                    "and the user who owns the loan. The renewal typically adds 14 days to the current due date.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loan renewed successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LoanDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"userId\": 1, \"bookId\": 1, \"loanDate\": \"2025-09-01\", \"expectedReturnDate\": \"2025-09-29\", \"status\": \"ACTIVE\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loan not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Loan is not active (already returned) or has reached maximum renewals",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Cannot renew a returned loan\", \"timestamp\": \"2025-09-10T19:12:55\", \"path\": \"/api/v1/loans/1/renew\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - You don't have permission to renew this loan",
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
    public ResponseEntity<LoanDTO> renewLoan(
            @Parameter(description = "ID of the loan to renew", required = true, example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(loanService.renewLoan(id));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(
            summary = "Get all overdue loans",
            description = "Retrieves a list of all loans that are past their due date. Only accessible to administrators and librarians.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of overdue loans retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN and LIBRARIAN roles can access this endpoint",
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
    public ResponseEntity<List<LoanDTO>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.findOverdueLoans());
    }
}