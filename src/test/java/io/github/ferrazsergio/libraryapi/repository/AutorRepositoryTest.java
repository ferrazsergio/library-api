package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
class AutorRepositoryTest {

    @Autowired
    AutorRepository autorRepository;

    @Test
    void salvarAutorTest() {
        Autor autor =  Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Britânica")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        autorRepository.save(autor);
    }
    @Test
    void atualizarAutorTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        autorRepository.findById(id).orElseThrow();
        Autor autor =  Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Americana")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        autorRepository.save(autor);
    }
}
