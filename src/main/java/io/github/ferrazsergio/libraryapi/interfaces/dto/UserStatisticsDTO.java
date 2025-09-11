package io.github.ferrazsergio.libraryapi.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private Integer userId;
    private String name;
    private String email;
    private Long loanCount;
}