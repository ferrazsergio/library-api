-- Popular categorias
INSERT INTO categories (name, description) VALUES
                                               ('Romance', 'Livros de romance'),
                                               ('Ficção Científica', 'Livros de ficção científica'),
                                               ('Biografia', 'Livros biográficos'),
                                               ('Tecnologia', 'Livros sobre tecnologia');

-- Popular autores
INSERT INTO authors (name, biography, birth_date) VALUES
                                                      ('Sérgio Ferraz Da Silva Junior', 'Autor brasileiro contemporâneo.', '1987-09-17'),
                                                      ('Ana Maria Machado', 'Uma das maiores escritoras brasileiras.', '1941-12-24'),
                                                      ('Isaac Asimov', 'Autor russo-americano de ficção científica.', '1920-01-02'),
                                                      ('Steve Jobs', 'Cofundador da Apple.', '1955-02-24');

-- Popular livros
INSERT INTO books (isbn, title, description, publish_date, available_quantity, total_quantity, publisher, category_id)
VALUES
    ('978000000001', 'O Amor em Tempos de Cólera', 'Um clássico do romance.', '1985-03-01', 3, 5, 'Editora Alfa', 1),
    ('978000000002', 'Fundação', 'Ficção científica seminal.', '1951-06-01', 2, 2, 'Editora Beta', 2),
    ('978000000003', 'Steve Jobs', 'Biografia do fundador da Apple.', '2011-10-24', 1, 1, 'Editora Gamma', 3),
    ('978000000004', 'Clean Code', 'Livro sobre boas práticas de programação.', '2008-08-01', 4, 4, 'Prentice Hall', 4);

-- Relacionar livros e autores
INSERT INTO book_author (book_id, author_id) VALUES
                                                 (1, 1), -- O Amor em Tempos de Cólera - Sérgio Ferraz Da Silva Junior
                                                 (2, 3), -- Fundação - Isaac Asimov
                                                 (3, 4), -- Steve Jobs - Steve Jobs (biografia, mas pode usar o próprio para teste)
                                                 (4, 2), -- Clean Code - Ana Maria Machado (exemplo, pode trocar pelo correto)
                                                 (4, 1); -- Clean Code - Sérgio Ferraz Da Silva Junior (multi-autoria exemplo)

-- Popular usuários
INSERT INTO users (name, email, password, role, phone, address, created_at, status)
VALUES
    ('Administrador', 'admin@teste.com', '$2b$10$abcdefg1234567890', 'ADMIN', '11999999999', 'Rua 1, São Paulo', NOW(), 'ACTIVE'),
    ('Bibliotecário', 'bibli@teste.com', '$2b$10$abcdefg1234567890', 'LIBRARIAN', '11988888888', 'Rua 2, Rio', NOW(), 'ACTIVE'),
    ('Leitor', 'leitor@teste.com', '$2b$10$abcdefg1234567890', 'READER', '11977777777', 'Rua 3, BH', NOW(), 'ACTIVE');

-- Popular empréstimos
INSERT INTO loans (user_id, book_id, loan_date, expected_return_date, status)
VALUES
    (3, 1, '2025-09-15', '2025-09-22', 'ACTIVE'),
    (3, 2, '2025-09-10', '2025-09-17', 'RETURNED'),
    (2, 3, '2025-09-12', '2025-09-19', 'OVERDUE');

-- Popular multas
INSERT INTO fines (loan_id, amount, paid, description) VALUES
                                                           (2, 5.50, TRUE, 'Atraso de devolução'),
                                                           (3, 10.00, FALSE, 'Empréstimo muito atrasado');

-- Popular atividades
INSERT INTO activities (activity_type, description, user_name, book_title)
VALUES
    ('LOGIN', 'Usuário Administrador fez login.', 'Administrador', NULL),
    ('EMPRESTIMO', 'Leitor pegou "O Amor em Tempos de Cólera" emprestado.', 'Leitor', 'O Amor em Tempos de Cólera'),
    ('MULTA', 'Gerada multa para empréstimo atrasado.', 'Leitor', 'Fundação'),
    ('DEVOLUCAO', 'Livro "Fundação" devolvido.', 'Leitor', 'Fundação');