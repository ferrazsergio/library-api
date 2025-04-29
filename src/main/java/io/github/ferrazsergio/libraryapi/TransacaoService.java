package io.github.ferrazsergio.libraryapi;

import io.github.ferrazsergio.libraryapi.model.Autor;
import io.github.ferrazsergio.libraryapi.model.Livro;
import io.github.ferrazsergio.libraryapi.model.enums.Genero;
import io.github.ferrazsergio.libraryapi.repository.AutorRepository;
import io.github.ferrazsergio.libraryapi.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class TransacaoService {

    @Autowired
    private AutorRepository autorRepository;
    @Autowired
    private LivroRepository livroRepository;


    @Transactional
    public void salvarLivroComFoto(){
        //salva o livro com foto
        //repositorio.save(livro);

        //pega o id do livro
        //livroRepository.findById(livro.getId()).orElseThrow();

        //salvar foto do livro -> bucket na nuvem
        // bucketService.upload(livro.getId(), livro.getFoto());

        //atualizar o nome arquivo que foi salvo
        //livro.setNomeArquivo("nome-arquivo.jpg");
        //repository.save(livro);
    }

    @Transactional
    public void atualizacaoSemAtualizar(){
        var livro = livroRepository.findById(UUID.fromString("a4b5c6d7-e8f9-4a2b-b3c4-d5e6f7g8h9i0")).orElseThrow();
        //livro.setdataPublicacao(LocalDate.of(1998, 7, 31));
    }

    @Transactional
    public void executarTransacao() {
        // salvar um autor
        Autor autor = Autor.builder()
                .nome("J.K. Rowling")
                .nacionalidade("Britânica")
                .dataNascimento(LocalDate.of(1965, 7, 31))
                .build();
        autorRepository.saveAndFlush(autor);



        // salvar um livro
        Livro livro = Livro.builder()
                .titulo("Harry Potter e a Pedra Filosofal")
                .autor(autor)
                .genero(Genero.FANTASIA)
                .dataPublicacao(LocalDate.of(1997, 6, 26))
                .preco(new BigDecimal("39.90"))
                .build();
        livroRepository.saveAndFlush(livro);

        if (autor.getNome().equals("J.K.S")) {
            throw new RuntimeException("Rollback !");
        }
    }
}
