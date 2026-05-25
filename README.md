
# Personal Finance Manager

A RESTful backend application built with **Spring Boot 3.2** and **Java 17** for managing personal finances. It features secure user authentication, transaction tracking, and budget management — all backed by an in-memory H2 database for easy local development.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Security | Spring Security |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Build | Maven |
| Containerization | Docker |
| Utilities | Lombok |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker (optional, for containerized runs)

---

## Getting Started

### Run Locally

```bash
# Clone the repository
git clone https://github.com/SRAJAN1405/syfe.git
cd syfe

# Build and run
mvn clean spring-boot:run
```

The application starts on **http://localhost:8080**.

### Run with Docker

```bash
# Build the Docker image
docker build -t personal-finance-manager .

# Run the container
docker run -p 8080:8080 personal-finance-manager
```

---

## H2 Console (Dev)

The in-memory H2 database console is accessible at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:testdb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## API Endpoint Testing

All endpoints are secured with **Spring Security**. Unless otherwise noted, requests require HTTP Basic Auth or a valid session token. Use the credentials registered via the Auth endpoints below.

---

### Authentication Endpoints

#### Register a New User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "StrongPass@123",
  "email": "john@example.com"
}
```

**Expected Response: `201 Created`**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

---

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "StrongPass@123"
}
```

**Expected Response: `200 OK`**
```json
{
  "token": "<jwt-or-session-token>",
  "username": "john_doe"
}
```

> Use the returned token as a Bearer token or configure Basic Auth for subsequent requests.

---

### Transaction Endpoints

All transaction endpoints require authentication.

#### Create a Transaction

```http
POST /api/transactions
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
Content-Type: application/json

{
  "type": "EXPENSE",
  "amount": 250.00,
  "category": "Groceries",
  "description": "Weekly grocery run",
  "date": "2026-05-25"
}
```

**Expected Response: `201 Created`**
```json
{
  "id": 1,
  "type": "EXPENSE",
  "amount": 250.00,
  "category": "Groceries",
  "description": "Weekly grocery run",
  "date": "2026-05-25"
}
```

---

#### Get All Transactions

```http
GET /api/transactions
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
```

**Expected Response: `200 OK`**
```json
[
  {
    "id": 1,
    "type": "EXPENSE",
    "amount": 250.00,
    "category": "Groceries",
    "date": "2026-05-25"
  }
]
```

---

#### Get Transaction by ID

```http
GET /api/transactions/{id}
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
```

**Expected Response: `200 OK`** (or `404 Not Found` if the ID doesn't exist)

---

#### Update a Transaction

```http
PUT /api/transactions/{id}
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
Content-Type: application/json

{
  "type": "EXPENSE",
  "amount": 300.00,
  "category": "Groceries",
  "description": "Monthly grocery run",
  "date": "2026-05-25"
}
```

**Expected Response: `200 OK`**

---

#### Delete a Transaction

```http
DELETE /api/transactions/{id}
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
```

**Expected Response: `204 No Content`**

---

### Budget Endpoints

#### Create a Budget

```http
POST /api/budgets
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
Content-Type: application/json

{
  "category": "Groceries",
  "limit": 5000.00,
  "month": "2026-05"
}
```

**Expected Response: `201 Created`**

---

#### Get All Budgets

```http
GET /api/budgets
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
```

**Expected Response: `200 OK`**

---

#### Get Budget Summary (Spent vs Limit)

```http
GET /api/budgets/summary
Authorization: Basic am9obl9kb2U6U3Ryb25nUGFzc0AxMjM=
```

**Expected Response: `200 OK`**
```json
[
  {
    "category": "Groceries",
    "limit": 5000.00,
    "spent": 250.00,
    "remaining": 4750.00
  }
]
```

---

### Testing with cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"StrongPass@123","email":"john@example.com"}'

# Get transactions (Basic Auth)
curl -u john_doe:StrongPass@123 http://localhost:8080/api/transactions

# Create a transaction
curl -X POST http://localhost:8080/api/transactions \
  -u john_doe:StrongPass@123 \
  -H "Content-Type: application/json" \
  -d '{"type":"EXPENSE","amount":250,"category":"Groceries","date":"2026-05-25"}'
```

---

### Testing with Postman

1. Import a new request collection.
2. Set base URL to `http://localhost:8080`.
3. Under the **Authorization** tab, choose **Basic Auth** and enter your username/password.
4. Use the request bodies from the examples above.
5. For bulk testing, create an **Environment** with a `base_url` variable set to `http://localhost:8080`.

---

### Common HTTP Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `201 Created` | Resource created successfully |
| `204 No Content` | Deleted successfully |
| `400 Bad Request` | Validation error — check request body |
| `401 Unauthorized` | Missing or invalid credentials |
| `403 Forbidden` | Authenticated but not authorized |
| `404 Not Found` | Resource does not exist |
| `500 Internal Server Error` | Unexpected server error |

---

## Running Tests

```bash
# Run all unit and integration tests
mvn test

# Run tests and generate a report
mvn verify
```

Spring Security test support is included via `spring-security-test`, so controller tests can use `@WithMockUser` for simulating authenticated requests without a live auth flow.

---

## Project Structure

```
src/
├── main/
│   └── java/com/finance/
│       ├── controller/     # REST controllers
│       ├── service/        # Business logic
│       ├── repository/     # JPA repositories
│       ├── model/          # Entity classes
│       ├── dto/            # Request/Response DTOs
│       └── security/       # Spring Security config
└── test/
    └── java/com/finance/   # Unit and integration tests
```

---

## License

This project is open source and available under the [MIT License](LICENSE).