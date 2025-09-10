package io.github.ferrazsergio.libraryapi.interfaces.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import io.github.ferrazsergio.libraryapi.domain.model.Author;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {

    private Integer id;

    @NotBlank(message = "Author name is required")
    private String name;

    private String biography;

    private LocalDate birthDate;

    public static AuthorDTO fromEntity(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .birthDate(author.getBirthDate())
                .build();
    }
}