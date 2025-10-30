# Loan Origination System — Backend

A Spring Boot 3 application that powers the Loan Origination demo, handling borrower applications, loan decisioning, and offer computation logic.

This backend exposes REST APIs consumed by the React frontend. It follows **Clean Architecture** principles with DTOs, entities, mappers, and service layers — designed to demonstrate production-grade structure for a take-home or live coding interview.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3**
    - Spring Web (REST APIs)
    - Spring Data JPA (PostgreSQL/H2)
    - Jakarta Validation
- **MapStruct** (for DTO–Entity mapping)
- **Lombok**
- **Flyway** (DB migrations)
- **Slf4j + Logback** (logging)
- **Gradle (Kotlin DSL)** for build
- **H2** in-memory DB (for local testing)

---

## Architecture Overview

| Layer | Description |
|-------|--------------|
| **Controller** | Handles incoming REST API requests |
| **Service** | Encapsulates business logic — loan approval rules, offer calculation |
| **Mapper** | Uses MapStruct to convert between DTOs and entities |
| **Entity / Repository** | JPA entities mapped to normalized tables (Applicant, LoanApplication, LoanOffer) |
| **Util** | Helper classes (e.g., `CryptoUtil` for SSN encryption) |
| **Exception** | Global error handling with `@RestControllerAdvice` |

---

## Security & Data Privacy

- **Sensitive data encryption:** SSNs are encrypted before saving to the database using `CryptoUtil`.
- **Validation:** DTOs use `jakarta.validation` annotations to ensure clean, validated input.
- **Global exception handling:** All unhandled exceptions are captured and logged with `GlobalExceptionHandler`.

---

## Business Rules Implemented

1. Borrower applies for a loan with required details.
2. Number of open credit lines is randomly generated (0–100).
3. Application approval rules:
    - Requested amount is < 10k  and  > 50k → **Denied**
    - Credit lines > 50 → **Denied**
    - Credit lines < 10 → 36 months @ 10% interest
    - Credit lines 10–50 → 24 months @ 20% interest
4. Approved loans return a computed monthly payment, interest rate, and term.

---

## Sample API Flow

### Endpoint
`POST /api/loan-applications/apply`

### Request (JSON)
```json
{
  "name": "Jane Doe",
  "address": "123 Main St",
  "email": "jane@example.com",
  "phone": "5551112222",
  "ssn": "1234567890",
  "requestedAmount": 25000,
}
```

### Response (JSON)
```json
{
  "decision": "APPROVED",
  "reason": null,
  "offer": {
    "totalLoanAmount": 25000,
    "interestRate": 0.20,
    "termMonths": 24,
    "monthlyPayment": 1271.52
  }
}
```
### Running Locally
1️⃣ Clone the repo
```
bash

git clone https://github.com/yourusername/loan-origination-backend.git
cd loan-origination-backend
```
2️⃣ Build the project
```
bash

./gradlew clean build
```
3️⃣ Run the application
```
bash

./gradlew bootRun
```
Backend will start at:
👉 http://localhost:8080

#### Useful Endpoints

**POST**	/api/loan-applications/apply	Submit a new loan application
**GET**	/actuator/health	Health check

### Database Schema

Normalized into 3 tables:

applicant → Stores borrower PII (SSN encrypted)

loan_application → Stores each loan request

loan_offer → Stores offer decisions and loan terms

### Testing
Unit Tests (JUnit 5 + Mockito)
```
bash

./gradlew test
```
