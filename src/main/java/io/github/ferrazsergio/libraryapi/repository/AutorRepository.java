package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface AutorRepository extends JpaRepository<Autor, UUID> {

}
