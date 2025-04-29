package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import io.github.ferrazsergio.libraryapi.model.Livro;
import io.github.ferrazsergio.libraryapi.model.enums.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @see  LivroRepositoryTest
 */
@Repository
public interface LivroRepository  extends JpaRepository<Livro, UUID> {

    //Query Methods
    // select * from livro where autor_id = ?1
    List<Livro> findbyAutor(Autor autor);

    // select * from livro where titulo like %?1%
    List<Livro> findByTituloContainingIgnoreCase(String titulo);

    // select * from livro where isbn like %?1%
    List<Livro> findByisbnContainingIgnoreCase(String isbn);

    // select * from livro where titulo like %?1% and preco = ?2
    List<Livro> findByLivroandPreco(String livro, BigDecimal preco);

    // select * from livro where titulo =  %?1% or isbn = %?2%
    List<Livro> findByTituloOrIsbn(String titulo, String isbn);

    // select * from livro where data_publicacao between ? and ?
    List<Livro> findByDataPublicacaoBetween(LocalDate dataInicio, LocalDate dataFim);

    // JPQL -> referencia a entidade e as propiedades da entidade
    // select l. * order by l.titulo
    @Query("select l from Livro l as l order by l.titulo, l.preco")
    List<Livro> listarTodosOrdenadoPorTituloAndPreco();

    //select distinct l.* from livro l join autor a on l.autor_id = a.id
    @Query("select a from Livro l join l.autor a")
    List<Autor> listarAutoresDosLivros();

    // select distinct l.titulo from livro l
    @Query("select distinct l.titulo from Livro l")
    List<String> listarTitulosLivros();

    @Query(" select l.genero from Livro l join l.autor a where a.nacionalidade = 'Brasileiro' order by l.genero")
    List<String> listarGenerosAutoresBrasileiros();

    // named parameter --> parametros nomeados
    @Query("select l from Livro l where l.genero = :generoLivro order by :paramOrdenacao")
    List<Livro> findByGenero(@Param("generoLivro") Genero generoLivro, @Param("paramOrdenacao") String paramOrdenacao);

    // positional parameters --> parametros posicionais
    @Query("select l from Livro l where l.genero = ?1 order by ?2")
    List<Livro> findByGeneroPositionalParameters(Genero generoLivro, String paramOrdenacao);


    @Modifying
    @Transactional
    @Query("delete from Livro l where l.genero = ?1")
    void deleteByGenero(Genero genero);

    @Modifying
    @Transactional
    @Query("update Livro l set l.dataPublicacao = ?1")
    void updateDataPublicacao(LocalDate novaData);
}