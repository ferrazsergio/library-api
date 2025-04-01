package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.model.Autor;
import io.github.ferrazsergio.libraryapi.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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
}
