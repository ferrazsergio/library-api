package io.github.ferrazsergio.libraryapi.infrastructure.repository;

import io.github.ferrazsergio.libraryapi.domain.model.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {

    @Query("SELECT f FROM Fine f WHERE f.loan.user.id = :userId AND f.paid = false")
    List<Fine> findUnpaidFinesByUserId(@Param("userId") Integer userId);

    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.loan.user.id = :userId AND f.paid = false")
    Double getTotalUnpaidFinesForUser(@Param("userId") Integer userId);
}
