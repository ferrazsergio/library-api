-- Categories
CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT
);

-- Authors
CREATE TABLE authors (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         biography TEXT,
                         birth_date DATE
);

-- Books
CREATE TABLE books (
                       id SERIAL PRIMARY KEY,
                       isbn VARCHAR(20) NOT NULL UNIQUE,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       publish_date DATE,
                       available_quantity INTEGER NOT NULL DEFAULT 0,
                       total_quantity INTEGER NOT NULL DEFAULT 0,
                       publisher VARCHAR(255),
                       category_id INTEGER REFERENCES categories(id),
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Book-Author relationship
CREATE TABLE book_author (
                             book_id INTEGER NOT NULL REFERENCES books(id),
                             author_id INTEGER NOT NULL REFERENCES authors(id),
                             PRIMARY KEY (book_id, author_id)
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       phone VARCHAR(255),
                       address VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP,
                       deleted BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE loans (
                       id SERIAL PRIMARY KEY,
                       user_id INTEGER NOT NULL REFERENCES users(id),
                       book_id INTEGER NOT NULL REFERENCES books(id),
                       loan_date DATE NOT NULL,
                       expected_return_date DATE NOT NULL,
                       return_date DATE,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       last_updated TIMESTAMP,
                       renewal_count INTEGER DEFAULT 0,
                       CONSTRAINT unique_active_loan UNIQUE (user_id, book_id, status)
);

-- Fines
CREATE TABLE fines (
                       id SERIAL PRIMARY KEY,
                       loan_id INTEGER NOT NULL UNIQUE REFERENCES loans(id),
                       amount NUMERIC(10, 2) NOT NULL,
                       paid BOOLEAN NOT NULL DEFAULT FALSE,
                       description TEXT
);

-- Indexes for better performance
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_category ON books(category_id);
CREATE INDEX idx_authors_name ON authors(name);
CREATE INDEX idx_loans_user ON loans(user_id);
CREATE INDEX idx_loans_book ON loans(book_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_expected_return ON loans(expected_return_date);