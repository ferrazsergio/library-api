package io.github.ferrazsergio.libraryapi.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    public boolean isAvailable() {
        return availableQuantity > 0 && !deleted;
    }

    public void decreaseAvailableQuantity() {
        if (availableQuantity > 0) {
            availableQuantity--;
        } else {
            throw new IllegalStateException("No copies available for loan");
        }
    }

    public void increaseAvailableQuantity() {
        if (availableQuantity < totalQuantity) {
            availableQuantity++;
        } else {
            throw new IllegalStateException("Available quantity cannot exceed total quantity");
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Book book = (Book) o;
        return getId() != null && Objects.equals(getId(), book.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}