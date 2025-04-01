package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import io.github.ferrazsergio.libraryapi.model.Livro;
import io.github.ferrazsergio.libraryapi.model.enums.Genero;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class AutorRepositoryTest {

    @Autowired
    AutorRepository autorRepository;
    @Autowired
    LivroRepository livroRepository;

    @Test
    void salvarAutorTest() {
        Autor autor = Autor.builder()
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
        Autor autor = Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Americana")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        autorRepository.save(autor);
    }

    @Test
    public void listarTodosOsAutoresTest() {
        autorRepository.findAll().forEach(System.out::println);
    }

    @Test
    public void countAutoresTest() {
        long count = autorRepository.count();
        System.out.println("Total de autores: " + count);
    }

    @Test
    public void deletarPorIdTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        autorRepository.deleteById(id);
    }

    @Test
    public void deleteTest() {
        var id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        var autor = autorRepository.findById(id).orElseThrow();
        autorRepository.delete(autor);
    }

    @Test
    void salvarAutorComLivrosTest() {
        Autor autor = Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Britânica")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();

        Livro livro = Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .isbn("978-3-16-148410-0")
                .dataPublicacao(LocalDate.of(1997, 6, 26))
                .genero(Genero.FANTASIA)
                .preco(new BigDecimal("39.90"))
                .autor(autor)
                .build();

        Livro livro2 = Livro.builder()
                .titulo("Harry Potter e a Câmara Secreta")
                .isbn("978-3-16-148410-1")
                .dataPublicacao(LocalDate.of(1998, 7, 2))
                .genero(Genero.FANTASIA)
                .preco(new BigDecimal("39.90"))
                .autor(autor)
                .build();

        autor.setLivros(new ArrayList<>());
        autor.getLivros().add(livro);
        autor.getLivros().add(livro2);

        autorRepository.save(autor);
        livroRepository.saveAll(autor.getLivros());
    }

    @Test
    @Transactional
    void listarLivrosPorAutorTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        Autor autor = autorRepository.findById(id).orElseThrow();
        List<Livro> livrosLista = livroRepository.findbyAutor(autor);
        autor.setLivros(livrosLista);
        autor.getLivros().forEach(livro -> System.out.println("- " + livro.getTitulo()));
    }
}
