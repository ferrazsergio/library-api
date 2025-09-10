package io.github.ferrazsergio.libraryapi.interfaces.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed error response for API errors")
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error title", example = "Bad Request")
    private String title;

    @Schema(description = "Detailed error message", example = "Validation failed for input data")
    private String message;

    @Schema(description = "Error timestamp", example = "2025-09-10T18:58:53")
    private LocalDateTime timestamp;

    @Schema(description = "Request path that caused the error", example = "/api/v1/books")
    private String path;

    @Schema(description = "Detailed validation errors (if applicable)")
    private List<ValidationError> errors;

    @Schema(description = "Trace ID for log correlation")
    private String traceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field validation error details")
    public static class ValidationError {

        @Schema(description = "Field name with error", example = "isbn")
        private String field;

        @Schema(description = "Validation error message", example = "ISBN is required")
        private String message;

        @Schema(description = "Rejected value", example = "")
        private String rejectedValue;
    }
}