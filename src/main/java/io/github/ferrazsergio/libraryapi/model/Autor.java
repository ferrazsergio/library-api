package io.github.ferrazsergio.libraryapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@ToString(exclude = "livros")
//@RequiredArgsConstructor
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
    @Transient
   //f @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Livro> livros;


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (!thisEffectiveClass.equals(oEffectiveClass)) return false;

        Autor autor = (Autor) o;
        return getId() != null && Objects.equals(getId(), autor.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }


}
