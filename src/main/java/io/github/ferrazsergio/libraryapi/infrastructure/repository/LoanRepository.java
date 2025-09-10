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
}