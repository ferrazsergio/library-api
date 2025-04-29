package io.github.ferrazsergio.libraryapi.repository;

import io.github.ferrazsergio.libraryapi.TransacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class TransacoesTest {

    @Autowired
    TransacaoService transacaoService;

    /**
     * Commit - grava os dados no banco de dados
     * Rollback - desfaz as alterações no banco de dados
     */
    @Test
    void transacaoTest() {
       transacaoService.executarTransacao();
    }

    @Test
    void atualizacaoSemAtualizarTest() {
        transacaoService.atualizacaoSemAtualizar();
    }
}
