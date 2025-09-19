package io.github.ferrazsergio.libraryapi.infrastructure.repository;

import io.github.ferrazsergio.libraryapi.domain.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    Page<Loan> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId")
    Page<Loan> findByBookId(@Param("bookId") Integer bookId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.expectedReturnDate < CURRENT_DATE")
    List<Loan> findAllOverdueLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'ACTIVE' AND l.book.id = :bookId")
    long countActiveLoansForBook(@Param("bookId") Integer bookId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'OVERDUE'")
    long countOverdueLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'RETURNED' AND (l.returnDate <= l.expectedReturnDate)")
    long countReturnedOnTime();

    @Query("SELECT l FROM Loan l ORDER BY " +
            "CASE " +
            "  WHEN l.returnDate IS NOT NULL THEN l.returnDate " +
            "  WHEN l.lastUpdated IS NOT NULL THEN l.lastUpdated " +
            "  ELSE l.loanDate " +
            "END DESC")
    List<Loan> findRecentLoanActivities(Pageable pageable);

    @Query("SELECT FUNCTION('MONTH', l.loanDate) as month, COUNT(l) " +
            "FROM Loan l " +
            "WHERE l.loanDate >= FUNCTION('DATE_SUB', CURRENT_DATE, 180) " +
            "GROUP BY FUNCTION('MONTH', l.loanDate) " +
            "ORDER BY month")
    List<Object[]> getLoanStatisticsByMonth();

    /**
     * Conta empréstimos por status.
     *
     * @param status status do empréstimo
     * @return contagem de empréstimos com o status especificado
     */
    long countByStatus(Loan.LoanStatus status);

    /**
     * Retorna o número total de empréstimos.
     *
     * @return total de empréstimos
     */
    @Query("SELECT COUNT(l) FROM Loan l")
    long getTotalLoans();
}