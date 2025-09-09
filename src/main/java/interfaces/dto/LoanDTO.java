package interfaces.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import model.domain.Loan;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private LocalDate loanDate;

    private LocalDate expectedReturnDate;

    private LocalDate returnDate;

    private String status;

    // Additional fields for response
    private UserDTO user;
    private BookDTO book;
    private FineDTO fine;

    public static LoanDTO fromEntity(Loan loan) {
        LoanDTO dto = LoanDTO.builder()
                .id(loan.getId())
                .loanDate(loan.getLoanDate())
                .expectedReturnDate(loan.getExpectedReturnDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus().name())
                .build();

        if (loan.getUser() != null) {
            dto.setUserId(loan.getUser().getId());
            dto.setUser(UserDTO.fromEntity(loan.getUser()));
        }

        if (loan.getBook() != null) {
            dto.setBookId(loan.getBook().getId());
            dto.setBook(BookDTO.fromEntity(loan.getBook()));
        }

        if (loan.getFine() != null) {
            dto.setFine(FineDTO.fromEntity(loan.getFine()));
        }

        return dto;
    }
}