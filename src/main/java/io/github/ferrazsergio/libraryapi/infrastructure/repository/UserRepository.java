package io.github.ferrazsergio.libraryapi.infrastructure.repository;


import io.github.ferrazsergio.libraryapi.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        JOIN Loan l ON l.user.id = u.id
        WHERE l.status = 'ACTIVE' AND l.expectedReturnDate < CURRENT_DATE
        GROUP BY u.id
    """)
    List<User> findUsersWithOverdueLoans();


    Page<User> findByDeletedFalse(Pageable pageable);

    long countByDeletedFalse();

    long countByCreatedAtAfterAndDeletedFalse(LocalDateTime date);

    Page<User> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Busca os usuários com mais empréstimos.
     */
    @Query("SELECT u, COUNT(l) as loanCount FROM User u JOIN Loan l ON u.id = l.user.id " +
            "WHERE u.deleted = false GROUP BY u.id ORDER BY loanCount DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);

    /**
     * Conta usuários que fizeram empréstimos após uma determinada data.
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN Loan l ON u.id = l.user.id " +
            "WHERE u.deleted = false AND l.loanDate >= :date")
    long countUsersWithLoansAfter(@Param("date") LocalDateTime date);
}