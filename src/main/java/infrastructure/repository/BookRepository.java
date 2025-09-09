package infrastructure.repository;

import model.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId AND b.deleted = false")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.category c WHERE c.id = :categoryId AND b.deleted = false")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.deleted = false")
    Page<Book> findAllNotDeleted(Pageable pageable);

    @Query("""
        SELECT b FROM Book b 
        JOIN Loan l ON l.book.id = b.id 
        GROUP BY b.id 
        ORDER BY COUNT(l.id) DESC
    """)
    List<Book> findMostBorrowedBooks(Pageable pageable);
}