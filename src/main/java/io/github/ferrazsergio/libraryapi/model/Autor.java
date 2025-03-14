package io.github.ferrazsergio.libraryapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "autor", schema = "library")
public class Autor {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "nome" , length = 100 , nullable = false )
    private String nome;
    @Column(name = "data_nascimento" , nullable = false )
    private LocalDate dataNascimento;
    @Column(name = "nacionalidade" , length = 50 , nullable = false )
    private String nacionalidade;
    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;
    @Column(name = "data_atualizacao")
    private LocalDate dataAtualizacao;
    @Column(name = "id_usuario")
    private UUID idUsuario;

}
