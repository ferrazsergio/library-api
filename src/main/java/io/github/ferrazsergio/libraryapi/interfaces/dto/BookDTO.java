package io.github.ferrazsergio.libraryapi.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.github.ferrazsergio.libraryapi.domain.model.Book;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Integer id;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private LocalDate publishDate;

    @NotNull(message = "Available quantity is required")
    @Positive(message = "Available quantity must be positive")
    private Integer availableQuantity;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Total quantity must be positive")
    private Integer totalQuantity;

    private Set<Integer> authorIds;

    private Integer categoryId;

    private String publisher;

    // Additional fields for responses
    private Set<AuthorDTO> authors;
    private CategoryDTO category;

    public static BookDTO fromEntity(Book book) {
        BookDTO dto = BookDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .description(book.getDescription())
                .publishDate(book.getPublishDate())
                .availableQuantity(book.getAvailableQuantity())
                .totalQuantity(book.getTotalQuantity())
                .publisher(book.getPublisher())
                .build();

        if (book.getCategory() != null) {
            dto.setCategory(CategoryDTO.fromEntity(book.getCategory()));
            dto.setCategoryId(book.getCategory().getId());
        }

        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            dto.setAuthors(book.getAuthors().stream()
                    .map(AuthorDTO::fromEntity)
                    .collect(Collectors.toSet()));

            dto.setAuthorIds(book.getAuthors().stream()
                    .map(author -> author.getId())
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}