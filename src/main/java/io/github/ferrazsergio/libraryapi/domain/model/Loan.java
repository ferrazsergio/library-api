package io.github.ferrazsergio.libraryapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "expected_return_date", nullable = false)
    private LocalDate expectedReturnDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @OneToOne(mappedBy = "loan", cascade = CascadeType.ALL)
    private Fine fine;

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE &&
                LocalDate.now().isAfter(expectedReturnDate);
    }

    public void returnBook() {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Cannot return a book that is not currently loaned");
        }

        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;

        if (isOverdue()) {
            this.calculateFine();
        }
    }

    private void calculateFine() {
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(expectedReturnDate, returnDate);
        BigDecimal fineAmount = BigDecimal.valueOf(daysLate * 0.50); // $0.50 per day

        if (this.fine == null) {
            this.fine = new Fine();
            this.fine.setLoan(this);
        }

        this.fine.setAmount(fineAmount);
        this.fine.setDescription("Late return fine: " + daysLate + " days overdue");
    }

    public void renew() {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Cannot renew a loan that is not active");
        }

        if (isOverdue()) {
            throw new IllegalStateException("Cannot renew an overdue loan");
        }

        // Add standard loan period (e.g., 14 days) to the current expected return date
        this.expectedReturnDate = this.expectedReturnDate.plusDays(14);
    }

    public enum LoanStatus {
        ACTIVE, RETURNED, LOST
    }
}