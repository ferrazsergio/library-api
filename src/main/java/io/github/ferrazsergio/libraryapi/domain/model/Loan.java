package io.github.ferrazsergio.libraryapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE &&
                LocalDate.now().isAfter(expectedReturnDate);
    }

    public void returnBook() {
        if (status != LoanStatus.ACTIVE) {
            throw new RuntimeException("This loan is not active");
        }

        status = LoanStatus.RETURNED;
        returnDate = LocalDate.now();
        lastUpdated = LocalDateTime.now();

        // Se o livro estiver em atraso, calcular multa
        if (returnDate.isAfter(expectedReturnDate)) {
            calculateFine();
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
            throw new RuntimeException("Cannot renew a non-active loan");
        }

        if (renewalCount >= 3) {
            throw new RuntimeException("Maximum renewal count reached");
        }

        renewalCount++;
        expectedReturnDate = expectedReturnDate.plusDays(14);
        lastUpdated = LocalDateTime.now();
    }

    public enum LoanStatus {
        ACTIVE, RETURNED, LOST, OVERDUE
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Loan loan = (Loan) o;
        return getId() != null && Objects.equals(getId(), loan.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}