create table autor(
  id uuid not null primary key,
  nome varchar(100) not null,
  data_nascimento date not null,
  nacionalidade varchar(50) not null,
  data_cadastro timestamp,
  data_atualizacao timestamp,
  id_usuario uuid
);