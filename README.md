# 📚 Library Management System API

API robusta e moderna para gerenciamento de bibliotecas, desenvolvida com **Java 21** e **Spring Boot 3.4+**. Controle completo de livros, autores, usuários, empréstimos, multas e estatísticas — pronta para produção, escalável e fácil de integrar com front-ends modernos.

---

## ✨ Features

- **Gestão completa** de livros, autores, categorias e usuários
- **Autenticação JWT** com RBAC (admin, bibliotecário, leitor)
- **Empréstimos, devoluções** e cálculo automático de multas por atraso
- **Relatórios e estatísticas** em endpoints dedicados
- **Cache Redis** para performance
- **Soft delete** para histórico de dados
- **Arquitetura limpa** (DDD, separação de camadas)
- **Documentação OpenAPI/Swagger** automática
- **Testes unitários e integração** com cobertura elevada
- **Pronta para Docker e escalabilidade**

---

## 🛠️ Stack Tecnológica

- **Java 21**, recursos modernos (Records, Pattern Matching, Virtual Threads)
- **Spring Boot 3.4+** (Web, Security, Data JPA, Validation, Actuator)
- **PostgreSQL** & **Flyway**
- **Redis** para cache
- **JUnit 5**, **TestContainers**
- **Docker & Docker Compose**

---

## 🚀 Como rodar

### Pré-requisitos

- Java 21
- Docker e Docker Compose
- Maven (ou use o wrapper incluso)

### Subindo tudo com Docker Compose

```bash
docker-compose up -d
```

- API disponível em `http://localhost:8080`
- PostgreSQL em `localhost:5432`
- Redis em `localhost:6379`

Para parar:

```bash
docker-compose down
```

### Rodando localmente para desenvolvimento

```bash
docker-compose up -d db redis
./mvnw spring-boot:run
```

---

## 📑 Documentação OpenAPI

Após rodar a aplicação, acesse:

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Testando a API (exemplos curl)

### Autenticação

```bash
# Registrar usuário
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin User","email":"admin@example.com","password":"password123","role":"ADMIN"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
```
Guarde o JWT retornado para as próximas requisições!

### Livros, autores e categorias

```bash
# Criar categoria
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"Ficção","description":"Livros de ficção"}'

# Criar autor
curl -X POST http://localhost:8080/api/v1/authors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"George Orwell","biography":"Inglês, romancista","birthDate":"1903-06-25"}'

# Criar livro
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "isbn":"9780451524935",
    "title":"1984",
    "description":"Romance distópico",
    "publishDate":"1949-06-08",
    "availableQuantity":5,
    "totalQuantity":5,
    "authorIds":[1],
    "categoryId":1,
    "publisher":"Secker & Warburg"
  }'

# Listar livros
curl -X GET http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Empréstimos

```bash
# Criar empréstimo
curl -X POST http://localhost:8080/api/v1/loans \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"userId":1,"bookId":1}'

# Devolver livro
curl -X PUT http://localhost:8080/api/v1/loans/1/return \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ✅ Testes

```bash
./mvnw test
# Ou para rodar uma classe específica
./mvnw test -Dtest=BookServiceTest
```

---

## 📊 Monitoramento

- Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Métricas: [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
- Prometheus: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

---

## 📦 Integração com Front-End

Combine com o [library-web](https://github.com/ferrazsergio/library-web) para uma experiência completa de gestão de biblioteca, com dashboard, gráficos, uploads e muito mais!

---

Desenvolvido por [@ferrazsergio](https://github.com/ferrazsergio)  
#Java #SpringBoot #CleanArchitecture #API #Biblioteca #Backend #OpenSource