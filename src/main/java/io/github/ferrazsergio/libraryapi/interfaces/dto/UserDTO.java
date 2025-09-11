package io.github.ferrazsergio.libraryapi.interfaces.dto;

import io.github.ferrazsergio.libraryapi.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String password; // Apenas para entrada, nunca retornado nas respostas
    private String phone;
    private String address;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Método para converter entidade para DTO (sem retornar a senha)
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}