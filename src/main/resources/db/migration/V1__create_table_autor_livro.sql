CREATE SCHEMA IF NOT EXISTS library;

CREATE TABLE library.autor (
                               id UUID NOT NULL PRIMARY KEY,
                               nome VARCHAR(100) NOT NULL,
                               data_nascimento DATE NOT NULL,
                               nacionalidade VARCHAR(50) NOT NULL,
                               data_cadastro TIMESTAMP,
                               data_atualizacao TIMESTAMP,
                               id_usuario UUID
);

CREATE TABLE library.livro (
                               id UUID NOT NULL PRIMARY KEY,
                               isbn VARCHAR(20) NOT NULL UNIQUE,
                               titulo VARCHAR(150) NOT NULL,
                               data_publicacao DATE NOT NULL,
                               genero VARCHAR(30) NOT NULL,
                               preco NUMERIC(5, 2) NOT NULL,
                               data_cadastro TIMESTAMP,
                               data_atualizacao TIMESTAMP,
                               id_usuario UUID,
                               id_autor UUID NOT NULL REFERENCES library.autor(id),
                               CONSTRAINT chk_genero CHECK (genero IN ('FICCAO', 'FANTASIA', 'MISTERIO', 'ROMANCE', 'BIOGRAFIA', 'CIENCIA'))
);