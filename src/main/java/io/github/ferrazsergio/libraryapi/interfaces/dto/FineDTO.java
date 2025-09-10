package io.github.ferrazsergio.libraryapi.interfaces.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.github.ferrazsergio.libraryapi.domain.model.Fine;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineDTO {

    private Integer id;
    private Integer loanId;
    private BigDecimal amount;
    private boolean paid;
    private String description;

    public static FineDTO fromEntity(Fine fine) {
        return FineDTO.builder()
                .id(fine.getId())
                .loanId(fine.getLoan().getId())
                .amount(fine.getAmount())
                .paid(fine.isPaid())
                .description(fine.getDescription())
                .build();
    }
}