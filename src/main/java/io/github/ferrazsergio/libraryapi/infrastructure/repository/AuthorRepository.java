package io.github.ferrazsergio.libraryapi.infrastructure.repository;

import io.github.ferrazsergio.libraryapi.domain.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
    Page<Author> findByBookId(@Param("bookId") Long bookId, Pageable pageable);
}