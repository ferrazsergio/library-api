# Library Management System API

This is a comprehensive library management system API built with Java 21, Spring Boot 3.4+, and modern architecture principles.

## Features

- Complete management of books, authors, and categories
- User authentication and authorization with JWT
- Loan management with fine calculation
- Reporting and statistics
- Caching with Redis
- Dockerized for easy deployment

## Prerequisites

- Java 21
- Docker and Docker Compose
- Maven (or use the included wrapper)

## Getting Started

### Running with Docker

The easiest way to get started is using Docker Compose:

```bash
# Build and start all services
docker-compose up -d

# To stop all services
docker-compose down
```

This will start:
- The Library API application on port 8080
- PostgreSQL database on port 5432
- Redis cache on port 6379

### Running Locally for Development

```bash
# Start PostgreSQL and Redis using Docker
docker-compose up -d db redis

# Run the application
./mvnw spring-boot:run
```

## API Documentation

Once the application is running, you can access the OpenAPI documentation at:

- http://localhost:8080/swagger-ui/index.html

## Testing the API

Here are some basic curl commands to test the API:

### Authentication

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin User","email":"admin@example.com","password":"password123","role":"ADMIN"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
```

Copy the JWT token from the login response for subsequent requests.

### Managing Books

```bash
# Create a category first
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"Fiction","description":"Fiction books"}'

# Create an author
curl -X POST http://localhost:8080/api/v1/authors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"George Orwell","biography":"English novelist and essayist","birthDate":"1903-06-25"}'

# Create a book
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "isbn":"9780451524935",
    "title":"1984",
    "description":"Dystopian social science fiction novel",
    "publishDate":"1949-06-08",
    "availableQuantity":5,
    "totalQuantity":5,
    "authorIds":[1],
    "categoryId":1,
    "publisher":"Secker & Warburg"
  }'

# List all books
curl -X GET http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Managing Loans

```bash
# Create a loan
curl -X POST http://localhost:8080/api/v1/loans \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"userId":1,"bookId":1}'

# Return a book
curl -X PUT http://localhost:8080/api/v1/loans/1/return \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=BookServiceTest
```

## Monitoring

The application includes Spring Boot Actuator endpoints for monitoring:

- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus endpoint: http://localhost:8080/actuator/prometheus