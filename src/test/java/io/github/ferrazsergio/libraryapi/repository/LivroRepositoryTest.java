package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import io.github.ferrazsergio.libraryapi.model.Livro;
import io.github.ferrazsergio.libraryapi.model.enums.Genero;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
class LivroRepositoryTest {

    @Autowired
    LivroRepository livroRepository;
    @Autowired
    AutorRepository autorRepository;

    @Test
    void salvarLivroTest() {
        Autor autor = autorRepository.findById(UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0")).orElseThrow();
        Livro livro = Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .autor(autor)
                .genero(Genero.FANTASIA)
                .dataPublicacao(LocalDate.of(1997, 6, 26))
                .preco(new BigDecimal("39.90"))
                .build();
         livroRepository.save(livro);
    }
    
    @Test
    void salvarAutorELivroTest() {
        Livro livro = Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .genero(Genero.FANTASIA)
                .dataPublicacao(LocalDate.of(1997, 6, 26))
                .preco(new BigDecimal("39.90"))
                .build();

        Autor autor =  Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Britânica")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        autorRepository.save(autor);
        livro.setAutor(autor);
        livroRepository.save(livro);
    }
    @Test
    void salvarCascadeTest() {
        Livro livro = Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .genero(Genero.FANTASIA)
                .dataPublicacao(LocalDate.of(1997, 6, 26))
                .preco(new BigDecimal("39.90"))
                .build();

        Autor autor =  Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Britânica")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        livro.setAutor(autor);
        livroRepository.save(livro);
    }

    @Test
    void atualizarAutorELivroTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        Autor autor = autorRepository.findById(id).orElseThrow();
        Livro livro = livroRepository.findById(id).orElseThrow();
        livro.setAutor(autor);
        livro.setTitulo("Harry Potter e a Câmara Secreta");
        livro.setGenero(Genero.FANTASIA);
        livro.setDataPublicacao(LocalDate.of(1998, 7, 2));
        livro.setPreco(new BigDecimal("39.90"));
        livroRepository.save(livro);
    }

    @Test
    void deletarPorIdTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        livroRepository.deleteById(id);
    }

    @Test
    void deleteCascadeTest(){
        var id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        var livro = livroRepository.findById(id).orElseThrow();
        livroRepository.delete(livro);
    }

    @Test
    void buscarLivroPorIdTest() {
        UUID id = UUID.fromString("f3b8c5a0-4d2e-4b1c-8f7d-6a2e5f3b8c5a0");
        Livro livro = livroRepository.findById(id).orElseThrow();
        System.out.println(livro);
    }

    @Test
    void pesquisaPorTitulo(){
        String titulo = "Harry Potter";
        livroRepository.findByTituloContainingIgnoreCase(titulo).forEach(System.out::println);
    }
    @Test
    void pesquisaPorIsbn(){
        String isbn = "978-3-16-148410-0";
        livroRepository.findByisbnContainingIgnoreCase(isbn).forEach(System.out::println);
    }
    @Test
    void pesquisaPorTituloEPreco(){
        var titulo = "Harry Potter";
        var preco = BigDecimal.valueOf(39.90);
        livroRepository.findByLivroandPreco(titulo, preco).forEach(System.out::println);
    }
    @Test
    void listarLivrosComQueryJPQL() {
        livroRepository.listarTodosOrdenadoPorTituloAndPreco().forEach(System.out::println);
    }

    @Test
    void listarAutoresDosLivros(){
        livroRepository.listarAutoresDosLivros().forEach(System.out::println);
    }

    @Test
    void listarTitulosLivros(){
        livroRepository.listarTitulosLivros().forEach(System.out::println);
    }

    @Test
    void listarGenerosAutoresBrasileiros(){
        livroRepository.listarGenerosAutoresBrasileiros().forEach(System.out::println);
    }

    @Test
    void listarLivrosPorGenero(){
        var genero = Genero.FANTASIA;
        var paramOrdenacao = "titulo";
        livroRepository.findByGenero(genero, paramOrdenacao).forEach(System.out::println);
    }

    @Test
    void listarLivrosPorGeneroPositionalParameters(){
        var genero = Genero.FANTASIA;
        var paramOrdenacao = "titulo";
        livroRepository.findByGeneroPositionalParameters(genero, paramOrdenacao).forEach(System.out::println);
    }

    @Test
    void deletarLivrosPorGenero(){
        var genero = Genero.FANTASIA;
        livroRepository.deleteByGenero(genero);
    }
    @Test
    void updateDataPublicacao(){
        var dataPublicacao = LocalDate.of(1998, 7, 2);
        livroRepository.updateDataPublicacao(dataPublicacao);
    }
}