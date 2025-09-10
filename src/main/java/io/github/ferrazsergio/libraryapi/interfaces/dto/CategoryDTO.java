package io.github.ferrazsergio.libraryapi.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import io.github.ferrazsergio.libraryapi.domain.model.Category;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Integer id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    public static CategoryDTO fromEntity(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}