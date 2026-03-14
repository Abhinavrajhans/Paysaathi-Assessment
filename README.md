# PaySathi — Accounting Integration Service

A Spring Boot service that integrates with an external accounting system, syncs financial data locally, and exposes credit insight APIs for receivables management.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup & Running](#setup--running)
- [Project Structure](#project-structure)
- [Architecture & Design Decisions](#architecture--design-decisions)
- [API Reference](#api-reference)
- [How the Sync Works](#how-the-sync-works)
- [Error Handling](#error-handling)
- [Assumptions Made](#assumptions-made)

---

## Overview

This service solves a common integration problem: a small business uses an external accounting system, and we need to pull customer, invoice, and payment data from it, store it locally, and expose useful financial insights on top of it.

**What it does:**
- Connects to an external accounting API and syncs customers, invoices, and payments into a local MySQL database
- Runs an initial sync on startup, then re-syncs automatically every 6 hours via a scheduler
- Exposes REST APIs for credit insights: outstanding balances, overdue invoices, and customer risk profiles
- Tracks every sync operation in a `sync_logs` table for observability

**Since no real external API was provided**, the external accounting system is simulated using **WireMock**, which boots automatically on port `8089` alongside the Spring Boot app and serves realistic stub responses from JSON mapping files.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 24 |
| Framework | Spring Boot 4.0.3 |
| Database | MySQL 8 |
| Build Tool | Gradle |
| HTTP Client | Spring `RestClient` |
| External API Mock | WireMock 3.5.4 |
| ORM | Spring Data JPA + Hibernate |
| Boilerplate Reduction | Lombok |

---

## Prerequisites

Before running the project, ensure you have the following installed:

- Java 24
- MySQL 8 (running locally)
- Gradle (or use the included `./gradlew` wrapper)

---

## Setup & Running

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd PaySathi
```

### 2. Create the MySQL database

```sql
CREATE DATABASE paysathi;
```

### 3. Configure database credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paysathi
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Run the application

```bash
./gradlew bootRun
```

That's it. On startup the application will:

1. Boot WireMock on port `8089` — the mock external accounting API
2. Auto-create all database tables via JPA (`ddl-auto=update`)
3. Immediately run a full sync — fetching customers, invoices, and payments from WireMock into MySQL
4. Start the scheduler for periodic re-syncs every 6 hours

### 5. Verify it's working

Once the app starts, hit any insight endpoint:

```bash
curl http://localhost:8080/api/insights/customers
```

You should see all 3 synced customers with their financial summaries.

---

## Project Structure

```
src/main/java/com/example/PaySathi/
├── config/
│   ├── AppConfig.java              # RestClient bean
│   ├── SyncOnStartup.java          # Triggers sync immediately on boot
│   ├── SyncScheduler.java          # Periodic sync every 6 hours
│   └── WireMockConfig.java         # Starts WireMock server on port 8089
│
├── controllers/
│   └── InsightsController.java     # All insight API endpoints
│
├── dto/                            # External API response shapes (input)
│   ├── CustomerDTO.java
│   ├── InvoiceDTO.java
│   ├── PaymentDTO.java
│   ├── CustomerSummaryResponse.java
│   ├── CustomerCreditResponse.java
│   └── OverdueInvoiceResponse.java
│
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── SyncException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│
├── gateway/
│   ├── AccountingGateway.java       # Interface — the contract
│   └── AccountingGatewayImpl.java   # HTTP implementation using RestClient
│
├── mapper/
│   ├── CustomerMapper.java
│   ├── InvoiceMapper.java
│   └── PaymentMapper.java
│
├── models/
│   ├── BaseEntity.java              # Shared id, createdAt, updatedAt
│   ├── Customer.java
│   ├── Invoice.java
│   ├── InvoiceStatus.java
│   ├── Payment.java
│   ├── SyncLog.java
│   └── SyncStatus.java
│
├── repositories/
│   ├── CustomerRepository.java
│   ├── InvoiceRepository.java
│   ├── PaymentRepository.java
│   └── SyncLogRepository.java
│
└── services/
    ├── SyncService.java             # Orchestrates sync order only
    ├── CustomerService.java         # Customer sync logic
    ├── InvoiceService.java          # Invoice sync logic
    ├── PaymentService.java          # Payment sync logic
    ├── SyncLogService.java          # Shared sync logging
    └── InsightsService.java         # All insight query logic

src/main/resources/
├── application.properties
└── wiremock/
    └── mappings/                    # WireMock stub JSON files
        ├── customers.json
        ├── customer-by-id.json
        ├── invoices.json
        ├── invoice-by-id.json
        ├── invoices-by-customer.json
        ├── payments.json
        ├── payment-by-id.json
        └── payments-by-invoice.json
```

---

## Architecture & Design Decisions

### 1. Gateway Pattern + Anti-Corruption Layer

All communication with the external accounting API is isolated inside the `gateway` package. `AccountingGateway` is an interface and `AccountingGatewayImpl` is the HTTP implementation.

**Why:** The rest of the application never directly imports HTTP or external API concerns. If the external system changes from REST to GraphQL tomorrow, only the `gateway` package changes. Nothing in the service layer needs to know about it.

### 2. Adapter Pattern — Mappers

Mapping from external API DTOs to internal JPA models is handled by dedicated `Mapper` classes. Services never build model objects themselves.

**Why:** DTOs represent what the external API sends. Models represent what our database stores. These are two different things and should not be coupled. The `CustomerDTO` has a field called `id` which maps to `externalId` in our model — this is a deliberate translation that lives in the mapper, not scattered across the codebase.

### 3. Dual ID Strategy — `externalId` + Internal `id`

Every entity stores both an internal auto-increment `id` (primary key used for all FK relationships) and an `externalId` (the ID from the external accounting system, stored as a unique varchar).

**Why:** If the external system changes its IDs, migrates, or is replaced entirely, our internal FK relationships remain intact. The `externalId` is only a lookup key used during sync to match incoming records to existing ones.

### 4. Upsert Pattern

During every sync, the service does:
```java
Entity entity = repository.findByExternalId(dto.getExternalId())
                           .orElseGet(Entity::new);
mapper.mapToEntity(dto, entity);
repository.save(entity);
```

JPA's `save()` checks if the entity has an `id`. If yes → UPDATE. If no → INSERT. This means running the sync 100 times never creates duplicate records — it is always idempotent.

### 5. Sync Order — Customers → Invoices → Payments

The sync always runs in this fixed order because of FK dependencies. Invoices reference customers. Payments reference invoices. If a customer doesn't exist locally when an invoice tries to sync, the invoice is skipped with a warning log rather than failing the entire sync.

### 6. `SyncLog` Table — Observability

Every sync operation — whether it succeeds or fails — is recorded in the `sync_logs` table with entity type, status, records fetched, records saved, error message, start time, and end time. This makes the integration observable without needing to dig through application logs.

### 7. `SyncLogService` — No Code Duplication

The three helper methods `startLog()`, `completeLog()`, and `failLog()` are extracted into a shared `SyncLogService` rather than being duplicated across `CustomerService`, `InvoiceService`, and `PaymentService`.

### 8. `fixedDelay` vs `fixedRate` for Scheduler

The scheduler uses `fixedDelay` (not `fixedRate`). `fixedDelay` waits for the previous run to finish before starting the next timer. `fixedRate` would start the next run on a fixed clock regardless of whether the previous run is still executing — this can cause two sync jobs to run concurrently, leading to race conditions and duplicate processing. `fixedDelay` is the safe choice for sync jobs.

### 9. Status Derivation — We Own the Business Logic

The external API sends an invoice status field, but we do not blindly trust it. During sync, `InvoiceMapper.deriveStatus()` recalculates the status based on our own rules:

- If `dueDate` has passed and `paidAmount < totalAmount` → `OVERDUE` (regardless of what the external API says)
- Otherwise → trust the external status

**Why:** The external system might have stale status values or different business rules. We own the definition of "overdue" for our product.

### 10. `outstandingAmount` — Stored, Not Calculated on the Fly

`outstandingAmount` is stored explicitly in the `invoices` table and recalculated every time an invoice is saved via `recalculateOutstanding()`. It could be derived as `totalAmount - paidAmount` at query time, but storing it makes insight queries fast and simple — especially for aggregations across many invoices.

### 11. N+1 Query Prevention — `JOIN FETCH`

`getAllOverdueInvoices()` needs the customer name for each invoice. Since `Invoice.customer` is `FetchType.LAZY`, calling `invoice.getCustomer()` inside a loop would trigger one DB query per invoice. Instead, `InvoiceRepository.findByStatusWithCustomer()` uses `JOIN FETCH` to load invoices and their customers in a single query.

### 12. Global Exception Handler — `@RestControllerAdvice`

All exception handling is centralised in `GlobalExceptionHandler`. Every error response returns the same consistent JSON shape with `status`, `error`, `message`, `path`, and `timestamp`. A missing customer returns `404`. A sync failure returns `500`. An unexpected error returns `500` with a safe generic message (no stack trace leakage).

### 13. WireMock for External API Simulation

Since no external API sandbox was provided, WireMock was used to simulate it. WireMock starts as an embedded server on port `8089` when the Spring Boot app boots and serves stub responses from JSON mapping files in `src/main/resources/wiremock/mappings/`. This approach version-controls the mock alongside the code and requires zero external tooling to run the project.

---

## API Reference

All insight endpoints are under `/api/insights`.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/insights/customers` | All customers with financial summaries |
| `GET` | `/api/insights/customers/{externalId}/summary` | Single customer summary |
| `GET` | `/api/insights/customers/{externalId}/credit-profile` | Customer credit risk profile |
| `GET` | `/api/insights/overdue` | All overdue invoices across all customers |

---

### 1. Get All Customer Summaries

```
GET /api/insights/customers
```

Returns every customer with their total invoice count, overdue invoice count, total billed, total paid, and total outstanding balance. Good for a portfolio-level dashboard view.

**Sample Response:**
```json
[
  {
    "externalId": "CUST-001",
    "name": "Ravi Shankar Enterprises",
    "email": "ravi@shankar.com",
    "totalInvoices": 2,
    "overdueInvoices": 1,
    "totalBilled": 80000.00,
    "totalPaid": 20000.00,
    "totalOutstanding": 60000.00
  },
  {
    "externalId": "CUST-002",
    "name": "Priya Textiles Pvt Ltd",
    "email": "priya@textiles.com",
    "totalInvoices": 2,
    "overdueInvoices": 0,
    "totalBilled": 115000.00,
    "totalPaid": 75000.00,
    "totalOutstanding": 40000.00
  },
  {
    "externalId": "CUST-003",
    "name": "Mehta & Sons Trading",
    "email": "mehta@sons.com",
    "totalInvoices": 1,
    "overdueInvoices": 1,
    "totalBilled": 90000.00,
    "totalPaid": 0.00,
    "totalOutstanding": 90000.00
  }
]
```

---

### 2. Get Single Customer Summary

```
GET /api/insights/customers/{externalId}/summary
```

Returns the financial summary for a specific customer.

**Example:** `GET /api/insights/customers/CUST-001/summary`

**Sample Response:**
```json
{
  "externalId": "CUST-001",
  "name": "Ravi Shankar Enterprises",
  "email": "ravi@shankar.com",
  "totalInvoices": 2,
  "overdueInvoices": 1,
  "totalBilled": 80000.00,
  "totalPaid": 20000.00,
  "totalOutstanding": 60000.00
}
```

**Error — Customer Not Found:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found: CUST-999",
  "path": "/api/insights/customers/CUST-999/summary",
  "timestamp": "2026-03-15T10:30:00"
}
```

---

### 3. Get Customer Credit Profile

```
GET /api/insights/customers/{externalId}/credit-profile
```

Returns a full credit risk profile for the customer — total outstanding balance, all overdue invoices with exact days overdue, and a computed risk level.

**Example:** `GET /api/insights/customers/CUST-001/credit-profile`

**Risk Level Logic:**

| Risk Level | Condition |
|---|---|
| `LOW` | No overdue invoices |
| `MEDIUM` | 1–2 overdue invoices AND outstanding under ₹1,00,000 |
| `HIGH` | 3+ overdue invoices OR outstanding above ₹1,00,000 |

**Sample Response:**
```json
{
  "externalId": "CUST-001",
  "name": "Ravi Shankar Enterprises",
  "totalOutstanding": 60000.00,
  "overdueCount": 1,
  "riskLevel": "MEDIUM",
  "overdueInvoices": [
    {
      "invoiceExternalId": "INV-002",
      "customerName": "Ravi Shankar Enterprises",
      "customerExternalId": "CUST-001",
      "totalAmount": 30000.00,
      "paidAmount": 0.00,
      "outstandingAmount": 30000.00,
      "dueDate": "2024-12-01",
      "daysOverdue": 104
    }
  ]
}
```

**Error — Customer Not Found:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found: CUST-999",
  "path": "/api/insights/customers/CUST-999/credit-profile",
  "timestamp": "2026-03-15T10:30:00"
}
```

---

### 4. Get All Overdue Invoices

```
GET /api/insights/overdue
```

Returns all overdue invoices across every customer in the system. Each entry includes the customer it belongs to, the outstanding amount, the due date, and the exact number of days it has been overdue. Results are fetched using a single `JOIN FETCH` query to avoid N+1 database calls.

**Sample Response:**
```json
[
  {
    "invoiceExternalId": "INV-002",
    "customerName": "Ravi Shankar Enterprises",
    "customerExternalId": "CUST-001",
    "totalAmount": 30000.00,
    "paidAmount": 0.00,
    "outstandingAmount": 30000.00,
    "dueDate": "2024-12-01",
    "daysOverdue": 104
  },
  {
    "invoiceExternalId": "INV-005",
    "customerName": "Mehta & Sons Trading",
    "customerExternalId": "CUST-003",
    "totalAmount": 90000.00,
    "paidAmount": 0.00,
    "outstandingAmount": 90000.00,
    "dueDate": "2024-11-01",
    "daysOverdue": 134
  }
]
```

---

### Error Response Shape

All errors across every endpoint return the same consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found: CUST-999",
  "path": "/api/insights/customers/CUST-999/summary",
  "timestamp": "2026-03-15T10:30:00"
}
```

---

## How the Sync Works

```
App Starts
    │
    ├── WireMock boots on port 8089
    │
    ├── SyncOnStartup triggers syncAll()
    │       │
    │       ├── syncCustomers()   → fetch from WireMock → upsert into DB → log result
    │       ├── syncInvoices()    → fetch from WireMock → upsert into DB → log result
    │       └── syncPayments()    → fetch from WireMock → upsert into DB → log result
    │
    └── Scheduler fires every 6 hours (fixedDelay)
            └── syncAll() repeats
```

Every sync is recorded in `sync_logs`:

| Column | Description |
|---|---|
| `entity_type` | CUSTOMER / INVOICE / PAYMENT |
| `status` | IN_PROGRESS → SUCCESS or FAILED |
| `records_fetched` | How many records came from the external API |
| `records_saved` | How many were actually saved (skipped records not counted) |
| `error_message` | Populated only on failure |
| `started_at` | When the sync began |
| `completed_at` | When it finished |

---

## Error Handling

| Scenario | Behaviour |
|---|---|
| Customer not found in insight API | `404 Not Found` with descriptive message |
| Sync job fails | `SyncException` thrown, logged to `sync_logs` with error message, `500` returned |
| Invoice references unknown customer | Invoice skipped with a `WARN` log, sync continues for remaining records |
| Payment references unknown invoice | Payment skipped with a `WARN` log, sync continues |
| Unexpected exception | `500 Internal Server Error` with safe generic message |

---

## Assumptions Made

1. **No external API was provided** — WireMock was used to simulate it with realistic stub data including overdue invoices, partially paid invoices, and multiple payments per invoice.

2. **Sync is full, not delta** — Every sync fetches all records from the external API. In production, an incremental/delta sync using `syncedAt` as a cursor would be more efficient, but full sync is appropriate for the scope of this exercise.

3. **`outstandingAmount` is stored explicitly** — Rather than computing it at query time, it is stored on the invoice and recalculated on every save. This is a deliberate performance trade-off.

4. **Risk level thresholds are hardcoded** — The `MEDIUM` / `HIGH` risk boundary (₹1,00,000 outstanding, 2 overdue invoices) is defined in code. In production these would be externalised to `application.properties` or a configuration table.

5. **WireMock runs embedded** — For simplicity, WireMock is started inside the Spring Boot process. In a real setup, the external API would be a separate service and the base URL would point to it via an environment variable.

6. **Single-instance scheduler** — The scheduler assumes a single app instance. In a multi-instance deployment, a distributed lock (e.g. ShedLock) would be needed to prevent concurrent syncs across instances.
