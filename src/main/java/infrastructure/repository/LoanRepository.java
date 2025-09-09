package infrastructure.repository;

import model.domain.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    Page<Loan> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId")
    Page<Loan> findByBookId(@Param("bookId") Long bookId, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.expectedReturnDate < CURRENT_DATE")
    List<Loan> findAllOverdueLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'ACTIVE' AND l.book.id = :bookId")
    long countActiveLoansForBook(@Param("bookId") Long bookId);
}