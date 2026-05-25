# Personal Finance Manager — Spring Boot REST API

A backend REST API for managing personal finances: track income/expenses, create custom categories, set savings goals, and generate monthly/yearly reports.

---

## Prerequisites (Windows 11)

Install the following before running the project:

### 1. Java 17 (JDK)
- Download from: https://adoptium.net/temurin/releases/?version=17
- Choose: Windows x64 `.msi` installer
- Run the installer — it sets up `JAVA_HOME` automatically

### 2. Apache Maven
- Download from: https://maven.apache.org/download.cgi
- Download the **Binary zip archive** (e.g. `apache-maven-3.9.x-bin.zip`)
- Extract to `C:\Program Files\Maven\`
- Add `C:\Program Files\Maven\bin` to your System PATH

### 3. Verify Installation
Open **Command Prompt** and run:
```
java -version
mvn -version
```
Both should print version info without errors.

---

## How to Run

### Step 1 — Open Command Prompt in project folder
- Unzip the downloaded `personal-finance-manager.zip`
- Open the folder, click the address bar, type `cmd`, press Enter

### Step 2 — Build the project
```
mvn clean package -DskipTests
```

### Step 3 — Run the application
```
mvn spring-boot:run
```
OR run the compiled JAR:
```
java -jar target\personal-finance-manager-1.0.0.jar
```

The app starts at: **http://localhost:8080**

---

## H2 Database Console (Browser)
URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:financeDB`
- Username: `sa`
- Password: *(leave blank)*

> **Note:** Data is in-memory and resets on every restart.

---

## API Endpoints

### Auth
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |

### Transactions
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/transactions` | Create transaction |
| GET | `/api/transactions` | Get all (supports filters: startDate, endDate, category, type) |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Categories
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/categories` | Get all categories |
| POST | `/api/categories` | Create custom category |
| DELETE | `/api/categories/{name}` | Delete custom category |

### Savings Goals
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/goals` | Create goal |
| GET | `/api/goals` | Get all goals |
| GET | `/api/goals/{id}` | Get specific goal |
| PUT | `/api/goals/{id}` | Update goal |
| DELETE | `/api/goals/{id}` | Delete goal |

### Reports
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/reports/monthly/{year}/{month}` | Monthly report |
| GET | `/api/reports/yearly/{year}` | Yearly report |

---

## Quick Test with curl (Windows 11)

Open **Command Prompt** and run:

```
# Register
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"john@example.com\",\"password\":\"pass123\",\"fullName\":\"John Doe\"}"

# Login (saves session cookie)
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"john@example.com\",\"password\":\"pass123\"}" -c cookies.txt

# Create a transaction
curl -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -b cookies.txt -d "{\"amount\":50000,\"date\":\"2026-05-01\",\"category\":\"Salary\",\"description\":\"May Salary\"}"

# Get transactions
curl http://localhost:8080/api/transactions -b cookies.txt

# Monthly report
curl http://localhost:8080/api/reports/monthly/2026/5 -b cookies.txt
```

---

## Default Categories

**Expense:** Food & Dining, Transportation, Housing, Utilities, Healthcare, Entertainment, Shopping, Education, Personal Care, Others

**Income:** Salary, Freelance, Business, Investment, Rental, Other Income

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `java: command not found` | Reinstall JDK 17 and restart CMD |
| `mvn: command not found` | Add Maven `bin` to PATH, restart CMD |
| Port 8080 in use | Run `netstat -ano \| findstr :8080` then kill the process, or change `server.port` in `application.properties` |
| Build fails | Run `mvn clean` then retry |
