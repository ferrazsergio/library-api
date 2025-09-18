package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.Author;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.AuthorRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthorDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    @Cacheable(value = "authors", key = "#id", unless = "#result == null")
    public AuthorDTO findById(Integer id) {
        return authorRepository.findById(id)
                .map(AuthorDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<AuthorDTO> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AuthorDTO> findByName(String name, Pageable pageable) {
        return authorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(AuthorDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BookDTO> findBooksByAuthor(Integer authorId, Pageable pageable) {
        // First verify the author exists
        if (!authorRepository.existsById(authorId)) {
            throw new RuntimeException("Author not found with ID: " + authorId);
        }

        return bookRepository.findByAuthorId(authorId, pageable)
                .map(BookDTO::fromEntity);
    }

    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public AuthorDTO create(AuthorDTO authorDTO) {
        Author author = new Author();
        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());
        author.setBirthDate(authorDTO.getBirthDate());

        Author savedAuthor = authorRepository.save(author);

        // Log activity
        activityService.logActivity(
                "AUTHOR_CREATED",
                "Autor criado: " + savedAuthor.getName(),
                null,
                null
        );

        return AuthorDTO.fromEntity(savedAuthor);
    }

    @Transactional
    @CacheEvict(value = "authors", key = "#id")
    public AuthorDTO update(Integer id, AuthorDTO authorDTO) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));

        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());
        author.setBirthDate(authorDTO.getBirthDate());

        Author updatedAuthor = authorRepository.save(author);

        // Log activity
        activityService.logActivity(
                "AUTHOR_UPDATED",
                "Autor atualizado: " + updatedAuthor.getName(),
                null,
                null
        );

        return AuthorDTO.fromEntity(updatedAuthor);
    }

    @Transactional
    @CacheEvict(value = "authors", key = "#id")
    public void delete(Integer id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));

        // Check if author has books
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete author that has books");
        }

        authorRepository.delete(author);

        // Log activity
        activityService.logActivity(
                "AUTHOR_DELETED",
                "Autor removido: " + author.getName(),
                null,
                null
        );
    }
}