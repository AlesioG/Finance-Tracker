# Personal Finance Tracker API

REST API for a personal finance tracker built with Java 17, Spring Boot 3, Spring Security, JWT, JPA/Hibernate, and PostgreSQL.

## Features

- User registration and login with JWT authentication
- Role-based access control with USER and ADMIN
- Account CRUD for authenticated users
- Deposit, withdrawal, and transfer transactions
- Transaction history with optional filtering by type and date range
- Input validation and global exception handling
- Service-layer unit tests with JUnit 5 and Mockito

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit 5
- Mockito

## Project Structure

src/main/java/lfh/project/financetracker
- config
- controller
- dto
- entity
- exception
- repository
- security
- service

## Prerequisites

Make sure you have installed:

- Java 17+
- Maven or use the included Maven Wrapper
- PostgreSQL

## Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE financetracker;
```
Update src/main/resources/application.properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financetracker
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

app.jwt.secret=your-very-long-secret-key-with-at-least-32-characters
app.jwt.expiration=86400000
```

## Running the Application

Using Maven Wrapper:
```bash
./mvnw spring-boot:run
```
The application will start on:
```url
http://localhost:8081
```
## Running Tests:
```bash
./mvnw test
```
## Authentication
### Register
```URL
POST /auth/register
```
Request:
```JSON
{
"email": "user@example.com",
"password": "password123"
}
```
Response:
```JSON
{
"token": "jwt_token_here"
}
```
### Login
```URL  
POST /auth/login
```

Request:
```JSON
{
"email": "user@example.com",
"password": "password123"
}
```

Response:
```JSON
{
"token": "jwt_token_here"
}
```
## Authorization

For protected endpoints, include this header:
```json
Authorization: Bearer <jwt_token>
```
## Endpoints
### Accounts
Create account

```URL  
POST /accounts
```
Request:
```JSON
{
"name": "Checking",
"initialBalance": 1000.00
}
```
Response:
```JSON
{
"id": 1,
"name": "Checking",
"balance": 1000.00
}
```
Get all accounts
```URL
GET /accounts
```
Get account by id
```URL
GET /accounts/{id}
```
Update account
```URL
PUT /accounts/{id}
```
Request:
```JSON
{
"name": "Main Checking"
}
```
Delete account
```URL
DELETE /accounts/{id}
```
### Transactions
Deposit
```URL
POST /transactions/deposit
```
Request:
```JSON
{
"accountId": 1,
"amount": 200.00
}
```
Withdraw
```URL
POST /transactions/withdraw
```
Request:
```JSON
{
"accountId": 1,
"amount": 50.00
}
```
Transfer
```URL
POST /transactions/transfer
```
Request:
```JSON
{
"fromAccountId": 1,
"toAccountId": 2,
"amount": 100.00
}
```
Transaction history
```URL
GET /transactions
```
Optional query params:

- type

- startDate

- endDate

Examples:
```URL
GET /transactions?type=DEPOSIT
GET /transactions?startDate=2026-03-01&endDate=2026-03-08
GET /transactions?type=TRANSFER&startDate=2026-03-01&endDate=2026-03-08
```
## Validation and Security Notes

- All protected endpoints require authentication

- Users can only access and modify their own accounts

- Transfers are only allowed between accounts owned by the authenticated user

- Negative amounts are rejected

- Overdrafts are rejected

- Passwords are stored using BCrypt hashing

- JWT is used for stateless authentication

## Design Decisions
### Layered architecture

The application follows a layered structure:

* Controller: handles HTTP requests and responses

* Service: contains business logic

* Repository: handles data access

### DTO usage

DTOs are used to avoid exposing entities directly in API responses and to keep request validation separate from persistence models.

### Security approach

Spring Security with JWT was used to secure the API. Public access is limited to register and login endpoints, while all other endpoints require a valid token.

### Balance updates

Account balances are only changed through transaction operations such as deposit, withdrawal, and transfer. The account update endpoint does not allow direct balance modification.

### Database choice

PostgreSQL was used because it reflects better a real-world backend setup.

### Testing

Service-layer unit tests cover:

- successful operations

- validation and business-rule failures

- unauthorized ownership/access scenarios

Main tested services:

- AccountService

- TransactionService

- AuthService

In addition to service-layer unit tests, integration tests were added for transaction workflows using MockMvc to verify authentication, account creation, transaction processing, and transaction history retrieval end-to-end.
  
### Notes / Limitations

- Admin role exists but admin-specific management endpoints were not implemented because they were not required by the assignment

- Rate limiting was not implemented

- Docker Compose was not implemented

- With more time, I would add structured logging, Swagger/OpenAPI documentation, and integration tests

### Possible Improvements

- Docker Compose for app + PostgreSQL
- Rate limiting on auth endpoints
- Integration tests for controllers
- OpenAPI/Swagger documentation
- Better error response structure
- Audit logging for transactions