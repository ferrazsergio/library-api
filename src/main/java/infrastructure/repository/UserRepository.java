package infrastructure.repository;


import model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        JOIN Loan l ON l.user.id = u.id
        WHERE l.status = 'ACTIVE' AND l.expectedReturnDate < CURRENT_DATE
        GROUP BY u.id
    """)
    List<User> findUsersWithOverdueLoans();
}